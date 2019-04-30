package enums;

public enum ErrMsgs {
	JoinSuccess("Join Success!"),
	WrongPWRepeat("Password Not Same!"),
	AlreadyFriend("Already Added!"),
	NoSuchUser("No Such User!"),
	AddMySelf("Cannot add Yourself!"),
	NothingInserted("Nothing Inserted!"),
	NoSuchID("No Such ID exists!"),
	WrongPassword("Wrong Password!"),
	LoginSuccess("Login Success!"),
	BlankNameField("Enter User Name!"),
	BlankPWField("Enter User Password!"),
	BlankRepeatPW("Repeat User Password!"),
	BlankIdField("Enter User ID!");
	
	String msg;
	ErrMsgs(String msg){
		this.msg = msg;
	}
	public String getMsg(){
		return msg;
	}
}
