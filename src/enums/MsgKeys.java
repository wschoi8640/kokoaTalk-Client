package enums;
/**
 * This Enum contains MsgKeys
 * 
 * @author wschoi8640
 * @version 1.0
 */
public enum MsgKeys {
	ChatroomAddFailByDupli("chatroom_exists"),
	ChatroomRemoveRequest("rmv_chatroom"),
	ChatroomAddRequest("add_chatroom"),
	ChatroomAddSuccess("chatroom_added"),
	ReceiveSuccess("rcv_ok"),
	ReceiveChatrooms("rcv_chatrooms"),
	ReceiveFriends("rcv_friends"),
	JoinFail("join_fail"),
	JoinSuccess("join_ok"),
	JoinRequest("do_join"),
	FriendAddFailByDupli("friend_exists"),
	FriendAddFailByID("no_such_user"),
	FriendAddSuccess("add"),
	FriendAddRequest("add_friend"),
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
