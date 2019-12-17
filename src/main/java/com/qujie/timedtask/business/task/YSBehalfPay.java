package com.qujie.timedtask.business.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qujie.timedtask.async.AsyncTask;
import com.qujie.timedtask.business.dao.BRebateDao;
import com.qujie.timedtask.business.dao.BehalfPayDao;
import com.qujie.timedtask.common.utils.BigDecimalUtils;
import com.qujie.timedtask.common.utils.DES;
import com.qujie.timedtask.common.utils.HttpPost;
import com.qujie.timedtask.common.utils.RedisUtils;
import com.qujie.timedtask.config.YSBehalfPayConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 易生代付
 *
 * @author ys
 * @date 2019/10/17 13:05
 */
@RestController
@RequestMapping("/ys")
@Component
public class YSBehalfPay {
    public  static Lock lock =  new ReentrantLock();
    @Autowired
    public AsyncTask asyncTask;
    @Autowired
    public BehalfPayDao behalfPayDao;
    @Autowired
    public BRebateDao bRebateDao;

    @Autowired
    public RedisUtils redisUtils;

    public static YSBehalfPay ysBehalfPay;

    @PostConstruct
    public void init() {
        ysBehalfPay = this;
        ysBehalfPay.asyncTask = this.asyncTask;
        ysBehalfPay.behalfPayDao = this.behalfPayDao;
        ysBehalfPay.bRebateDao = this.bRebateDao;
        ysBehalfPay.redisUtils = this.redisUtils;
    }

    private static Logger logger = LoggerFactory.getLogger(YSBehalfPay.class);

    /**
     * 测试代付交易
     *
     * @return
     */
    @RequestMapping("/Pay")
    public Object testPay() {
        Map<String, String> map = new HashMap<>();
        map.put("accountNo", "6212810302001066260");//收款账号
        map.put("accName", "袁帅");//收款账户
        map.put("stlmAmt", "10");//代付款 分！
        map.put("amtType", "0");//交易方式（0：授信余额；1：资金余额/结算资金）
        map.put("cardKind", "D");//卡种（D借记卡，C信用卡,H合一卡，X未知）
        map.put("accType", "00");//账户类型: 00 -- 个人帐户，10 -- 对公账户, FF -- 虚拟帐户
        map.put("requestId", String.valueOf((int) ((Math.random() * 9 + 1) * 10000000)));//请求流水 填写订单号即可
        Object o = behalfPay(map);
        return o;
    }

    /**
     * 测试查询
     *
     * @return
     */
    @RequestMapping("/query")
    public Object testQuery(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("oldRequestId", request.getParameter("oldRequestId"));//原交易流水号
        map.put("payDate", "20191216");//原付款日期
        map.put("requestId", String.valueOf((int) ((Math.random() * 9 + 1) * 10000000)));//新请求流水
        Object o = query(map);
        return o;
    }

    /**
     * 测试额度查询
     *
     * @return
     */
    @RequestMapping("/queryQuota")
    public Object testqueryQuota() {
        Object o = queryQuota(String.valueOf((int) ((Math.random() * 9 + 1) * 10000000)));
        return o;
    }

    /**
     * 测试代付额度调整明细查询
     *
     * @return
     */
    @RequestMapping("/queryDetailedQuota")
    public Object queryDetailedQuota() {
        Map<String, String> map = new HashMap<>();
        map.put("pageNumber", "0");//页码（初始值0）
        map.put("pageSize", "30");//每页大小
        map.put("payDate", "20191201");//调整日期
        map.put("requestId", String.valueOf((int) ((Math.random() * 9 + 1) * 10000000)));//新请求流水
        Object o = queryDetailedQuota(map);
        return o;
    }


    /**
     * 代付请求
     *
     * @param param
     * @return
     */
    public static Boolean behalfPay(Map<String, String> param) {
        /**返利金开始*/
        Map<String, Object> map = new HashMap<>();
        double rebateMoney = 0;

        /**返利金结束*/
        String data = "";
        try {
            if (Integer.parseInt(param.get("artificial")) == 0) {//是否是人工手动代付 0：订单代付，1：人工代付
                String isTrue = getIsTrue(param.get("requestId"), String.valueOf(Double.parseDouble(param.get("payamount")) / 100));
                if (Double.parseDouble(param.get("payamount"))>=100){
                    map = RebateTransactions(param.get("requestId"), String.valueOf(Double.parseDouble(param.get("payamount")) / 100));
                    if (map.size() > 0) {
                        map.put("payamount",String.valueOf(Double.parseDouble(param.get("payamount")) / 100));
                        rebateMoney = Double.parseDouble(map.get("amount").toString()) * 100;
                    }
                }
                if (!StringUtils.isBlank(isTrue)){
                    String shopMoney = ysBehalfPay.redisUtils.get("rebatemoneyshopid:" + isTrue).toString().replace("\"","");//获取商户月流水上限总额
                    ysBehalfPay.redisUtils.set("rebatemoneyshopid:" + isTrue,BigDecimalUtils.formatDouble(Double.parseDouble(shopMoney)+Double.parseDouble(param.get("payamount"))/100));
                }
                if (ysBehalfPay.behalfPayDao.getPayCount(param.get("requestId")) <= 0) {
                    return false;
                }
            }

            logger.debug("易生代付-传入参数：" + param.toString());

            String subsidyPayment = param.get("subsidyPayment");//补贴金额
            String stlmAmt = param.get("stlmAmt");//实际支付金额
            double pay = Double.parseDouble(stlmAmt) + Double.parseDouble(subsidyPayment) + rebateMoney;//代付金额
            logger.debug("易生代付-代付金额：" + pay);
            String signStr = param.get("accountNo") + param.get("requestId") + pay + rebateMoney;
            String encode = DES.encryptECB3Des(YSBehalfPayConfig.privateKey, signStr);//加密
            logger.debug("易生代付-加解密");
            Map<String, String> dataMap = new HashMap<>();
            String payDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            dataMap.put("payDate", payDate);//付款日期YYYYMMDD
            dataMap.put("otherCode", YSBehalfPayConfig.otherCode);//第三方标识
            dataMap.put("accountNo", param.get("accountNo"));//入账账号
            dataMap.put("accName", param.get("accName"));//入账户号
            dataMap.put("accType", param.get("accType"));//账户类型: 00 -- 个人帐户，10 -- 对公账户, FF -- 虚拟帐户
            dataMap.put("cardKind", param.get("cardKind"));//卡种（D借记卡，C信用卡,H合一卡，X未知）
            dataMap.put("bankCode", param.get("bankCode"));//开户行代码 YSBehalfPayConfig.bankCode
            dataMap.put("bankName", YSBehalfPayConfig.bankName);//开户行名称
            dataMap.put("stlmAmt", BigDecimalUtils.formatDouble(pay));//应代付金额（分）
            dataMap.put("amtType", param.get("amtType"));//交易方式（0：授信余额；1：资金余额/结算资金）
            dataMap.put("payFlag", YSBehalfPayConfig.payFlag);//业务类型（1转账；…）
            dataMap.put("qztransChennl", YSBehalfPayConfig.qztransChennl);//出款渠道标识号
            dataMap.put("backUrl", YSBehalfPayConfig.backUrl);//回调

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("tradeCode", "D00001");//请求码
            postMap.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));//请求时间戳（格式YYYYMMDDHH24MISS
            postMap.put("deviceIp", "");//请求设备ip
            postMap.put("channelId", YSBehalfPayConfig.channelId);//请求渠道标识
            String requestId = param.get("requestId");
            postMap.put("requestId", requestId);//请求流水号（和渠道标识唯一标识一个请求），纯数字
            postMap.put("signStr", encode);//签名（data域数据为签名数据串）
            postMap.put("data", dataMap);
            data = JSON.toJSONString(postMap);
            logger.debug("易生代付-发送请求：" + data);
            String resp = HttpPost.ysPost(YSBehalfPayConfig.url, data);
            logger.debug("易生代付-响应参数：" + resp);
            logger.debug("易生代付-进入异步");
            ysBehalfPay.asyncTask.callback(resp, payDate, param.get("artificial"), pay, map);
            logger.debug("易生代付-解析json");
            JSONObject jsonObject = JSON.parseObject(resp);
            /**发送记录保存*/
            logger.debug("易生代付-发送记录保存");
            Map<String, Object> saveMap = new HashMap<>();
            saveMap.put("id", UUID.randomUUID().toString());
            saveMap.put("orderId", requestId);
            saveMap.put("actualPayment", Double.parseDouble(stlmAmt) / 100);
            saveMap.put("subsidyPayment", Double.parseDouble(subsidyPayment) / 100);
            saveMap.put("sumPayment", pay / 100);
            saveMap.put("rebatePayment", rebateMoney / 100);
            saveMap.put("requestMsg", data);
            saveMap.put("responseMsg", resp);
            saveMap.put("addtime", new Date());
            logger.debug("易生代付-返回状态");
            if (!"XXXXXX".equals(jsonObject.get("acceRetCode").toString())) {
                saveMap.put("success", 1);
                ysBehalfPay.behalfPayDao.insertYSBehalfPaySendRecord(saveMap);
                return true;
            }
            saveMap.put("success", 0);
            ysBehalfPay.behalfPayDao.insertYSBehalfPaySendRecord(saveMap);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("易生代付-异常原因："+e.toString());
            Map<String, Object> saveMap = new HashMap<>();
            saveMap.put("id", UUID.randomUUID().toString());
            saveMap.put("orderId", param.get("requestId"));
            saveMap.put("actualPayment", Double.parseDouble(param.get("stlmAmt")) / 100);
            saveMap.put("subsidyPayment", Double.parseDouble(param.get("subsidyPayment")) / 100);
            double pay = Double.parseDouble(param.get("stlmAmt")) + Double.parseDouble(param.get("subsidyPayment")) + rebateMoney;
            saveMap.put("sumPayment", pay / 100);
            saveMap.put("requestMsg", data);
            saveMap.put("rebatePayment", rebateMoney / 100);
            saveMap.put("responseMsg", e.getMessage());
            saveMap.put("addtime", new Date());
            saveMap.put("success", 0);
            ysBehalfPay.behalfPayDao.insertYSBehalfPaySendRecord(saveMap);
            logger.error("易生代付接口异常：" + e.getMessage());
            return false;
        }

    }


    /**
     * 返利金充值代付
     *
     * @param param
     * @return
     */
    public static boolean RebateBehalfPay(Map<String, String> param) {
        String data = "";
        try {
            logger.info("易生代付-传入参数：" + param);
            String signStr = param.get("accountNo") + param.get("requestId") + param.get("stlmAmt");
            String encode = DES.encryptECB3Des(YSBehalfPayConfig.privateKey, signStr);//加密

            Map<String, String> dataMap = new HashMap<>();
            String payDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            dataMap.put("payDate", payDate);//付款日期YYYYMMDD
            dataMap.put("otherCode", YSBehalfPayConfig.otherCode);//第三方标识
            dataMap.put("accountNo", param.get("accountNo"));//入账账号
            dataMap.put("accName", param.get("accName"));//入账户号
            dataMap.put("accType", param.get("accType"));//账户类型: 00 -- 个人帐户，10 -- 对公账户, FF -- 虚拟帐户
            dataMap.put("cardKind", param.get("cardKind"));//卡种（D借记卡，C信用卡,H合一卡，X未知）
            dataMap.put("bankCode", param.get("bankCode"));//开户行代码 YSBehalfPayConfig.bankCode
            dataMap.put("bankName", YSBehalfPayConfig.bankName);//开户行名称
            dataMap.put("stlmAmt", param.get("stlmAmt"));//应代付金额（分）
            dataMap.put("amtType", param.get("amtType"));//交易方式（0：授信余额；1：资金余额/结算资金）
            dataMap.put("payFlag", YSBehalfPayConfig.payFlag);//业务类型（1转账；…）
            dataMap.put("qztransChennl", YSBehalfPayConfig.qztransChennl);//出款渠道标识号
            dataMap.put("backUrl", YSBehalfPayConfig.backUrl);//回调

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("tradeCode", "D00001");//请求码
            postMap.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));//请求时间戳（格式YYYYMMDDHH24MISS
            postMap.put("deviceIp", "");//请求设备ip
            postMap.put("channelId", YSBehalfPayConfig.channelId);//请求渠道标识
            String requestId = param.get("requestId");//交易流水

            postMap.put("requestId", requestId);//请求流水号（和渠道标识唯一标识一个请求），纯数字
            postMap.put("signStr", encode);//签名（data域数据为签名数据串）
            postMap.put("data", dataMap);
            data = JSON.toJSONString(postMap);
            logger.info("易生代付-发送请求：" + data);
            String resp = HttpPost.ysPost(YSBehalfPayConfig.url, data);
            logger.info("易生代付-响应参数：" + resp);
            ysBehalfPay.asyncTask.BRebateCallback(resp, payDate, param.get("stlmAmt"));//异步调用
            JSONObject jsonObject = JSON.parseObject(resp);
            Map<String, Object> saveMap = new HashMap<>();
            saveMap.put("id", UUID.randomUUID().toString());
            saveMap.put("orderId", requestId);
            saveMap.put("actualPayment", Double.parseDouble(param.get("stlmAmt")) / 100);
            saveMap.put("subsidyPayment", 0);
            saveMap.put("sumPayment", Double.parseDouble(param.get("stlmAmt")) / 100);
            saveMap.put("requestMsg", data);
            saveMap.put("rebatePayment", 0);
            saveMap.put("responseMsg", resp);
            saveMap.put("addtime", new Date());
            if (!"XXXXXX".equals(jsonObject.get("acceRetCode").toString())) {
                saveMap.put("success", 1);
                ysBehalfPay.behalfPayDao.insertYSBehalfPaySendRecord(saveMap);
                return true;
            }
            saveMap.put("success", 0);
            ysBehalfPay.behalfPayDao.insertYSBehalfPaySendRecord(saveMap);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> saveMap = new HashMap<>();
            saveMap.put("id", UUID.randomUUID().toString());
            saveMap.put("orderId", param.get("requestId"));
            saveMap.put("actualPayment", Double.parseDouble(param.get("stlmAmt")) / 100);
            saveMap.put("subsidyPayment", 0);
            saveMap.put("sumPayment", Double.parseDouble(param.get("stlmAmt")) / 100);
            saveMap.put("requestMsg", data);
            saveMap.put("rebatePayment", 0);
            saveMap.put("responseMsg", e.getMessage());
            saveMap.put("addtime", new Date());
            saveMap.put("success", 0);
            ysBehalfPay.behalfPayDao.insertYSBehalfPaySendRecord(saveMap);
            logger.error("易生代付接口异常：" + e.getMessage());
            return false;
        }

    }

    /**
     * 代付查询
     *
     * @param param
     * @return
     */
    public static Object query(Map<String, String> param) {
        try {
            String signStr = param.get("oldRequestId") + YSBehalfPayConfig.otherCode + param.get("requestId");
            String encode = DES.encryptECB3Des(YSBehalfPayConfig.privateKey, signStr);//加密

            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("payDate", param.get("payDate"));//付款日期YYYYMMDD
            dataMap.put("otherCode", YSBehalfPayConfig.otherCode);//第三方标识
            dataMap.put("oldRequestId", param.get("oldRequestId"));//入账账号

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("tradeCode", "D00002");//请求码
            postMap.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));//请求时间戳（格式YYYYMMDDHH24MISS
            postMap.put("deviceIp", "");//请求设备ip
            postMap.put("channelId", YSBehalfPayConfig.channelId);//请求渠道标识
            postMap.put("requestId", param.get("requestId"));//请求流水号（和渠道标识唯一标识一个请求），纯数字
            postMap.put("signStr", encode);//签名（data域数据为签名数据串）
            postMap.put("data", dataMap);
            String data = JSON.toJSONString(postMap);
            logger.info("易生代付-发送请求：" + data);
            String resp = HttpPost.ysPost(YSBehalfPayConfig.url, data);
            logger.info("易生代付查询响应：" + resp);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("易生代付查询异常：" + e.getMessage());
            return null;
        }
    }

    /**
     * 代付额度查询
     *
     * @param requestId
     * @return
     */
    public static String queryQuota(String requestId) {
        try {
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String signStr = date + requestId;
            String encode = DES.encryptECB3Des(YSBehalfPayConfig.privateKey, signStr);//加密
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("tradeCode", "E00001");//请求码
            postMap.put("reqTime", date);//请求时间戳（格式YYYYMMDDHH24MISS
            postMap.put("deviceIp", "");//请求设备ip
            postMap.put("channelId", YSBehalfPayConfig.channelId);//请求渠道标识
            postMap.put("requestId", requestId);//请求流水号（和渠道标识唯一标识一个请求），纯数字
            postMap.put("signStr", encode);//签名（data域数据为签名数据串）
            postMap.put("data", "");//签名（data域数据为签名数据串）
            String data = JSON.toJSONString(postMap);
            System.out.println("发送数据：" + data);
            logger.info("易生代付额度查询-发送请求：" + data);
            String resp = HttpPost.ysPost(YSBehalfPayConfig.url, data);
            logger.info("易生代付额度查询-响应：" + resp);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("易生代付额度查询异常：" + e.getMessage());
            return null;
        }
    }

    /**
     * 代付额度调整明细查询
     *
     * @param param
     * @return
     */
    public static Object queryDetailedQuota(Map<String, String> param) {
        try {
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String signStr = date + param.get("requestId");
            String encode = DES.encryptECB3Des(YSBehalfPayConfig.privateKey, signStr);//加密
            Map<String, Object> map = new HashMap<>();
            map.put("payDate", param.get("payDate"));//调整日期 yyyyMMdd
            map.put("pageNumber", Integer.parseInt(param.get("pageNumber")));//页码（初始值0）
            map.put("pageSize", Integer.parseInt(param.get("pageSize")));//每页大小（默认值10）

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("tradeCode", "E00002");//请求码
            postMap.put("reqTime", date);//请求时间戳（格式YYYYMMDDHH24MISS
            postMap.put("deviceIp", "");//请求设备ip
            postMap.put("channelId", YSBehalfPayConfig.channelId);//请求渠道标识
            postMap.put("requestId", param.get("requestId"));//请求流水号（和渠道标识唯一标识一个请求），纯数字
            postMap.put("signStr", encode);//签名（data域数据为签名数据串）
            postMap.put("data", map);
            String data = JSON.toJSONString(postMap);
            System.out.println("发送数据：" + data);
            logger.info("易生代付额度调整明细查询-发送请求：" + data);
            String resp = HttpPost.ysPost(YSBehalfPayConfig.url, data);
            System.out.println("易生代付额度调整明细查询响应：" + resp);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("易生代付额度调整明细查询异常：" + e.getMessage());
            return null;
        }
    }


    public static Map<String, Object> RebateTransactions(String orderNum, String payamount) {
        Map<String, Object> respMap = new HashMap<>();
        try {
            lock.lock();
            logger.info("lock锁");
            String id = UUID.randomUUID().toString();
            respMap.put("id", id);
            logger.info("开始收集商户返利金");
            int countservicefee_rebate_configurationactivity = ysBehalfPay.bRebateDao.getCountservicefee_rebate_configurationactivity();
            logger.info("判断活动是否存在：" + countservicefee_rebate_configurationactivity);
            if (countservicefee_rebate_configurationactivity == 0) {
                respMap.put("amount", 0);
                return respMap;
            }
            String date = ysBehalfPay.bRebateDao.getDate("1");
            String smpayDate = ysBehalfPay.bRebateDao.smpayDate(orderNum);
            if (!StringUtils.isBlank(date)){
                Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
                Date smpayDateparse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(smpayDate);
                if (parse.getTime()>smpayDateparse.getTime()){
                    logger.info("活动未开始，不返利");
                    respMap.put("amount", 0);
                    return respMap;
                }

            }
            /**
             * 收集商户返利金额 并属于一二级行业的和城市区域商圈
             */
            //获取免支付手续费返利活动配置表ID
            String getservicefeeid = ysBehalfPay.bRebateDao.getservicefeeid("1");
            //获取该商户的一二级行业及城市区域
            Map<String, Object> shopType = ysBehalfPay.bRebateDao.getShopTypeSMTypeID(orderNum);
            //获取手续费
            String getrebatescale = ysBehalfPay.bRebateDao.getrebatescale("1");
            double getServiceFeeRebateAmounts = Double.parseDouble(getrebatescale);
            //该商户是否存在一二级行业
            int count = ysBehalfPay.bRebateDao.isCount(shopType.get("ShopType").toString(), shopType.get("SMTypeID").toString(), getservicefeeid);
            if (count > 0) {//存在
                List<Map<String, Object>> citymapList = ysBehalfPay.bRebateDao.getservicefee_rebate_configurationactivity_city("1");
                logger.info("获取城市区域商圈：" + citymapList.size());
                //收集商户信息
                Map<String, Object> mapList = new HashMap<>();
                for (Map<String, Object> t : citymapList) {
                    if (("0".equals(t.get("citycode").toString()) || shopType.get("CityCode").toString().equals(t.get("citycode").toString())) && ("0".equals(t.get("districtcode").toString()) || shopType.get("DistrictCode").toString().equals(t.get("districtcode").toString())) && ("0".equals(t.get("businesscircleid").toString()) || shopType.get("BusinessCircleID").toString().toUpperCase().equals(t.get("businesscircleid").toString()))) {
                        mapList.put("id", id);
                        mapList.put("shopid", shopType.get("shopid").toString());
                        mapList.put("TransactionTime", shopType.get("ztime").toString());
                        mapList.put("paymoney", payamount);
                        mapList.put("serviceFee", BigDecimalUtils.formatDouble(Double.valueOf(payamount) * getServiceFeeRebateAmounts));
                        mapList.put("serviceFeeRebate", BigDecimalUtils.formatDoublethree(Double.valueOf(payamount) * getServiceFeeRebateAmounts));
                        break;
                    }
                }

                if (Double.parseDouble(mapList.get("serviceFeeRebate").toString())>0&&0.03>=Double.parseDouble(mapList.get("serviceFeeRebate").toString())){//判断是否大于0.03 小于按0.03计算
                    mapList.put("serviceFeeRebate", "0.03");
                    logger.debug("默认0.03");
                }else {
                    logger.debug("不走默认");
                    mapList.put("serviceFeeRebate", BigDecimalUtils.formatDouble(Double.parseDouble(mapList.get("serviceFeeRebate").toString())));
                    logger.debug("不走默认："+mapList.get("serviceFeeRebate").toString());
                }

                Map<String, Object> rebatemonthup = ysBehalfPay.bRebateDao.getRebatemonthup("1");
                String rebateup = rebatemonthup.get("rebateup").toString();//活动预算上限
                int i1 = ysBehalfPay.bRebateDao.getservicefee_rebate_configurationactivityCount();//判断活动是否存在上限金额

                /**
                 * 插入服务费返利表
                 */
                if (mapList.size() > 0) {
                    if (ysBehalfPay.redisUtils.get("rebateMoneySum")==null){
                        ysBehalfPay.redisUtils.set("rebateMoneySum","0");
                    }
                    String shopMoneySum = ysBehalfPay.redisUtils.get("rebateMoneySum").toString();//获取所有商户返利金额
                    //TODO 此处改用redis
                    if (ysBehalfPay.redisUtils.get("rebatemoneyshopid:" + shopType.get("shopid").toString())==null){
                        ysBehalfPay.redisUtils.set("rebatemoneyshopid:" + shopType.get("shopid").toString(),"0");
                    }
                    String shopMoney = ysBehalfPay.redisUtils.get("rebatemoneyshopid:" + shopType.get("shopid").toString()).toString();//获取商户月流水上限总额

                    //查询商户一二级行业
                    Map<String, Object> getShopType = ysBehalfPay.bRebateDao.getShopType(shopType.get("shopid").toString());
                    //根据一二级行业获取月流水上限金额
                    Map<String, Object> getShopTypeList = ysBehalfPay.bRebateDao.getShopTypeList(rebatemonthup.get("id").toString(), getShopType.get("ShopType").toString(), getShopType.get("SMTypeID").toString());
                    logger.info("获取一二级行业及月流水上限金额数量：" + getShopTypeList.size());
                    String smpayupperlimit = getShopTypeList.get("rebatemonthup").toString();//月享受补贴流水封顶
                    logger.info("月享受补贴流水封顶：" + smpayupperlimit);

                    String getShopMoney = shopMoney.replace("\"","");//获取商户当月流水交易总金额
                    logger.info("商户Redis流水金额：" + getShopMoney);
                    String allMoney =shopMoneySum.replace("\"","");//获取所有商户返利金额
                    logger.info("返利金总预算金额：" + allMoney);
//                  String getShopMoney = ysBehalfPay.bRebateDao.getShopMoney(shopType.get("shopid").toString());//获取商户当月流水交易总金额
//                    String allMoney = ysBehalfPay.bRebateDao.getAllMoney();//获取所有商户返利金额
                    if (ysBehalfPay.bRebateDao.getServicefee_rebate_shop_exclude(shopType.get("shopid").toString()) <= 0 && Double.parseDouble(mapList.get("paymoney").toString()) > 0) {
                        if (i1 > 0) {//活动有上限
                            if (Double.parseDouble(allMoney) >= Double.parseDouble(rebateup)) {//达到活动上限金额
                                mapList.put("serviceFeeRebate", 0);
                                logger.info("返利金已达上限额度关闭活动");
                                ysBehalfPay.bRebateDao.updateActivitytype(rebatemonthup.get("id").toString());
                            } else {
                                if (Double.parseDouble(allMoney) + Double.parseDouble(mapList.get("serviceFeeRebate").toString()) >= Double.parseDouble(rebateup)) {//加上本次返利金额达到上限
                                    double difference = Double.parseDouble(rebateup) - Double.parseDouble(allMoney);//差额 还剩多少钱
                                    double temp = Double.valueOf(mapList.get("serviceFeeRebate").toString()) - difference;
                                    double t = Double.valueOf(mapList.get("serviceFeeRebate").toString()) - temp;
                                    mapList.put("serviceFeeRebate", BigDecimalUtils.formatDouble(t));

                                    logger.info("返利金已达上限额度关闭活动");
                                    ysBehalfPay.bRebateDao.updateActivitytype(rebatemonthup.get("id").toString());
                                    if (Double.parseDouble(getShopMoney) >= Double.parseDouble(smpayupperlimit)) {//商户已达当月上限金额
                                        mapList.put("serviceFeeRebate", 0);
                                    } else {
                                        if (Double.parseDouble(getShopMoney) + Double.parseDouble(mapList.get("paymoney").toString()) > Double.parseDouble(smpayupperlimit)) {//加上本次返利金商户金额达到月流水金额
                                            double money = Double.parseDouble(smpayupperlimit) - Double.parseDouble(getShopMoney);//得到差额
                                            double temp1 = Double.parseDouble(mapList.get("paymoney").toString()) - money;
                                            double t1 = Double.parseDouble(mapList.get("paymoney").toString()) - temp1;
                                            if (Double.parseDouble(mapList.get("serviceFeeRebate").toString())>=t1){
                                                mapList.put("serviceFeeRebate", BigDecimalUtils.formatDouble(t1 * getServiceFeeRebateAmounts));
                                            }
                                        }
                                    }
                                } else {//未达到预算上限
                                    if (Double.parseDouble(getShopMoney) >= Double.parseDouble(smpayupperlimit)) {//商户已达当月上限金额
                                        mapList.put("serviceFeeRebate", 0);
                                    } else {
                                        if (Double.parseDouble(getShopMoney) + Double.parseDouble(mapList.get("paymoney").toString()) > Double.parseDouble(smpayupperlimit)) {//加上本次返利金商户金额达到月流水金额
                                            double money = Double.parseDouble(smpayupperlimit) - Double.parseDouble(getShopMoney);//得到差额
                                            double temp = Double.parseDouble(mapList.get("paymoney").toString()) - money;
                                            double t = Double.parseDouble(mapList.get("paymoney").toString()) - temp;
                                            mapList.put("serviceFeeRebate", BigDecimalUtils.formatDouble(t * getServiceFeeRebateAmounts));
                                        }
                                    }
                                }

                            }
                        }else {
                            if (Double.parseDouble(getShopMoney) >= Double.parseDouble(smpayupperlimit)) {//商户已达当月上限金额
                                mapList.put("serviceFeeRebate", 0);
                            } else {
                                if (Double.parseDouble(getShopMoney) + Double.parseDouble(mapList.get("paymoney").toString()) > Double.parseDouble(smpayupperlimit)) {//加上本次返利金商户金额达到月流水金额
                                    double money = Double.parseDouble(smpayupperlimit) - Double.parseDouble(getShopMoney);//得到差额
                                    double temp1 = Double.parseDouble(mapList.get("paymoney").toString()) - money;
                                    double t1 = Double.parseDouble(mapList.get("paymoney").toString()) - temp1;
                                    mapList.put("serviceFeeRebate", BigDecimalUtils.formatDouble(t1 * getServiceFeeRebateAmounts));

                                }
                            }
                        }



                        int isOk = ysBehalfPay.bRebateDao.insertserviceFeerebateTransactions(mapList);//插入服务费返利表
                        if (isOk > 0) {
                            respMap.put("amount", mapList.get("serviceFeeRebate").toString());
                            double sum = Double.parseDouble(allMoney)+Double.parseDouble(mapList.get("serviceFeeRebate").toString());
                            ysBehalfPay.redisUtils.set("rebateMoneySum",String.valueOf(sum));
                            return respMap;
                        } else {
                            respMap.put("amount", 0);
                            return respMap;
                        }
                    }
                }
            }
            respMap.put("amount", 0);
            return respMap;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("商户充值返利金定时任务接口异常：" + e.getMessage());
            respMap.put("amount", 0);
            return respMap;
        }finally {
            lock.unlock();
            logger.info("释放锁...");
        }
    }
    public static String getIsTrue(String orderNum, String payamount) {
        try {
            Map<String, Object> mapList = new HashMap<>();
            int countservicefee_rebate_configurationactivity = ysBehalfPay.bRebateDao.getCountservicefee_rebate_configurationactivity();
            logger.info("判断活动是否存在：" + countservicefee_rebate_configurationactivity);
            if (countservicefee_rebate_configurationactivity == 0) {
                return "";
            }
            /**
             * 收集商户返利金额 并属于一二级行业的和城市区域商圈
             */
            //获取免支付手续费返利活动配置表ID
            String getservicefeeid = ysBehalfPay.bRebateDao.getservicefeeid("1");
            //获取该商户的一二级行业及城市区域
            Map<String, Object> shopType = ysBehalfPay.bRebateDao.getShopTypeSMTypeID(orderNum);
            //获取手续费
            String getrebatescale = ysBehalfPay.bRebateDao.getrebatescale("1");
            double getServiceFeeRebateAmounts = Double.parseDouble(getrebatescale);
            //该商户是否存在一二级行业
            int count = ysBehalfPay.bRebateDao.isCount(shopType.get("ShopType").toString(), shopType.get("SMTypeID").toString(), getservicefeeid);
            if (count > 0) {//存在
                List<Map<String, Object>> citymapList = ysBehalfPay.bRebateDao.getservicefee_rebate_configurationactivity_city("1");
                logger.info("获取城市区域商圈：" + citymapList.size());
                //收集商户信息
                for (Map<String, Object> t : citymapList) {
                    if (("0".equals(t.get("citycode").toString()) || shopType.get("CityCode").toString().equals(t.get("citycode").toString())) && ("0".equals(t.get("districtcode").toString()) || shopType.get("DistrictCode").toString().equals(t.get("districtcode").toString())) && ("0".equals(t.get("businesscircleid").toString()) || shopType.get("BusinessCircleID").toString().toUpperCase().equals(t.get("businesscircleid").toString()))) {
                        mapList.put("id", UUID.randomUUID().toString());
                        mapList.put("shopid", shopType.get("shopid").toString());
                        mapList.put("TransactionTime", shopType.get("ztime").toString());
                        mapList.put("paymoney", payamount);
                        mapList.put("serviceFee", BigDecimalUtils.formatDouble(Double.valueOf(payamount) * getServiceFeeRebateAmounts));
                        mapList.put("serviceFeeRebate", BigDecimalUtils.formatDoublethree(Double.valueOf(payamount) * getServiceFeeRebateAmounts));
                        break;
                    }
                }
            }
            if (mapList.size()>0){
                if (ysBehalfPay.redisUtils.get("rebatemoneyshopid:" + shopType.get("shopid").toString())==null){
                    ysBehalfPay.redisUtils.set("rebatemoneyshopid:" + shopType.get("shopid").toString(),"0");
                }
                return shopType.get("shopid").toString();
            }else {
                return "";
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("判断商户是否可以累加流水方法异常："+e.getMessage());
            return "";
        }
    }

}
