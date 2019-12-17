package com.qujie.timedtask.common.utils;

import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReturnResult {

    private String Code; //状态码

    private String message; //返回消息  特指错误消息

    private Object data;

    private String returnTime;

    public String getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ReturnResult(String code, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSSS");
        Code = code;
        this.message = message;
        this.returnTime = sdf.format(new Date());
        this.data = "[]";
    }

    public ReturnResult(String code, String message, Object obj) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSSS");
        Code = code;
        this.message = message;
        this.returnTime = sdf.format(new Date());
        this.data = obj!=null?obj:"[]";
    }

    public ReturnResult(){

    }

    public static String successResult(String message){
        ReturnResult returnResult = new ReturnResult("True",message);
        return JSONObject.toJSON(returnResult).toString();
    }
    public static String successResult(String message,Object obj){
        ReturnResult returnResult = new ReturnResult("True",message,obj);
        return JSONObject.toJSON(returnResult).toString();
    }

    public static String failResult(String message,Object obj){
        ReturnResult returnResult = new ReturnResult("False",message,obj);
        return JSONObject.toJSON(returnResult).toString();
    }
}
