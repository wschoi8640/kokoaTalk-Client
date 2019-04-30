package enums;

public enum MsgKeys {
	AddFailByDupli("friend_exists"),
	AddFailByID("no_such_user"),
	AddSuccess("add"),
	AddRequest("add_friend"),
	RemoveSuccess("rmv_ok"),
	RemoveRequest("rmv_friend"),
	RefreshSuccess("refresh_ok"),
	RefreshRequest("do_refresh"),
	LogoutRequest("do_logout"),
	LoginFailByID("no_id"),
	LoginFailByPW("wrong_pw"),
	LoginSuccess("hello"),
	LoginRequest("do_login");
	
	String key;
	MsgKeys(String key){
		this.key = key;
	}
	public String getKey(){
		return key;
	}
}
