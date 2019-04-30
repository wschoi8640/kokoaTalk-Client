package com.wschoi.kokoaClient;

public enum MsgKeys {
	LogoutRequest("do_logout"),
	LoginFailByID("no_id"),
	LoginFailByPW("wrong_pw"),
	LoginResponse("hello"),
	LoginRequest("do_login");
	
	String key;
	MsgKeys(String key){
		this.key = key;
	}
	String getKey(){
		return key;
	}
}
