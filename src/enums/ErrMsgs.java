package enums;

public enum ErrMsgs {
	AlreadyFriend("Already Added!"),
	NoSuchUser("No Such User!"),
	AddMySelf("Cannot add Yourself!"),
	NothingInserted("Nothing Inserted!"),
	NoSuchID("No Such ID exists!"),
	WrongPassword("Wrong Password!"),
	LoginSuccess("Login Success!"),
	BlankPWField("Enter User Password!"),
	BlankIdField("Enter User ID!");
	
	String msg;
	ErrMsgs(String msg){
		this.msg = msg;
	}
	public String getMsg(){
		return msg;
	}
}
