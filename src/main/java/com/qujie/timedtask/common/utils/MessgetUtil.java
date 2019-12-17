package com.qujie.timedtask.common.utils;


public class MessgetUtil {
    /**
     * 发送短信验证码
     * @param tel
     * @param content
     * @return
     */
    public boolean SendMessage(String tel,String content){
        boolean result = false;
        SmsClient smsClient = new SmsClient();
        String custCode = "300607";							 //[必填] 用户账号
        String password = "68AKQCLF1B";						 //[必填] 账号密码
        String serviceBaseUrl = "http://123.58.255.70:8860";
        result = smsClient.send(custCode,password,serviceBaseUrl,tel,content);
        return result;
    }
}
