package com.qujie.timedtask.common.utils;

/**
 * 结果对象
 */
public class ResultMsg {
	private boolean success;//是否成功 true：成功 false：失败
	private String code;//错误码
	private String msg;//异常信息
	private String data;//数据集
	
	/**
	 * true 成功  false 失败
	 * @return boolean
	 */
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	/**
	 *  1000 服务器出现未知异常！
	 *  1001 操作不合法，操作前未获取Token，或Token已过时
	 *	1002 签名验证不通过！
	 *	1003Json参数解析出错
	 *	1004 操作不合法，cust_code: xxxxxx不存在
	 *	1005 客户端IP鉴权不通过
	 *	1006 客户账号已停用！
	 *	1008 客户提交接口协议HTTPS, 与客户参数允许的协议不一致！
	 *	1009 提交的短信内容超过规定的长度！
	 *	1011 客户账户不存在！
	 *	1012 账户没有足够的余额
	 *	1013 扩展号码(sp_code)不符合规范！
	 */
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * 错误信息描述
	 * @return msg
	 */
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	/**
	 * 数据结果 json字符串
	 * @return String
	 */
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	
}
