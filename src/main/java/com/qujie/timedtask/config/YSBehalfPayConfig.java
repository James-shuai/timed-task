package com.qujie.timedtask.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 易生代付配置
 * @author ys
 * @date 2019/10/17 13:05
 */
@Configuration
@PropertySource("classpath:sdk_ysbehalf_pay.properties")
public class YSBehalfPayConfig {

   public static String url;
   public static String channelId;
   public static String otherCode;
   public static String accType;
   public static String cardKind;
//   public static String amtType;
   public static String payFlag;
   public static String qztransChennl;
   public static String backUrl;
   public static String bankCode;
   public static String bankName;
   public static String privateKey;
   public static String phone;

    public  String getUrl() {
        return url;
    }

    @Value("${ysbehalf.url}")
    public  void setUrl(String url) {
        YSBehalfPayConfig.url = url;
    }


    public  String getChannelId() {
        return channelId;
    }
    @Value("${ysbehalf.channelId}")
    public  void setChannelId(String channelId) {
        YSBehalfPayConfig.channelId = channelId;
    }

    public  String getOtherCode() {
        return otherCode;
    }
    @Value("${ysbehalf.otherCode}")
    public  void setOtherCode(String otherCode) {
        YSBehalfPayConfig.otherCode = otherCode;
    }

    public  String getAccType() {
        return accType;
    }
    @Value("${ysbehalf.accType}")
    public  void setAccType(String accType) {
        YSBehalfPayConfig.accType = accType;
    }

    public  String getCardKind() {
        return cardKind;
    }
    @Value("${ysbehalf.cardKind}")
    public  void setCardKind(String cardKind) {
        YSBehalfPayConfig.cardKind = cardKind;
    }

//    public  String getAmtType() {
//        return amtType;
//    }
//    @Value("${ysbehalf.amtType}")
//    public  void setAmtType(String amtType) {
//        YSBehalfPayConfig.amtType = amtType;
//    }

    public  String getPayFlag() {
        return payFlag;
    }
    @Value("${ysbehalf.payFlag}")
    public  void setPayFlag(String payFlag) {
        YSBehalfPayConfig.payFlag = payFlag;
    }

    public  String getQztransChennl() {
        return qztransChennl;
    }
    @Value("${ysbehalf.qztransChennl}")
    public  void setQztransChennl(String qztransChennl) {
        YSBehalfPayConfig.qztransChennl = qztransChennl;
    }

    public  String getBackUrl() {
        return backUrl;
    }
    @Value("${ysbehalf.backUrl}")
    public  void setBackUrl(String backUrl) {
        YSBehalfPayConfig.backUrl = backUrl;
    }


    public static String getBankCode() {
        return bankCode;
    }
    @Value("${ysbehalf.bankCode}")
    public  void setBankCode(String bankCode) {
        YSBehalfPayConfig.bankCode = bankCode;
    }

    public static String getBankName() {
        return bankName;
    }
    @Value("${ysbehalf.bankName}")
    public  void setBankName(String bankName) {
        YSBehalfPayConfig.bankName = bankName;
    }

    public  String getPrivateKey() {
        return privateKey;
    }
    @Value("${ysbehalf.privateKey}")
    public  void setPrivateKey(String privateKey) {
        YSBehalfPayConfig.privateKey = privateKey;
    }

    public String getPhone() {
        return phone;
    }
    @Value("${ysbehalf.phone}")
    public void setPhone(String phone) {
        YSBehalfPayConfig.phone = phone;
    }
}
