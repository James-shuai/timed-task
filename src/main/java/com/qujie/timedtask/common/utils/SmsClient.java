package com.qujie.timedtask.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;


/**
 * 短信服务相关方法
 */
public class SmsClient {
	
	/**
	 * 获取token信息
	 * @param custCode 客户账号
	 * @param serviceBaseUrl https://ip:port
	 * @return ResultMsg
	 */
	private ResultMsg getToken(String custCode, String serviceBaseUrl){
		ResultMsg resultMsg = new ResultMsg();
		try {
			//发送token请求
			QueryReq getTokenReq = new QueryReq();
			
			getTokenReq.setCust_code(custCode);
			
			String postData = GsonUtils.toJSONString(getTokenReq);//JSON.toJSONString(getTokenReq);
			String getTokenResp = HttpsClient.post(serviceBaseUrl + "/getToken", postData,
					"application/json", "utf-8");
			resultMsg.setSuccess(true);
			resultMsg.setData(getTokenResp);
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg.setSuccess(false);
			resultMsg.setCode("1000");
			resultMsg.setMsg("服务器出现未知异常");
		}
		return resultMsg;
	}
	
	/**
	 * 发送短信
	 * @param smsReq SmsReq实体类
	 * @param password 客户密码
	 * @param serviceBaseUrl https://ip:port
	 * @return ResultMsg
	 */
	public ResultMsg sendSms(SmsReq smsReq, String password, String serviceBaseUrl){

		SmsClient smsClient = new SmsClient();
		ResultMsg resultMsg = smsClient.getToken(smsReq.getCust_code(), serviceBaseUrl);
		
		try {
			String sign = MD5.getMD5((smsReq.getContent() + password).getBytes("utf-8"));
			smsReq.setSign(sign);
			String postData = GsonUtils.toJSONString(smsReq);
			String sendSmsResp = HttpsClient.post(serviceBaseUrl + "/sendSms", postData, "application/json", "utf-8");

			resultMsg.setSuccess(true);
			resultMsg.setData(sendSmsResp);
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg.setSuccess(false);
			resultMsg.setCode("1000");
			resultMsg.setMsg("服务器出现未知异常");
		}
		return resultMsg;
	}
	
	/**
	 * 发送短信
	 * @param uid 			[选填] 业务标识，由贵司自定义32为数字透传至我司
	 * @param custCode 		[必填] 用户账号
	 * @param content 		[必填] 短信内容
	 * @param destMobiles 	[必填] 接收号码，同时发送给多个号码时,号码之间用英文半角逗号分隔(,)
	 * @param needReport 	[选填] 状态报告需求与否，是 yes 否 no 默认yes
	 * @param spCode 		[选填] 长号码
	 * @param msgFmt 		[选填] 信息格式，0：ASCII串；3：短信写卡操作；4：二进制信息；8：UCS2编码；默认8
	 * @param serviceBaseUrl 			[必填] https://ip:port
	 * @param password 		[必填] 账号密码
	 * @return ResultMsg
	 */
	public ResultMsg sendSms(String uid, String custCode, String content, String destMobiles,
			String needReport, String spCode, String msgFmt, String serviceBaseUrl,String password){

		SmsClient smsClient = new SmsClient();
		ResultMsg resultMsg = smsClient.getToken(custCode, serviceBaseUrl);
		try {
			SmsReq req = new SmsReq();
			req.setUid(uid);
			req.setCust_code(custCode);
			req.setContent(content);
			req.setDestMobiles(destMobiles);
			req.setNeed_report(needReport);
			req.setSp_code(spCode);
			req.setMsgFmt(msgFmt);
			
			String sign = MD5.getMD5((req.getContent() + password).getBytes("utf-8"));
			req.setSign(sign);
			String postData = GsonUtils.toJSONString(req);
			String sendSmsResp = HttpsClient.post(serviceBaseUrl + "/sendSms", postData, "application/json", "utf-8");
			
			JSONObject jsonObject = GsonUtils.parseObject(sendSmsResp, JSONObject.class);
			if (!"failed".equals(jsonObject.getString("status"))) {//判断是否获取token信息成功
				resultMsg.setSuccess(true);
				resultMsg.setData(sendSmsResp);
			} else {
				resultMsg.setSuccess(false);
				resultMsg.setCode(jsonObject.getString("respCode"));
				resultMsg.setMsg(jsonObject.getString("respMsg"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg.setSuccess(false);
			resultMsg.setCode("1000");
			resultMsg.setMsg("服务器出现未知异常");
		}
		return resultMsg;
	}
	
	
	/**
	 * 发送变量短信
	 * @param variantSmsReq VariantSmsReq实体类
	 * @param password 客户密码
	 * @param serviceBaseUrl https://ip:port
	 * @return ResultMsg
	 */
	public ResultMsg sendVariantSms(VariantSmsReq variantSmsReq, String serviceBaseUrl,String password){

		SmsClient smsClient = new SmsClient();
		ResultMsg resultMsg = smsClient.getToken(variantSmsReq.getCust_code(), serviceBaseUrl);
		try {
			String sign = MD5.getMD5((variantSmsReq.getContent() + password).getBytes("utf-8"));
			variantSmsReq.setSign(sign);
			String postData = GsonUtils.toJSONString(variantSmsReq);
			String sendSmsResp = HttpsClient.post(serviceBaseUrl + "/sendVariantSms", postData, "application/json", "utf-8");
			
			JSONObject jsonObject = GsonUtils.parseObject(sendSmsResp, JSONObject.class);
			if (!"failed".equals(jsonObject.getString("status"))) {//判断是否获取token信息成功
				resultMsg.setSuccess(true);
				resultMsg.setData(sendSmsResp);
			} else {
				resultMsg.setSuccess(false);
				resultMsg.setCode(jsonObject.getString("respCode"));
				resultMsg.setMsg(jsonObject.getString("respMsg"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg.setSuccess(false);
			resultMsg.setCode("1000");
			resultMsg.setMsg("服务器出现未知异常");
		}
		return resultMsg;
	}
	
	
	/**
	 * 查询账户余额
	 * @param custCode 客户账号
	 * @param password 客户密码
	 * @param serviceBaseUrl https://ip:port
	 * @return ResultMsg
	 */
	public ResultMsg queryAccount(String custCode, String password, String serviceBaseUrl){

		SmsClient smsClient = new SmsClient();
		ResultMsg resultMsg = smsClient.getToken(custCode, serviceBaseUrl);
		try {
			if (resultMsg.isSuccess()) {
				GetTokenResp gtResp = GsonUtils.parseObject(resultMsg.getData(), GetTokenResp.class);
				QueryReq queryAccountReq = new QueryReq();
				String sign = MD5.getMD5((gtResp.getToken() + password).getBytes("utf-8"));
				
				queryAccountReq.setToken_id(gtResp.getToken_id());
				queryAccountReq.setCust_code(custCode);
				queryAccountReq.setSign(sign);
				
				String postData = GsonUtils.toJSONString(queryAccountReq);
				String queryAccountResp = HttpsClient.post(serviceBaseUrl + "/queryAccount",
						postData, "application/json", "utf-8");
				
				JSONObject jsonObject = GsonUtils.parseObject(queryAccountResp, JSONObject.class);

				if (!"failed".equals(jsonObject.getString("status"))) {
					resultMsg.setSuccess(true);
					resultMsg.setData(queryAccountResp);
				} else {
					resultMsg.setSuccess(false);
					resultMsg.setCode(jsonObject.getString("respCode"));
					resultMsg.setMsg(jsonObject.getString("respMsg"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg.setSuccess(false);
			resultMsg.setCode("1000");
			resultMsg.setMsg("服务器出现未知异常");
		}
		return resultMsg;
	}
	
	
	/**
	 * 获取上行记录
	 * @param custCode 客户账号
	 * @param password 客户密码
	 * @param serviceBaseUrl https://ip:port
	 * @return ResultMsg
	 */
	public ResultMsg getMo(String custCode, String password, String serviceBaseUrl){

		SmsClient smsClient = new SmsClient();
		ResultMsg resultMsg = smsClient.getToken(custCode, serviceBaseUrl);
		try {
			if (resultMsg.isSuccess()) {
				GetTokenResp gtResp = GsonUtils.parseObject(resultMsg.getData(), GetTokenResp.class);
				QueryReq queryAccountReq = new QueryReq();
				String sign = MD5.getMD5((gtResp.getToken() + password).getBytes("utf-8"));
				
				queryAccountReq.setToken_id(gtResp.getToken_id());
				queryAccountReq.setCust_code(custCode);
				queryAccountReq.setSign(sign);
				
				String postData = GsonUtils.toJSONString(queryAccountReq);
				String getMoResp = HttpsClient.post(serviceBaseUrl + "/getMO",
						postData, "application/json", "utf-8");
				
				JSONObject jsonObject;
				try {
					GsonUtils.parseArray(getMoResp, JsonArray.class);//无异常代表是json数组，即正常返回数据
					resultMsg.setSuccess(true);
					resultMsg.setData(getMoResp);
				} catch (Exception e) {
					jsonObject = GsonUtils.parseObject(getMoResp, JSONObject.class);
					if (!"failed".equals(jsonObject.getString("status"))) {
						resultMsg.setSuccess(true);
						resultMsg.setData(getMoResp);
					} else {
						resultMsg.setSuccess(false);
						resultMsg.setCode(jsonObject.getString("respCode"));
						resultMsg.setMsg(jsonObject.getString("respMsg"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg.setSuccess(false);
			resultMsg.setCode("1000");
			resultMsg.setMsg("服务器出现未知异常");
		}
		return resultMsg;
	}
	
	/**
	 * 获取状态报告
	 * @param custCode 客户账号
	 * @param password 客户密码
	 * @param serviceBaseUrl https://ip:port
	 * @return ResultMsg
	 */
	public ResultMsg getReport(String custCode, String password, String serviceBaseUrl){

		SmsClient smsClient = new SmsClient();
		ResultMsg resultMsg = smsClient.getToken(custCode, serviceBaseUrl);
		try {
			if (resultMsg.isSuccess()) {
				GetTokenResp gtResp = GsonUtils.parseObject(resultMsg.getData(), GetTokenResp.class);
				QueryReq queryAccountReq = new QueryReq();
				String sign = MD5.getMD5((gtResp.getToken() + password).getBytes("utf-8"));
				
				queryAccountReq.setToken_id(gtResp.getToken_id());
				queryAccountReq.setCust_code(custCode);
				queryAccountReq.setSign(sign);
				
				String postData = GsonUtils.toJSONString(queryAccountReq);
				String getReportResp = HttpsClient.post(serviceBaseUrl + "/getReport",
						postData, "application/json", "utf-8");
				
				JSONObject jsonObject;
				try {
					GsonUtils.parseArray(getReportResp, JsonArray.class);//无异常代表是json数组，即正常返回数据
					resultMsg.setSuccess(true);
					resultMsg.setData(getReportResp);
				} catch (Exception e) {
					jsonObject = GsonUtils.parseObject(getReportResp, JSONObject.class);
					if (!"failed".equals(jsonObject.getString("status"))) {
						resultMsg.setSuccess(true);
						resultMsg.setData(getReportResp);
					} else {
						resultMsg.setSuccess(false);
						resultMsg.setCode(jsonObject.getString("respCode"));
						resultMsg.setMsg(jsonObject.getString("respMsg"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg.setSuccess(false);
			resultMsg.setCode("1000");
			resultMsg.setMsg("服务器出现未知异常");
		}
		return resultMsg;
	}
	
	/**
	 * 获取某手机号码的黑名单类型
	 * @param custCode 客户账号
	 * @param password 客户密码
	 * @param serviceBaseUrl https://ip:port
	 * @param mobile 手机号码
	 * @return ResultMsg
	 */
	public ResultMsg getBlacklist(String custCode, String password, String serviceBaseUrl, String mobile){

		SmsClient smsClient = new SmsClient();
		ResultMsg resultMsg = smsClient.getToken(custCode, serviceBaseUrl);
		try {
			if (resultMsg.isSuccess()) {
				GetTokenResp gtResp = GsonUtils.parseObject(resultMsg.getData(), GetTokenResp.class);
				QueryReq queryAccountReq = new QueryReq();
				String sign = MD5.getMD5((gtResp.getToken() + password).getBytes("utf-8"));
				
				queryAccountReq.setToken_id(gtResp.getToken_id());
				queryAccountReq.setCust_code(custCode);
				queryAccountReq.setSign(sign);
				queryAccountReq.setMobile(mobile);
				
				String postData = GsonUtils.toJSONString(queryAccountReq);
				String getReportResp = HttpsClient.post(serviceBaseUrl + "/getBlacklist",
						postData, "application/json", "utf-8");
				
				JSONObject jsonObject;
				try {
					GsonUtils.parseArray(getReportResp, JsonArray.class);//无异常代表是json数组，即正常返回数据
					resultMsg.setSuccess(true);
					resultMsg.setData(getReportResp);
				} catch (Exception e) {
					jsonObject = GsonUtils.parseObject(getReportResp, JSONObject.class);
					if (!"failed".equals(jsonObject.getString("status"))) {
						resultMsg.setSuccess(true);
						resultMsg.setData(getReportResp);
					} else {
						resultMsg.setSuccess(false);
						resultMsg.setCode(jsonObject.getString("respCode"));
						resultMsg.setMsg(jsonObject.getString("respMsg"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg.setSuccess(false);
			resultMsg.setCode("1000");
			resultMsg.setMsg("服务器出现未知异常");
		}
		return resultMsg;
	}

	
	public boolean send(String custCode,String password,String serviceBaseUrl,String mobile,String content) {
//		String custCode = "300481";							 //[必填] 用户账号
//		String password = "8JZTNP1WIE";						 //[必填] 账号密码
//		String serviceBaseUrl = "http://123.58.255.70:8860"; 			 //[必填] https://ip:port

		try{
			/**
			 * 1.通过SmsReq对象传参
			 */  
			SmsReq smsReq = new SmsReq();
			smsReq.setUid("");							//[选填] 业务标识，由贵司自定义32为数字透传至我司
			smsReq.setCust_code(custCode);				//[必填] 用户账号
			smsReq.setContent(content);				//[必填] 短信内容
			smsReq.setDestMobiles(mobile);		//[必填] 接收号码，同时发送给多个号码时,号码之间用英文半角逗号分隔(,)
			smsReq.setNeed_report("yes");				//[选填] 状态报告需求与否，是 yes 否 no 默认yes
			smsReq.setSp_code(custCode);						//[选填] 长号码
			smsReq.setMsgFmt("8");						//[选填] 信息格式，0：ASCII串；3：短信写卡操作；4：二进制信息；8：UCS2编码；默认8
	
			SmsClient smsClient = new SmsClient();
			ResultMsg resultMsg = smsClient.sendSms(smsReq, password, serviceBaseUrl);
			if (resultMsg.isSuccess()) {
				/**
				 * 成功返回json对象字符串，data数据如下：
				 * {
					    "uid": "1123344567",
					    "status": "success",
					    "respCode": "0",
					    "respMsg": "提交成功！",
					    "totalChargeNum": 1,
					    "result": [
					        {
					            "msgid": "59106312221352221524",
					            "mobile": "1348908xxxx",
					            "code": "0",
					            "msg": "提交成功.",
					            "chargeNum": 1
					        }
					    ]
					}
				 */
				System.out.println(resultMsg.getData());
				return true;
			} else {
				/**
				 *  1000：服务器出现未知异常！
				 *  1001 操作不合法，操作前未获取Token，或Token已过时
				 *	1002 签名验证不通过！
				 *	1003 Json参数解析出错
				 *	1004 操作不合法，cust_code: xxxxxx不存在
				 *	1005 客户端IP鉴权不通过
				 *	1006 客户账号已停用！
				 *	1008 客户提交接口协议HTTPS, 与客户参数允许的协议不一致！
				 *	1009 提交的短信内容超过规定的长度！
				 *	1011 客户账户不存在！
				 *	1012 账户没有足够的余额
				 *	1013 扩展号码(sp_code)不符合规范！
				 */
				System.out.println(resultMsg.getCode());
				System.out.println(resultMsg.getMsg());
				return false;
			}
		}
		catch(Exception e){
		}
		return false;
	}
}
