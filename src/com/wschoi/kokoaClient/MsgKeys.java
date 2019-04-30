package com.wschoi.kokoaClient;

public enum MsgKeys {
	BlankIdField("Enter User ID!");
	
	String msg;
	MsgKeys(String msg){
		this.msg = msg;
	}
	String getMsg(){
		return msg;
	}
}
