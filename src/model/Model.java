package model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import domain.ChatRoomService;
import domain.ChatUserSevice;
import domain.ChattingRoom;
import domain.JoinService;
import domain.LoginService;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Model {
	private ChattingRoom chattingRoom;
	private JoinService joinService;
	private LoginService loginService;
	private ChatUserSevice chatUserService;
	private ChatRoomService chatRoomService;
	private Socket sock;
	private ObjectInputStream messageListRcv;
	private ObjectOutputStream messageListSend;
	private PrintWriter messageSend;
	private BufferedReader messageRcv;
	private InputStream in;
	private OutputStream out;
	private GridPane friendGrid;
	private GridPane loginGrid;
	private Label titleLabel; 
	public ChattingRoom getChattingRoom() {
		return chattingRoom;
	}
	public void setChattingRoom(ChattingRoom chattingRoom) {
		this.chattingRoom = chattingRoom;
	}
	public JoinService getJoinService() {
		return joinService;
	}
	public void setJoinService(JoinService joinService) {
		this.joinService = joinService;
	}
	public LoginService getLoginService() {
		return loginService;
	}
	public void setLoginService(LoginService loginService) {
		this.loginService = loginService;
	}
	public ChatUserSevice getChatUserService() {
		return chatUserService;
	}
	public void setChatUserService(ChatUserSevice chatUserService) {
		this.chatUserService = chatUserService;
	}
	public ChatRoomService getChatRoomService() {
		return chatRoomService;
	}
	public void setChatRoomService(ChatRoomService chatRoomService) {
		this.chatRoomService = chatRoomService;
	}
	public Socket getSock() {
		return sock;
	}
	public void setSock(Socket sock) {
		this.sock = sock;
	}
	public ObjectInputStream getMessageListRcv() {
		return messageListRcv;
	}
	public void setMessageListRcv(ObjectInputStream messageListRcv) {
		this.messageListRcv = messageListRcv;
	}
	public ObjectOutputStream getMessageListSend() {
		return messageListSend;
	}
	public void setMessageListSend(ObjectOutputStream messageListSend) {
		this.messageListSend = messageListSend;
	}
	public PrintWriter getMessageSend() {
		return messageSend;
	}
	public void setMessageSend(PrintWriter messageSend) {
		this.messageSend = messageSend;
	}
	public BufferedReader getMessageRcv() {
		return messageRcv;
	}
	public void setMessageRcv(BufferedReader messageRcv) {
		this.messageRcv = messageRcv;
	}
	public InputStream getIn() {
		return in;
	}
	public void setIn(InputStream in) {
		this.in = in;
	}
	public OutputStream getOut() {
		return out;
	}
	public void setOut(OutputStream out) {
		this.out = out;
	}
	public GridPane getFriendGrid() {
		return friendGrid;
	}
	public void setFriendGrid(GridPane friendGrid) {
		this.friendGrid = friendGrid;
	}
	public GridPane getLoginGrid() {
		return loginGrid;
	}
	public void setLoginGrid(GridPane loginGrid) {
		this.loginGrid = loginGrid;
	}
	public Label getTitleLabel() {
		return titleLabel;
	}
	public void setTitleLabel(Label titleLabel) {
		this.titleLabel = titleLabel;
	}
	public String getConnectedID() {
		return connectedID;
	}
	public void setConnectedID(String connectedID) {
		this.connectedID = connectedID;
	}
	public String getConnectedName() {
		return connectedName;
	}
	public void setConnectedName(String connectedName) {
		this.connectedName = connectedName;
	}
	public List<ToggleButton> getFriendsList() {
		return friendsList;
	}
	public void setFriendsList(List<ToggleButton> friendsList) {
		this.friendsList = friendsList;
	}
	public List<Stage> getOpenedChatrooms() {
		return openedChatrooms;
	}
	public void setOpenedChatrooms(List<Stage> openedChatrooms) {
		this.openedChatrooms = openedChatrooms;
	}
	public String[] getArgs() {
		return args;
	}
	public void setArgs(String[] args) {
		this.args = args;
	}
	private String connectedID;
	private String connectedName;
	private List<ToggleButton> friendsList;
	private List<Stage> openedChatrooms;
	private String [] args;
	

}
