package enums;

public enum Settings {
	WelcomeMsg("Welcome!"),
	ConnectedFriendColor("-fx-background-color: Yellow"),
	Font("Consolas"),
	LoginServerPort(10001),
	ServerIP("192.168.100.112"),
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