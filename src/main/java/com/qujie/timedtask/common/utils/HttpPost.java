package com.qujie.timedtask.common.utils;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HttpPost {

    private Logger logger = LoggerFactory.getLogger(HttpPost.class);

    public static String post(String url,String data){
        try {
            String postURL = url;
            PostMethod postMethod = null;
            postMethod = new PostMethod(postURL) ;
            postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8") ;
            //参数设置，需要注意的就是里边不能传NULL，要传空字符串

            postMethod.setRequestBody(data);

            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
            int response = httpClient.executeMethod(postMethod); // 执行POST方法
            String result = postMethod.getResponseBodyAsString() ;

            return result;
        } catch (Exception e) {
            //logger.info("请求异常"+e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String message(String url,String data){
        try {
            String postURL = url;
            PostMethod postMethod = null;
            postMethod = new PostMethod(postURL) ;
            postMethod.setRequestHeader("Content-Type", "application/json;charset=utf-8") ;
            //参数设置，需要注意的就是里边不能传NULL，要传空字符串

            postMethod.setRequestBody(data);

            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
            int response = httpClient.executeMethod(postMethod); // 执行POST方法
            String result = postMethod.getResponseBodyAsString() ;

            return result;
        } catch (Exception e) {
            //logger.info("请求异常"+e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }


    public static String postSound(String url, String data, Map<String,Object> param){
        try {
            String postURL = url;
            PostMethod postMethod = null;
            postMethod = new PostMethod(postURL) ;
            postMethod.addRequestHeader("signature",param.get("sign").toString());
            postMethod.addRequestHeader("reqMsgId",param.get("reqMsgId").toString());
            postMethod.addRequestHeader("appId",param.get("appId").toString());
            postMethod.addRequestHeader("reqTime",param.get("reqTime").toString());
//            postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8") ;
            //参数设置，需要注意的就是里边不能传NULL，要传空字符串

            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            postMethod.setRequestBody(data);

            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
            int response = httpClient.executeMethod(postMethod); // 执行POST方法
            String result = postMethod.getResponseBodyAsString() ;

            return result;
        } catch (Exception e) {
            //logger.info("请求异常"+e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 易生请求
     * @param url
     * @param data
     * @return
     */
    public static String ysPost(String url,String data){
        try {
            String postURL = url;
            PostMethod postMethod = null;
            postMethod = new PostMethod(postURL) ;
//            postMethod.setRequestHeader("Connection", "Keep-Alive") ;
//            postMethod.setRequestHeader("Accept", "application/json") ;
//            postMethod.setRequestHeader("Content-Length", "597");
//            postMethod.setRequestHeader("User-Agent", "Apache-HttpClient/4.5.8(Java/1.8.0_40)") ;
//            postMethod.setRequestHeader("Accept-Encoding", "gzip,deflate") ;
            postMethod.setRequestHeader("Content-Type", "application/json;charset=GBK") ;
            //参数设置，需要注意的就是里边不能传NULL，要传空字符串

            postMethod.setRequestBody(data);

            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
            int response = httpClient.executeMethod(postMethod); // 执行POST方法
            String result = postMethod.getResponseBodyAsString() ;

            return result;
        } catch (Exception e) {
            //logger.info("请求异常"+e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
