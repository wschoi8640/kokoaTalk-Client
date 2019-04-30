package com.wschoi.kokoaClient;

public enum ClientSettings {
	Font("Consolas"),
	LoginServerPort(10001),
	ServerIP("192.168.0.43"),
	Title("KokoaTalk");
	String setting;
	int num;
	ClientSettings(String strSetting){
		this.setting = strSetting;
	}
	ClientSettings(int num){
		this.num = num;
	}
	String getSetting(){
		return setting;
	}
	int getNum() {
		return num;
	}
}
