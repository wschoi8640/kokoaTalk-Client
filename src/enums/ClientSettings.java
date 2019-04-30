package enums;

public enum ClientSettings {
	Font("Consolas"),
	LoginServerPort(10001),
	ServerIP("192.168.0.154"),
	Title("KokoaTalk");
	String setting;
	int num;
	ClientSettings(String strSetting){
		this.setting = strSetting;
	}
	ClientSettings(int num){
		this.num = num;
	}
	public String getSetting(){
		return setting;
	}
	public int getNum() {
		return num;
	}
}
