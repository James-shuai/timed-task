package com.qujie.timedtask.business.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qujie.timedtask.business.dao.BRebateDao;
import com.qujie.timedtask.business.dao.TestMapper;
import com.qujie.timedtask.business.task.YSBehalfPay;
import com.qujie.timedtask.common.utils.BigDecimalUtils;
import com.qujie.timedtask.common.utils.RedisUtils;
import com.qujie.timedtask.common.utils.ReturnResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ys
 * @date 2019/10/21 11:18
 */
@RestController
@RequestMapping(value = "/api/behalfPay")
public class BehalfPayController {
    public  static Lock lock =  new ReentrantLock();

    private Logger logger = LoggerFactory.getLogger(BehalfPayController.class);

    @Autowired
    private BRebateDao bRebateDao;

    @Autowired
    private TestMapper testMapper;
    @Autowired
    private RedisUtils redisUtils;

    @RequestMapping(value = "/Pay",method = RequestMethod.POST)
    public Object Pay(@RequestBody String request){
        try {
            logger.info("人工手动代付请求传入参数："+request);
            JSONObject jsonObject = JSON.parseObject(request);
            String money = jsonObject.get("money").toString();
            String requestId = jsonObject.get("requestId").toString();
            String cardnum = jsonObject.get("cardnum").toString();
            String bankacctname = jsonObject.get("bankacctname").toString();
            String cardKind = jsonObject.get("cardKind").toString();
            String accType = jsonObject.get("accType").toString();
            String bankCode = jsonObject.get("bankCode").toString();//行号
            String artificial = jsonObject.get("artificial").toString();//行号
            String subsidyPayment = jsonObject.get("subsidyPayment").toString();//行号

            String orderMoney = bRebateDao.getOrderMoney(requestId);

            Map<String,String> payMap = new HashMap<>();
            payMap.put("accountNo",cardnum);//收款账号
            payMap.put("accName",bankacctname);//收款账户
            payMap.put("stlmAmt", money);//代付款 分！
            payMap.put("bankCode", bankCode);//
            payMap.put("subsidyPayment", subsidyPayment);//代付款 分！
            payMap.put("requestId", requestId);//请求流水 填写订单号即可
            payMap.put("amtType","0");//交易方式（0：授信余额；1：资金余额/结算资金）
            payMap.put("cardKind",cardKind);//卡种（D借记卡，C信用卡,H合一卡，X未知）
            payMap.put("accType",accType);//账户类型: 00 -- 个人帐户，10 -- 对公账户, FF -- 虚拟帐户
            payMap.put("artificial",artificial);
            if (!StringUtils.isBlank(orderMoney)){
                payMap.put("payamount",  BigDecimalUtils.formatDouble(Double.parseDouble(orderMoney)*100));
            }
            logger.info("人工手动代付请求参数："+payMap);
            boolean resp = YSBehalfPay.behalfPay(payMap);
            logger.info("人工手动代付响应结果："+resp);
            if (resp){
                return ReturnResult.successResult("操作成功",resp);
            }
            return ReturnResult.failResult("操作失败",resp);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("打款异常："+e.getMessage());
            return ReturnResult.failResult("返回成功","打款异常");
        }
    }

    /**
     * 查询订单代付状态
     * @param request
     * @return
     */
    @RequestMapping(value = "/query",method = RequestMethod.POST)
    public Object query(@RequestBody String request){
        try {
            logger.info("查询订单代付状态-传入参数："+request);
            JSONObject jsonObject = JSON.parseObject(request);
            String ordernum = jsonObject.get("ordernum").toString();
            String date = bRebateDao.queryOrder(ordernum);
            Map<String,String> sendMap = new HashMap<>();
            sendMap.put("oldRequestId",ordernum);
            sendMap.put("payDate",date);
            sendMap.put("requestId", String.valueOf((int) ((Math.random() * 9 + 1) * 10000000)));//新请求流水
            String query = YSBehalfPay.query(sendMap).toString();//得到查询结果
            JSONObject jsonObject1 = JSON.parseObject(query);//转JSON对象
            JSONObject jsondata = (JSONObject) jsonObject1.get("data");//获取data对象
            if ("000000".equals(jsonObject1.get("acceRetCode").toString())) {
                if ("XXXXXX".equals(jsondata.get("retCode").toString())) {//交易成功 跳出循环 并更改数据库状态
                    return ReturnResult.successResult("可以重新代付",jsondata.get("retMsg").toString());
                }else{
                    return ReturnResult.failResult("禁止重新代付",jsondata.get("retMsg").toString());
                }
            }
            return ReturnResult.failResult("查询通过失败，禁止重新代付",jsondata.get("retMsg").toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询订单代付状态接口异常："+e.getMessage());
            return ReturnResult.failResult("禁止重新代付",e.getMessage());
        }
    }

    /**
     * 查询备付金余额
     * @return
     */
    @RequestMapping(value = "/queryBalance",method = RequestMethod.POST)
    public Object queryBalance(){
        try {
            logger.info("查询备付金余额");
            String resp = YSBehalfPay.queryQuota(String.valueOf((int) ((Math.random() * 9 + 1) * 10000000)));
            JSONObject jsonObject1 = JSON.parseObject(resp);//转JSON对象
            JSONObject jsondata = (JSONObject) jsonObject1.get("data");//获取data对象
            return ReturnResult.successResult("查询成功",jsondata.get("stlm_newamt").toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询备付金余额接口异常："+e.getMessage());
            return ReturnResult.successResult("查询异常",e.getMessage());
        }
    }

    /**
     * 增加商户redis交易流水与返利金上限
     * @param request
     * @return
     */
    public Object addShopTransactionFlow(@RequestBody String request){
        try {
            JSONObject jsonObject = JSON.parseObject(request);
            String ordernum = jsonObject.get("ordernum").toString();
            Map<String, Object> map = bRebateDao.selectShopid(ordernum);
            if (map==null&&map.size()==0){
                return ReturnResult.failResult("失败","该订单未代付成功");
            }
            String shopid = map.get("shopid").toString();
            if (redisUtils.get("rebatemoneyshopid:"+shopid)==null){
                return ReturnResult.failResult("失败","该商户没有交易流水");
            }
            String money = redisUtils.get("rebatemoneyshopid:" + shopid).toString();
            double v = Double.parseDouble(money);
            double money1 = Double.parseDouble(map.get("money").toString());
            String s = BigDecimalUtils.formatDouble(v + money1);
            redisUtils.set("rebatemoneyshopid:"+shopid,s);
            return ReturnResult.successResult("成功","处理成功");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ReturnResult.failResult("失败","该操作异常");
        }
    }


    @RequestMapping("/q")
    public void sss(){
        String ordernumF="";
        String ordernumW="";
        List<Map<String, Object>> selectF = testMapper.Fselect();
        List<Map<String, Object>> selectW = testMapper.Wselect();
        for (Map<String, Object> t:selectF) {
            String money = testMapper.selectYS(t.get("ordernum").toString());
            double ss = Double.parseDouble(t.get("payamount").toString())+Double.parseDouble(t.get("newuserreduceamount").toString())+Double.parseDouble(t.get("vipmemberreduceamount").toString());
            if (Double.parseDouble(money)!=ss){
                ordernumF+=t.get("ordernum").toString()+",";
            }
        }
        for (Map<String, Object> t:selectW) {
            String money = testMapper.selectYS(t.get("ordernum").toString());
            double ss = Double.parseDouble(t.get("payamount").toString())+Double.parseDouble(t.get("newuserreduceamount").toString())+Double.parseDouble(t.get("vipmemberreduceamount").toString())-Double.parseDouble(t.get("payfee").toString());
            if (Double.parseDouble(money)!=ss){
                ordernumW+=t.get("ordernum").toString()+",";
            }
        }
        System.out.println("返利对比错误："+ordernumF);
        System.out.println("没有返利对比错误："+ordernumW);

    }




}
