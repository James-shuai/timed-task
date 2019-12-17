package com.qujie.timedtask.async;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qujie.timedtask.business.dao.BRebateDao;
import com.qujie.timedtask.business.dao.BehalfPayDao;
import com.qujie.timedtask.business.task.YSBehalfPay;
import com.qujie.timedtask.common.utils.HttpPost;
import com.qujie.timedtask.common.utils.RedisUtils;
import com.qujie.timedtask.config.Config;
import com.qujie.timedtask.config.YSBehalfPayConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 异步执行
 *
 * @author ys
 * @date 2019/10/18 17:42
 */
@Component
public class AsyncTask {

    /**
     * 注入 Dao 方式
     * 例子：
     *
     * @Autowired public BehalfPayDao behalfPayDao;
     * public  static AsyncTask asyncTask;
     * @PostConstruct public void init(){
     * asyncTask=this;
     * asyncTask.behalfPayDao=this.behalfPayDao;
     * }
     * 调用方式：asyncTask.behalfPayDao.方法名
     */


    public String appKey = "1ce5afaeae06e62d6329def2";//必填，例如466f7032ac604e02fb7bda89
    public String masterSecret = "70816256f699227c56b44477";//必填，每个应用都对应一个masterSecret

    @Autowired
    public BehalfPayDao behalfPayDao;
    @Autowired
    public BRebateDao bRebateDao;
    public static AsyncTask asyncTask;

    @PostConstruct
    public void init() {
        asyncTask = this;
        asyncTask.behalfPayDao = this.behalfPayDao;
        asyncTask.bRebateDao = this.bRebateDao;
    }

    @Autowired
    private RedisUtils redisUtil;

    private static Logger logger = LoggerFactory.getLogger(AsyncTask.class);
    @Autowired
    private Config config;

    /**
     * 易生通过查询方式更改数据库状态
     *
     * @param resp       代付请求返回报文
     * @param payDate    支付日期
     * @param artificial 代付类型 0：订单代付，1：人工手动代付
     */
    @Async
    public void callback(String resp, String payDate, String artificial, double pay,Map<String, Object> param) {
        try {
            logger.info("易生代付查询获取参数：" + resp + ";" + payDate);
            JSONObject jsonObject = JSON.parseObject(resp);
            Map<String, String> respMap = new HashMap<>();
            respMap.put("oldRequestId", jsonObject.get("requestId").toString());//原交易流水号
            respMap.put("payDate", payDate);//原付款日期
            /**
             * 如果15秒还是处理中 则视为交易失败！
             * 可以适当增大时间
             */
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("payment", pay / 100);
            double rebateMoney = 0;
            double payamount = 0;
            if (param.size()>0){
                rebateMoney=Double.parseDouble(param.get("amount").toString());//返利金金额
                payamount=Double.parseDouble(param.get("payamount").toString());//交易流水
            }
            String shopid ="";
            for (int i = 0; i < 5; i++) {
                respMap.put("requestId", createOrderId());//新请求流水
                String query = YSBehalfPay.query(respMap).toString();//得到查询结果
                JSONObject jsonObject1 = JSON.parseObject(query);//转JSON对象
                JSONObject jsondata = (JSONObject) jsonObject1.get("data");//获取data对象
                recordMap.put("id", UUID.randomUUID().toString());
                String substring = jsondata.get("oldRequestId").toString();
                recordMap.put("orderId", substring);
                recordMap.put("addtime", new Date());
                if ("000000".equals(jsonObject1.get("acceRetCode").toString())) {
                    if ("000000".equals(jsondata.get("retCode").toString())) {//交易成功 跳出循环 并更改数据库状态
                        recordMap.put("msg", JSON.toJSONString(jsonObject1));
                        recordMap.put("success", "1");
                        recordMap.put("description", jsondata.get("retMsg").toString());//描述
                        //TODO 业务逻辑
                        //订单代付
                        if (Integer.parseInt(artificial) == 0) {
                            if (rebateMoney<=0){
                                asyncTask.behalfPayDao.updateTraderecordIspayYsbehalf(substring, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                            }else {
                                if (param.containsKey("id")){
                                    shopid = getShopMoney(param.get("id").toString());
                                    asyncTask.behalfPayDao.updateTraderecordIspayYsbehalfFL(substring, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                                    asyncTask.bRebateDao.updateserviceFeerebateTransactions(param.get("id").toString(),substring);
                                }else {
                                    asyncTask.behalfPayDao.updateTraderecordIspayYsbehalf(substring, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                                }
                            }
                            recordMap.put("type", "0");//订单代付
                        } else {
                            recordMap.put("type", "1");//人工代付
                        }
                        break;
                    }
                    if ("XXXXXX".equals(jsondata.get("retCode").toString())) {//交易失败 跳出循环
                        if (!StringUtils.isBlank(shopid)){
                            String getShopMoney = redisUtil.get("rebatemoneyshopid:" + shopid).toString().replace("\"","");
                            String rebateMoneySum = redisUtil.get("rebateMoneySum").toString().replace("\"","");
                            double shop = Double.parseDouble(getShopMoney);
                            double sum = Double.parseDouble(rebateMoneySum);
                            redisUtil.set("rebatemoneyshopid:" + shopid,String.valueOf(shop-payamount));
                            redisUtil.set("rebateMoneySum",String.valueOf(sum-rebateMoney));
                        }

                        if (Integer.parseInt(artificial) == 0) {
                            recordMap.put("type", "0");//订单代付
                        } else {
                            recordMap.put("type", "1");//人工代付
                        }
                        recordMap.put("msg", JSON.toJSONString(jsonObject1));
                        recordMap.put("success", "0");
                        recordMap.put("description", jsondata.get("retMsg").toString());//描述
                        /**发送短信*/
                        String centon = "订单号：" + substring + "代付失败，原因：" + jsondata.get("retMsg").toString()+"，返利金没有累加。";
                        sendMsg(centon);
                        break;
                    }

                }
                recordMap.put("description", jsondata.get("retMsg").toString());//描述
                if (Integer.parseInt(artificial) == 0) {
                    recordMap.put("type", "0");//订单代付
                } else {
                    recordMap.put("type", "1");//人工代付
                }
                recordMap.put("msg", JSON.toJSONString(jsonObject1));
                recordMap.put("success", "2");
                if (i == 0) {
                    Thread.sleep(2 * 1000);
                }
                if (i == 1) {
                    Thread.sleep(4 * 1000);
                }
                if (i == 2) {
                    Thread.sleep(8 * 1000);
                }
                if (i == 3) {
                    Thread.sleep(16 * 1000);
                }
                if (i == 4) {
                    if (!StringUtils.isBlank(shopid)){
                        String getShopMoney = redisUtil.get("rebatemoneyshopid:" + shopid).toString().replace("\"","");
                        String rebateMoneySum = redisUtil.get("rebateMoneySum").toString().replace("\"","");
                        double shop = Double.parseDouble(getShopMoney);
                        double sum = Double.parseDouble(rebateMoneySum);
                        redisUtil.set("rebatemoneyshopid:" + shopid,String.valueOf(shop-payamount));
                        redisUtil.set("rebateMoneySum",String.valueOf(sum-rebateMoney));
                    }
                    /**发送短信*/
                    String centon = "订单号：" + substring + "代付异常，原因：" + jsondata.get("retMsg").toString()+"，返利金没有累加。";
                    sendMsg(centon);
                }
            }
            int i = asyncTask.behalfPayDao.saveYSBehalfPayRecord(recordMap);//插入代付记录
            logger.info("易生代付查询插入记录状态：" + i);

        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("易生代付查询异常：" + e.getMessage());
        }

    }



    public String getShopMoney(String serviceId){
        logger.info("开始商户返利");
        Map<String, Object> shopServiceFee = asyncTask.bRebateDao.getShopServiceFee(serviceId);
        Map<String, Object> insertMap = new HashMap<>();
        String id = UUID.randomUUID().toString();
        insertMap.put("id", id);
        insertMap.put("shopid", shopServiceFee.get("shopid").toString());
        insertMap.put("money", shopServiceFee.get("serviceFeeRebate").toString());
        insertMap.put("rebateType", 1);
        insertMap.put("rebateTypeDescribe", "服务费返利");
        insertMap.put("addtime", new Date());
//        int i = asyncTask.bRebateDao.insertRebateTransactions(insertMap);//存入返利金记录表
        asyncTask.bRebateDao.updateServiceFeerebateTransactions(id, shopServiceFee.get("shopid").toString());//修改服务费返利表
//        int count = asyncTask.bRebateDao.countRebateTransactionsTotalSum(shopServiceFee.get("shopid").toString());
//        if (count > 0) {
//            asyncTask.bRebateDao.updateRebateTransactionsTotalSum(shopServiceFee.get("shopid").toString(), shopServiceFee.get("serviceFeeRebate").toString());
//        } else {
//            asyncTask.bRebateDao.insertRebateTransactionsTotalSum(shopServiceFee.get("shopid").toString(), shopServiceFee.get("serviceFeeRebate").toString());
//        }
//        if (i > 0&&Double.parseDouble(insertMap.get("money").toString())>0) {
//            JPushClient jpushClient = new JPushClient(masterSecret, appKey);
//            List<String> aliasList = new ArrayList<>();
//            aliasList.add(insertMap.get("shopid").toString());
//
//            String fromater = DateFormatUtils.format(DateUtil.getDateByAddDays(-1), "yyyy年MM月dd日");
//            String msg = "您的【商户贴息返利一"+fromater+"】返利金,已存入您的返利金账户。";
//            int result = JPushUtil.sendToAliasList(jpushClient, aliasList, msg, "type", "rebate", "");
//            logger.info("消息推送结果："+result);
//        }
        return shopServiceFee.get("shopid").toString();
    }


    /**
     * 返利金充值代付
     *
     * @param resp
     * @param payDate
     */
    @Async
    public void BRebateCallback(String resp, String payDate, String stlmAmt) {
        try {
            logger.info("返利金充值代付查询获取参数：" + resp + ";" + payDate);
            JSONObject jsonObject = JSON.parseObject(resp);
            Map<String, String> respMap = new HashMap<>();
            respMap.put("oldRequestId", jsonObject.get("requestId").toString());//原交易流水号
            respMap.put("payDate", payDate);//原付款日期
            /**
             * 如果15秒还是处理中 则视为交易失败！
             * 可以适当增大时间
             */
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("payment", Double.parseDouble(stlmAmt) / 100);
            recordMap.put("type", "2");//返利金代付
            for (int i = 0; i < 5; i++) {
                respMap.put("requestId", createOrderId());//新请求流水
                String query = YSBehalfPay.query(respMap).toString();//得到查询结果
                JSONObject jsonObject1 = JSON.parseObject(query);//转JSON对象
                JSONObject jsondata = (JSONObject) jsonObject1.get("data");//获取data对象
                recordMap.put("id", UUID.randomUUID().toString());
                String transactionNum = jsondata.get("oldRequestId").toString();
                recordMap.put("orderId", transactionNum);
                recordMap.put("addtime", new Date());
                if ("000000".equals(jsonObject1.get("acceRetCode").toString())) {
                    if ("000000".equals(jsondata.get("retCode").toString())) {//交易成功 跳出循环 并更改数据库状态
                        System.out.println("执行易生充值代付异步调用");
                        recordMap.put("msg", JSON.toJSONString(jsonObject1));
                        recordMap.put("success", "1");
                        recordMap.put("description", jsondata.get("retMsg").toString());//描述
                        //TODO 业务逻辑
                        for (int j = 0; j < 5; j++) {
                            int count = asyncTask.bRebateDao.selectrebateTransactions(transactionNum);
                            if (count > 0) {//如果订单存在修改状态并跳出循环
                                asyncTask.bRebateDao.updaterebateTransactions(transactionNum);
                                break;
                            }
                            Thread.sleep(3000);
                        }
                        break;
                    }
                    if ("XXXXXX".equals(jsondata.get("retCode").toString())) {//交易失败 跳出循环
                        recordMap.put("msg", JSON.toJSONString(jsonObject1));
                        recordMap.put("success", "0");
                        recordMap.put("description", jsondata.get("retMsg").toString());//描述
                        String centon = "返利金代付订单号：" + transactionNum + "代付失败，原因：" + jsondata.get("retMsg").toString();
                        sendMsg(centon);
                        break;
                    }

                }
                recordMap.put("msg", JSON.toJSONString(jsonObject1));
                recordMap.put("success", "2");
                recordMap.put("description", jsondata.get("retMsg").toString());//描述
                if (i == 0) {
                    Thread.sleep(2 * 1000);
                }
                if (i == 1) {
                    Thread.sleep(4 * 1000);
                }
                if (i == 2) {
                    Thread.sleep(8 * 1000);
                }
                if (i == 3) {
                    Thread.sleep(16 * 1000);
                }
                if (i == 4) {
                    /**发送短信*/
                    String centon = "返利金代付订单号：" + transactionNum + "代付异常，原因：" + jsondata.get("retMsg").toString();
                    sendMsg(centon);
                }
            }
            int i = asyncTask.behalfPayDao.saveYSBehalfPayRecord(recordMap);//插入代付记录
            logger.info("返利金充值代付查询插入记录状态：" + i);

        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("返利金充值代付查询异常：" + e.getMessage());
        }

    }


    /**
     * 生成唯一订单号
     */
    public String createOrderId() {
        int machineId = 9;//最大支持1-9个集群机器部署  
        int hashCodeV = UUID.randomUUID().toString().hashCode();
        if (hashCodeV < 0) {//有可能是负数  
            hashCodeV = -hashCodeV;
        }
        return machineId + String.format("%015d", hashCodeV);
    }


    public void sendMsg(String content) {
        String[] phones = YSBehalfPayConfig.phone.split(",");
        for (int i = 0; i < phones.length; i++) {
            Map<String, Object> sendmap = new HashMap<String, Object>();
            sendmap.put("mobiles", phones[i]);
            sendmap.put("content", content);
            logger.info("发送短信url:" + config.getMessageurl());
            String smResult = HttpPost.message(config.getMessageurl(), new JSONObject(sendmap).toString());
            logger.info("验证码发送状态：" + smResult);
            //TODO 生产环境注释
//            MessgetUtil messgetUtil = new MessgetUtil();
//            boolean b = messgetUtil.SendMessage(phones[i], content);
//            logger.info("验证码发送状态：" + b);
        }
    }


}
