package com.qujie.timedtask.common.utils;

/**
 * 发送短信对象实体 
 */
public class SmsReq {
	
	private String uid = ""; 			//业务标识[选填]，由贵司自定义32为数字透传至我司
	private String content = ""; 		//短信内容[必填]
	private String destMobiles = ""; 	//接收号码[必填]，同时发送给多个号码时,号码之间用英文半角逗号分隔(,)
	private String cust_code = ""; 		//用户账号[必填]
	private String sp_code = ""; 		//长号码[选填]
	private String need_report = ""; 	//状态报告需求与否[选填]，是 yes 否 no 默认yes
	private String sign = ""; 			//数字签名[必填]，签名内容根据 “短信内容+客户密码”进行MD5编码后获得。
	private String msgFmt = "8"; 		//信息格式[选填]，0：ASCII串；3：短信写卡操作；4：二进制信息；8：UCS2编码；
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getDestMobiles() {
		return destMobiles;
	}
	public void setDestMobiles(String destMobiles) {
		this.destMobiles = destMobiles;
	}
	public String getCust_code() {
		return cust_code;
	}
	public void setCust_code(String cust_code) {
		this.cust_code = cust_code;
	}
	public String getSp_code() {
		return sp_code;
	}
	public void setSp_code(String sp_code) {
		this.sp_code = sp_code;
	}
	public String getNeed_report() {
		return need_report;
	}
	public void setNeed_report(String need_report) {
		this.need_report = need_report;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getMsgFmt() {
		return msgFmt;
	}
	public void setMsgFmt(String msgFmt) {
		this.msgFmt = msgFmt;
	}
	
}
