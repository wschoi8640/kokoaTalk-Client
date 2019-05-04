package enums;
/**
 * This Enum Contains Settings of Client
 * 
 * @author wschoi8640
 * @version 1.0
 */
public enum Settings {
	WelcomeMsg("Welcome!"),
	ConnectedFriendColor("-fx-background-color: Yellow"),
	Font("Consolas"),
	LoginServerPort(10001),
	ServerIP("192.168.0.12"),
	Title("KokoaTalk");
	
	String setting;
	int num;
	Settings(String strSetting){
		this.setting = strSetting;
	}
	Settings(int num){
		this.num = num;
	}
	public String getSetting(){
		return setting;
	}
	public int getNum() {
		return num;
	}
}