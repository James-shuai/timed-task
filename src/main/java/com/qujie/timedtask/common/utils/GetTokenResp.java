package com.qujie.timedtask.common.utils;

/**
 * token对象实体
 */
public class GetTokenResp {
	
	private String token_id;
	private String token;
	
	public GetTokenResp(){}
	
	public GetTokenResp(String token_id, String token){
		this.token_id = token_id;
		this.token = token;
	}
	public String getToken_id() {
		return token_id;
	}
	public void setToken_id(String token_id) {
		this.token_id = token_id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
}
