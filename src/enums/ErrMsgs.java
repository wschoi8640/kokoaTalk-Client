package enums;

public enum ErrMsgs {
	WrongPWFormat("must include Alphabet and Number!"),
	WrongIDFormat("Symbols Not allowed!"),
	AlreadyExistingID("ID Already Exist!"),
	AlreadyOpenedChat("Already Opened Chatroom!"),
	ChooseOneChatRoom("Choose one Chatroom!"),
	NoChatRoomChosen("No Chatroom Chosen!"),
	JoinSuccess("Join Success!"),
	WrongPWRepeat("Password Not Same!"),
	AlreadyAdded("Already Added!"),
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
