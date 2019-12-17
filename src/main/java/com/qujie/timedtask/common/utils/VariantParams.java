package com.qujie.timedtask.common.utils;

/**
 * 变量短信对象实体
 */
public class VariantParams {
	private String mobile;		//手机号码
	private String[] vars;		//变量内容 

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String[] getVars() {
		return vars;
	}

	public void setVars(String[] vars) {
		this.vars = vars;
	}
}