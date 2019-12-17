package com.qujie.timedtask.common.utils;

import java.util.List;

public class VariantSmsReq {
	private String uid = "";                  //业务标识[选填]，由贵司自定义32为数字透传至我司
	private String content = "";              //[必填]短信模板。其中的变量用“${vari}”来替代,i代表变量的序号（从0开始增长，每次增加1）。例如：“${var0}用户您好，今天${var1}的天气，晴，温度${var2}度，事宜外出。”，该短信中具有两个变量参数。编码为UTF-8格式。
	private List<VariantParams> params;       //[必填]同时发送给多个号码时,号码之间用英文半角逗号分隔(,),，其中变量短信每一组为json格式，如: [{"mobile"："手机号码","var":["福州","30"]},{"mobile"："手机号码","var":["厦门","32"]}]	每组变量中第一个变量固定为目标手机号码，对应短信模板中的参数，var为变量个数要与内容中的(其中i变量系好，从1开始)个数匹配，以此类推。（请注意：变量中不要包含有逗号和竖线，否则发送的格式解析会有问题）
	private String cust_code = "";            //用户账号[必填]
	private String sp_code = "";              //长号码[选填]
	private String need_report = "";          //状态报告需求与否[选填]，是 yes 否 no 默认yes
	private String sign = "";                 //数字签名[必填]，签名内容根据 “短信内容+客户密码”进行MD5编码后获得。
	private String msgFmt = "8";              //信息格式[选填]，0：ASCII串；3：短信写卡操作；4：二进制信息；8：UCS2编码；
	
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

	public List<VariantParams> getParams() {
		return params;
	}

	public void setParams(List<VariantParams> params) {
		this.params = params;
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
