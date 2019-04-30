package com.wschoi.kokoaClient.enums;

public enum AlertMsgs {
	NoSuchID("No Such ID exists!"),
	WrongPassword("Wrong Password!"),
	LoginSuccess("Login Success!"),
	BlankPWField("Enter User Password!"),
	BlankIdField("Enter User ID!");
	
	String msg;
	AlertMsgs(String msg){
		this.msg = msg;
	}
	public String getMsg(){
		return msg;
	}
}
