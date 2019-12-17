package com.qujie.timedtask.business.task;

import com.qujie.timedtask.business.dao.BRebateDao;
import com.qujie.timedtask.business.dao.BehalfPayDao;
import com.qujie.timedtask.common.utils.BigDecimalUtils;
import com.qujie.timedtask.common.utils.RedisUtils;
import com.qujie.timedtask.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代付定时任务
 *
 * @author ys
 * @date 2019/10/14 12:47
 */
@Component
public class TaskBehalfPay {

    private Logger logger = LoggerFactory.getLogger(TaskBehalfPay.class);

    @Autowired
    public BehalfPayDao behalfPayDao;

    @Autowired
    public BRebateDao bRebateDao;


    public static TaskBehalfPay TaskBehalfPay;

    @PostConstruct
    public void init() {
        TaskBehalfPay = this;
        TaskBehalfPay.behalfPayDao = this.behalfPayDao;
        TaskBehalfPay.bRebateDao = this.bRebateDao;
    }

    @Autowired
    private RedisUtils redisUtil;

    @Autowired
    private Config config;

    @Scheduled(cron = "0 00 0 1 * ?")//每月一号零点执行
//    @Scheduled(cron = "0 00 00 * * ?")//每天00：00 执行
    public void removeRebateShop() {
        try {
            logger.info("开始删除Redis商户返利金流水上限");
            List<String> shopIdList = TaskBehalfPay.bRebateDao.getShopId();
            shopIdList.stream().forEach(c->{
                redisUtil.del("rebatemoneyshopid:"+c);
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除商户返利金上限定时任务异常:"+e.getMessage());
        }
    }

    /**
     * 易生定时任务
     */
    @Scheduled(cron = "0 50 00 * * ?")//每天00：50 执行
//    @Scheduled(cron = "0 28 10 * * ?")//每天00：50 执行
    @Transactional
    public void YStaskPay() {
        try {
            List<Map<String, Object>> maps = TaskBehalfPay.behalfPayDao.smpayTraderecordNoList();//获取没有代付的订单
            maps.stream().forEach(t -> {
                Object num = redisUtil.get("paybehalf:" + t.get("id").toString().toUpperCase());
                logger.info("易生定时任务-Redis：" + num);
                if (num != null) {
                    if (Integer.parseInt(num.toString()) > 1) {
                        return;
                    }
                }
                double d = Double.parseDouble(t.get("payamount").toString()) - Double.parseDouble(t.get("payfee").toString());
                double vipmemberreduceamount = Double.parseDouble(t.get("vipmemberreduceamount").toString());
                if (d > 0) {
                    logger.info("易生定时任务-代付金额：" + BigDecimalUtils.formatDouble(d * 100) + "分");
                    Map<String, String> payMap = new HashMap<>();
                    payMap.put("accountNo", t.get("cardnum").toString());//收款账号
                    payMap.put("accName", t.get("bankacctname").toString());//收款账户
                    payMap.put("amtType", "0");//交易方式（0：授信余额；1：资金余额/结算资金）
                    payMap.put("cardKind", "D");//卡种（D借记卡，C信用卡,H合一卡，X未知）
                    String bankCode = "";
                    String accType = "00";
                    if ("对私".equals(t.get("accounttype").toString())) {
                        accType = "00";
                    }
                    if ("对公".equals(t.get("accounttype").toString())) {
                        accType = "10";
                        bankCode = t.get("bankCode").toString();
                    }
                    payMap.put("accType", accType);//账户类型: 00 -- 个人帐户，10 -- 对公账户, FF -- 虚拟帐户
                    payMap.put("artificial", "0");
                    payMap.put("bankCode", bankCode);
                    payMap.put("payamount",  BigDecimalUtils.formatDouble((Double.parseDouble(t.get("payamount").toString())*100)+(vipmemberreduceamount * 100)));
                    payMap.put("stlmAmt", BigDecimalUtils.formatDouble(d * 100));//代付款 分！
                    payMap.put("subsidyPayment", BigDecimalUtils.formatDouble(vipmemberreduceamount * 100));//代付款 分！
                    payMap.put("requestId", t.get("ordernum").toString());//请求流水 填写订单号即可
                    logger.info("易生定时任务-开始发起代付：" + payMap);
                    Object o = YSBehalfPay.behalfPay(payMap);
                    logger.info("易生定时任务-订单【" + t.get("ordernum").toString() + "】代付返回结果：：" + o);
                } else {
                    logger.info("易生定时任务-联机代付订单金额为0");
                    int count = TaskBehalfPay.behalfPayDao.updateSmpayTraderecordispaybehalf(t.get("id").toString());
                    logger.info("易生定时任务-修改没有联机代付状态：" + count);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("易生定时任务异常：" + e.getMessage());
        }

    }


    /**
     * 易生定时任务 删除Key
     */
    @Scheduled(cron = "0 00 03 * * ?")//每天03：00 执行
    public void YStaskPayDelkey() {
        try {
            logger.info("易生定时任务删除RedisKey-开始执行");
            List<Map<String, Object>> list = TaskBehalfPay.behalfPayDao.orderNumList();
            logger.info("易生定时任务删除RedisKey-获取代付成功的订单数量：" + list.size());
            list.stream().forEach(t -> {
                if (t.containsKey("id")) {
                    redisUtil.del("paybehalf:" + t.get("id").toString().toUpperCase());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("易生定时删除RedisKey异常：" + e.getMessage());
        }

    }


    /**
     * 次日下架
     */
    @Scheduled(cron = "0 03 00 * * ?")//每天00：03 执行
    @Transactional
    public void nextDayFrame() {
        try {
            logger.info("开始执行次日下架定时任务");
            List<String> marketingVouchersTemporaryList = TaskBehalfPay.behalfPayDao.MarketingVouchersTemporaryList();
            logger.info("次日下架定时任务-获取临时表数量：" + marketingVouchersTemporaryList.size());
            marketingVouchersTemporaryList.stream().forEach(t -> {
                int i = TaskBehalfPay.behalfPayDao.updateActivitystatus(t);
                if (i > 0) {
                    TaskBehalfPay.behalfPayDao.deleteMarketingVouchersTemporary(t);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("次日下架定时任务异常：" + e.getMessage());
        }

    }


    /**
     * 商户返利金代付定时任务
     */
//    @Scheduled(cron = "0 45 00 * * ?")//每天00：45 执行
    public void taskRebateTransactionsBehalfPay() {
        try {
            List<Map<String, Object>> behalfPay = TaskBehalfPay.bRebateDao.getBehalfPay();
            if (behalfPay.size() > 0) {
                behalfPay.stream().forEach(t -> {
                    Map<String, String> payMap = new HashMap<>();
                    double d = Double.valueOf(t.get("money").toString());//提现金额
                    payMap.put("accountNo", t.get("cardnum").toString());//收款账号
                    payMap.put("accName", t.get("bankacctname").toString());//收款账户
                    payMap.put("amtType", "1");//交易方式（0：授信余额；1：资金余额/结算资金）
                    payMap.put("cardKind", "D");//卡种（D借记卡，C信用卡,H合一卡，X未知）
                    String accType = "00";
                    String bankCode = "";
                    if ("对私".equals(t.get("accounttype").toString())) {
                        accType = "00";
                    }
                    if ("对公".equals(t.get("accounttype").toString())) {
                        accType = "10";
                        bankCode = t.get("bankCode").toString();
                    }
                    payMap.put("accType", accType);//账户类型: 00 -- 个人帐户，10 -- 对公账户, FF -- 虚拟帐户
                    payMap.put("bankCode", bankCode);
                    payMap.put("stlmAmt", BigDecimalUtils.formatDouble(d * 100));//代付款 分！
                    String requestId = t.get("transactionNum").toString();
                    payMap.put("requestId", requestId);//请求流水 填写订单号即可
                    logger.info("返利金定时任务-开始发起代付：" + payMap);
                    boolean aBoolean = YSBehalfPay.RebateBehalfPay(payMap);
                    logger.info("返利金定时任务代付结果:" + aBoolean);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("返利金代付定时任务异常：" + e.getMessage());
        }
    }




}
