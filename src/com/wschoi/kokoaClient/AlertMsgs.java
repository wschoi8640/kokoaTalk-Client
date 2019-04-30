package com.wschoi.kokoaClient;

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
	String getMsg(){
		return msg;
	}
}
