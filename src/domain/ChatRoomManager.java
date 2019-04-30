package domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import enums.ErrMsgs;
import enums.MsgKeys;
import enums.Settings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Model;
import utils.AlertHandler;

/**
 * This class consists ChatRoomService Panel and offers ChatRoomManage service 
 * 
 * @author wschoi8640
 * @version 1.0
 */
public class ChatRoomManager extends VBox {
	
	private Model model;
	private Button showUsrBtn;
	private Button showChatBtn;
	private Button openChatBtn;
	private Button addChatroomBtn;
	private Button rmvChatroomBtn;
	private Button logoutBtn;
	private ToggleButton newChatroom;
	private BufferedReader messageRcv;
	private Socket sock;
	private ScrollPane scrollPane;
	private GridPane menuGrid;
	private GridPane chatroomGrid;
	private GridPane funcGrid;
	private Label userNameLabel;
	private List<String> messageList;
	private List<String> friendsList;
	private List<ArrayList<String>> chatroomList;
	private List<String> selectedFriendList;
	private List<Stage> openedChatroomList;
	private List<ToggleButton> chatroomButtons;
	private ObjectOutputStream messageListSend;
	private ObjectInputStream messageListRcv;

	private String selectedChatroom = "";
	private int chatroom_row = 0;
	private int chatroom_column = 0;
	private double btnHeight = 50;
	private double btnWidth = 300;

	/**
	 * receive Primary Stage and Socket Object
	 * <br/> consist Panel
	 * @param model
	 */
	public ChatRoomManager(Model model) {
		this.model = model;
		this.sock = model.getSock();
		initChatRoomManageGrid();
	}

	void initChatRoomManageGrid() {
		// message List to send Server 
		messageList = new ArrayList<String>();
		
		// user's Friend List
		friendsList = new ArrayList<String>();
		
		// user's Chatroom List
		chatroomList = new ArrayList<ArrayList<String>>();
		openedChatroomList = new ArrayList<Stage>();
		chatroomButtons = new ArrayList<ToggleButton>();
		selectedFriendList = new ArrayList<String>();

		// Menu Grid
		menuGrid = new GridPane();
		menuGrid.setAlignment(Pos.TOP_CENTER);
		menuGrid.setVgap(15);

		// ChatRoom Grid
		chatroomGrid = new GridPane();
		chatroomGrid.setPrefSize(600, 550);
		chatroomGrid.setHgap(15);
		chatroomGrid.setVgap(15);

		// add / rmv Util Grid
		funcGrid = new GridPane();
		funcGrid.setVgap(15);

		// btn for Changing Grid(show Friends)
		showUsrBtn = new Button("Show Friends");
		showUsrBtn.setPrefWidth(btnWidth);
		showUsrBtn.setPrefHeight(btnHeight);
		showUsrBtn.setOnAction(e -> showUserHandler(e));

		// btn for Changing Grid(show Chat rooms)
		showChatBtn = new Button("Show Chat Rooms");
		showChatBtn.setPrefWidth(btnWidth);
		showChatBtn.setPrefHeight(btnHeight);

		// btn for Open Chatroom
		openChatBtn = new Button("Open Chatroom");
		openChatBtn.setPrefSize(2 * btnWidth, btnHeight);
		openChatBtn.setOnAction(e -> openChatHandler(e));

		// shows current User Name
		userNameLabel = new Label(model.getConnectedName());
		userNameLabel.setFont(new Font(Settings.Font.getSetting(), 30.0));
		userNameLabel.setPrefHeight(btnHeight);

		// btn for Add Chatroom
		addChatroomBtn = new Button("Add Chatroom");
		addChatroomBtn.setPrefSize(btnWidth, btnHeight);
		addChatroomBtn.setOnAction(e -> addChatroomHandler(e));

		// btn for Remove Chatroom
		rmvChatroomBtn = new Button("Remove Chatroom");
		rmvChatroomBtn.setPrefSize(btnWidth, btnHeight);
		rmvChatroomBtn.setOnAction(e -> rmvChatroomHandler(e));

		// btn for Logout
		logoutBtn = new Button("Logout");
		logoutBtn.setPrefSize(2 * btnWidth, btnHeight);
		logoutBtn.setOnAction(e -> logoutHandler(e));

		menuGrid.add(showUsrBtn, 0, 0);
		menuGrid.add(showChatBtn, 1, 0);

		funcGrid.add(addChatroomBtn, 0, 0);
		funcGrid.add(rmvChatroomBtn, 1, 0);

		// rcv ChatRoom List from Server 
		chatroomButtons = rcvChatrooms();
		if (chatroomButtons == null)
			chatroomButtons = new ArrayList<ToggleButton>();

		// add Chatrooms to Chatroom Grid
		addChatroomsToGrid(chatroomButtons);

		// add Scroll to Grid
		scrollPane = new ScrollPane(chatroomGrid);
		scrollPane.setStyle("-fx-background-color:transparent;");
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		// Set Auto Change by Panel size changes
		model.getLoginService().widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth,
					Number newSceneWidth) {
				setWidthProperty(newSceneWidth);
			}
		});
		model.getLoginService().heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight,
					Number newSceneHeight) {
				setHeightProperty(newSceneHeight);
			}
		});
		this.setAlignment(Pos.TOP_CENTER);
		this.setFillWidth(true);
		this.setSpacing(btnHeight / 5);
		this.getChildren().addAll(menuGrid, userNameLabel, openChatBtn, chatroomGrid, scrollPane, funcGrid, logoutBtn);
	}

	protected void setHeightProperty(Number newSceneHeight) {
		chatroomGrid.setPrefHeight(newSceneHeight.doubleValue() * 5.5 / 7);
		showUsrBtn.setPrefHeight(newSceneHeight.doubleValue() / 14);
		showChatBtn.setPrefHeight(newSceneHeight.doubleValue() / 14);
		userNameLabel.setPrefHeight(newSceneHeight.doubleValue() / 14);
	}

	protected void setWidthProperty(Number newSceneWidth) {
		chatroomGrid.setPrefWidth(newSceneWidth.doubleValue());
		showUsrBtn.setPrefWidth(newSceneWidth.doubleValue() / 2);
		showChatBtn.setPrefWidth(newSceneWidth.doubleValue() / 2);
		openChatBtn.setPrefWidth(newSceneWidth.doubleValue());
		addChatroomBtn.setPrefWidth(newSceneWidth.doubleValue() / 2);
		rmvChatroomBtn.setPrefWidth(newSceneWidth.doubleValue() / 2);
		logoutBtn.setPrefWidth(newSceneWidth.doubleValue());
	}

	/**
	 * Removes Selected Chatrooms from Grid
	 * <br/>
	 * <br/>1. Make List from selected Chatrooms. 
	 * <br/>2. Send Server key and List. 
	 * <br/>3. Remove Selected Chatrooms by Server response.
	 * 
	 * @param removeEvent
	 */
	void rmvChatroomHandler(ActionEvent e) {
		// Remove when having any Chatroom
		if (!chatroomButtons.isEmpty()) {
			List<String> rmvChatroomsList = new ArrayList<String>();
			// Add Selected Chatrooms to Remove List
			for (ToggleButton chatroom : chatroomButtons) {
				if (chatroom.isSelected())
					rmvChatroomsList.add(chatroom.getText());
			}
			// Send Server when having any Selected Friends
			if (!rmvChatroomsList.isEmpty()) {
				try {
					if (sock.isConnected()) {
						messageListSend = model.getMessageListSend();
						messageRcv = model.getMessageRcv();

						// Add key and UserName and List to Msg List 
						messageList.clear();
						messageList.add(0, "rmv_chatroom");
						messageList.add(1, model.getConnectedName());
						for (String rmvChatroom : rmvChatroomsList) {
							messageList.add(rmvChatroom);
						}

						// Send Server Msg
						messageListSend.writeObject(messageList);
						messageListSend.flush();
						messageListSend.reset();
						messageList.clear();

						// Receive Server Response
						String message;
						if ((message = messageRcv.readLine()) != null) {
							// Remove Successful
							if (message.equals(MsgKeys.RemoveSuccess.getKey()) || message.equals("yrmv_ok")) {
								List<ToggleButton> tempList = new ArrayList<ToggleButton>();
								tempList.addAll(chatroomButtons);
								// Remove Selected Chatrooms from Chatroom List
								for (ToggleButton myChatrooms : tempList) {
									for (String rmvChatroom : rmvChatroomsList) {
										if (myChatrooms.getText().equals(rmvChatroom))
											chatroomButtons.remove(myChatrooms);
									}
								}
								// Update Grid
								chatroomGrid.getChildren().clear();
								addChatroomsToGrid(chatroomButtons);
								// Save Modification
								model.setChatroomGrid(chatroomGrid);
								model.setChatroomsList(chatroomButtons);
							}
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		}
	}

	/**
	 * Execute on OpenChat Btn Event
	 * <br/>
	 * <br/>1. Count selected chatrooms
	 * <br/>2. Alert if num of chatroom > 1
	 * <br/>2. Open new Window and Add ChattingRoom Grid
	 * 
	 * @param openEvent
	 */
	void openChatHandler(ActionEvent e) {
		int count = 0;

		// Count selected Chatroom
		for (ToggleButton chatroom : chatroomButtons) {
			if (chatroom.isSelected()) {
				selectedChatroom = chatroom.getText();
				count++;
			}
		}

		// Check Count and Alert
		if (count == 0) {
			AlertHandler.alert(ErrMsgs.NoChatRoomChosen.getMsg());
		}
		if (count > 1) {
			AlertHandler.alert(ErrMsgs.ChooseOneChatRoom.getMsg());
		}

		// Open Chatroom if Valid
		if (count == 1) {
			// Check if Chatroom is Opened
			for (Stage openedChatroom : openedChatroomList) {
				if (openedChatroom.getTitle().equals(selectedChatroom)) {
					AlertHandler.alert(ErrMsgs.AlreadyOpenedChat.getMsg());
					return;
				}
			}

			// Create Chatting Room and Add to new Window 
			ChattingRoom chattingRoom = new ChattingRoom(model.getConnectedName(), selectedChatroom, model);

			Stage stage = new Stage();
			stage.setOnCloseRequest(e1 -> closeHandler(e1, stage));
			stage.setTitle(selectedChatroom);
			stage.setScene(new Scene(chattingRoom, 600, 700));
			stage.show();

			// add opened ChattingRoom to List
			openedChatroomList.add(stage);
		}
	}

	/**
	 * Executed When ChattingRoom is Closed
	 * 
	 * @param windowCloseEvent
	 * @param primaryStage
	 */
	void closeHandler(WindowEvent e, Stage stage) {
		openedChatroomList.remove(stage);
		stage.close();
	}

	/**
	 * Receive user Chatrooms from Server
	 * <br/>
	 * <br/>1. Request Server User Chatroom List.
	 * <br/>2. Change List into Buttons and return.
	 * 
	 * @return List<Chatroom>
	 */
	private List<ToggleButton> rcvChatrooms() {
		if (sock.isConnected()) {
			List<ToggleButton> temp_list = new ArrayList<ToggleButton>();

			messageListSend = model.getMessageListSend();
			messageListRcv = model.getMessageListRcv();

			// Add Key and UserName to Msg List
			messageList.add(0, MsgKeys.ReceiveChatrooms.getKey());
			messageList.add(1, model.getConnectedName());

			try {
				// Send Request to Server
				messageListSend.writeObject(messageList);
				messageListSend.flush();
				messageListSend.reset();
				messageList.clear();
				// rcv Server response
				messageList = (ArrayList<String>) messageListRcv.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			// make ChatRoomBtnList from response
			if (messageList.get(0).equals(MsgKeys.ReceiveSuccess.getKey())) {
				for (int i = 1; i < messageList.size(); i++) {
					newChatroom = new ToggleButton(messageList.get(i));
					newChatroom.setPrefSize(2 * btnWidth, btnHeight);

					temp_list.add(newChatroom);
				}
				return temp_list;
			}
		}
		return null;

	}

	/**
	 * Add ChatRooms to ChatRooms Grid

	 * @param chatButtons
	 */
	void addChatroomsToGrid(List<ToggleButton> chats) {
		chatroom_row = 0;
		chatroom_column = 0;
		chatroomGrid.getChildren().clear();
		openChatBtn.setVisible(true);
		if (chats != null) {
			for (ToggleButton Friend : chats) {
				chatroomGrid.add(Friend, chatroom_column, chatroom_row);
				if (chatroom_column / 2 == 1) {
					chatroom_row++;
					chatroom_column = 0;
				} else {
					chatroom_column = chatroom_column + 2;
				}
			}
		}
		// Save Modification
		model.setChatroomGrid(chatroomGrid);
		model.setChatroomsList(chatroomButtons);
	}

	/**
	 * Execute On Add Chatroom Btn
	 * <br/>
	 * <br/>1. make Friend List from user Friend List
	 * <br/>2. make Blank List to show Selected Friends  
	 * <br/>3. make Selected List editable
	 * <br/>4. Add submit button and send selected List to server onsubmit 
	 * <br/>5. If successful add Chatroom, If fails show result
	 * 
	 * @param addEvent
	 */
	void addChatroomHandler(ActionEvent e) {
		openChatBtn.setVisible(false);
		friendsList.clear();
		chatroomGrid.getChildren().clear();
		chatroomGrid.getColumnConstraints().clear();

		// Add User Friends to FriendList
		for (ToggleButton friend : model.getFriendsList()) {
			friendsList.add(friend.getText());
		}
		chatroomGrid.setPadding(new Insets(5));
		chatroomGrid.setHgap(10);
		chatroomGrid.setVgap(10);

		// Create UI
		ColumnConstraints column1 = new ColumnConstraints(150, 150, Double.MAX_VALUE);
		ColumnConstraints column2 = new ColumnConstraints(50);
		ColumnConstraints column3 = new ColumnConstraints(150, 150, Double.MAX_VALUE);

		column1.setHgrow(Priority.ALWAYS);
		column3.setHgrow(Priority.ALWAYS);
		chatroomGrid.getColumnConstraints().addAll(column1, column2, column3);

		// Add Title to Friend List
		Label candidatesLbl = new Label("Friends");
		GridPane.setHalignment(candidatesLbl, HPos.CENTER);
		chatroomGrid.add(candidatesLbl, 0, 0);

		// Add Title to Selected List
		Label selectedLbl = new Label("Selected");
		chatroomGrid.add(selectedLbl, 2, 0);
		GridPane.setHalignment(selectedLbl, HPos.CENTER);

		// Add Friend List to ListView 
		final ObservableList<String> candidates = FXCollections.observableArrayList(friendsList);
		final ListView<String> candidatesListView = new ListView<>(candidates);
		chatroomGrid.add(candidatesListView, 0, 1);

		// Add Selected List to ListView
		final ObservableList<String> selected = FXCollections.observableArrayList();
		final ListView<String> selectedListView = new ListView<>(selected);
		chatroomGrid.add(selectedListView, 2, 1);

		// Add > button for selecting
		Button sendRightButton = new Button(" > ");
		sendRightButton.setOnAction((ActionEvent event) -> {
			// If any Selected Send to Selected List
			String selectedFriend = candidatesListView.getSelectionModel().getSelectedItem();
			if (selectedFriend != null) {
				candidatesListView.getSelectionModel().clearSelection();
				candidates.remove(selectedFriend);
				selected.add(selectedFriend);
			}
		});

		// Add < button for removing
		Button sendLeftButton = new Button(" < ");
		sendLeftButton.setOnAction((ActionEvent event) -> {

			// If any Selected remove from Selected List
			String unselectedFriend = selectedListView.getSelectionModel().getSelectedItem();
			if (unselectedFriend != null) {
				selectedListView.getSelectionModel().clearSelection();
				selected.remove(unselectedFriend);
				candidates.add(unselectedFriend);
			}
		});

		VBox vbox = new VBox(5);
		vbox.getChildren().addAll(sendRightButton, sendLeftButton);

		// make Chatroom from Selecte List
		// Send List to Server and make Chatroom if response Valid
		Button submitButton = new Button("Submit");
		submitButton.setPrefSize(btnWidth, btnHeight);
		submitButton.setOnAction((ActionEvent event) -> {
			// Send to Server if any Selected
			if (selected.size() > 0) {
				String temp = "";
				selectedFriendList.clear();
				selectedFriendList = selected.stream().collect(Collectors.toList());
				messageListSend = model.getMessageListSend();

				// Add key and User name and Selected List to Msg List
				messageList.clear();
				messageList.add("add_chatroom");
				messageList.add(model.getConnectedName());
				for (String selectedFriend : selectedFriendList) {
					temp = temp + selectedFriend;
					if (!selectedFriend.equals(selectedFriendList.get(selectedFriendList.size() - 1)))
						temp = temp + ", ";
				}
				messageList.add(temp);

				try {
					messageListSend.writeObject(messageList);
					messageListSend.flush();
					messageListSend.reset();
					messageList.clear();
					chatroomGrid.getChildren().clear();

					// Add ChatRoom to Grid
					addChatroomByResponse(selectedFriendList);
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		Button cancelButton = new Button("Cancel");
		cancelButton.setPrefSize(btnWidth, btnHeight);
		cancelButton.setOnAction((ActionEvent event) -> {
			addChatroomsToGrid(chatroomButtons);

		});

		chatroomGrid.add(vbox, 1, 1);
		chatroomGrid.add(submitButton, 0, 2);
		chatroomGrid.add(cancelButton, 2, 2);
	}

	/**
	 * Add Chatroom to Chatroom Grid by Response
	 * <br/> Add Chatroom if Response is Valid
	 * 
	 * @param selectedList
	 */
	void addChatroomByResponse(List<String> selectedList) {
		messageRcv = model.getMessageRcv();

		String line;
		String key = MsgKeys.ChatroomAddSuccess.getKey();

		try {
			if ((line = messageRcv.readLine()) != null) {
				if (line.substring(line.length() - key.length(), line.length()).equals(key)) {
					
					// Add new Chatroom to ChatroomList
					chatroomList.add((ArrayList<String>) selectedList);
					String buttonText = "";
					// Make Chatroom Btn Form
					for (String selected : selectedList) {
						buttonText = buttonText + selected;
						if (selected.equals(selectedList.get(selectedList.size() - 1)))
							continue;
						buttonText = buttonText + ", ";
					}
					ToggleButton newChatroom = new ToggleButton(buttonText);
					newChatroom.setPrefSize(2 * btnWidth, btnHeight);
					chatroomButtons.add(newChatroom);
					addChatroomsToGrid(chatroomButtons);
					// Save Modification
					model.setChatroomGrid(chatroomGrid);
					model.setChatroomsList(chatroomButtons);
				} else {
					AlertHandler.alert(ErrMsgs.AlreadyAdded.getMsg());
					addChatroomHandler(null);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		messageList.clear();

	}

	/**
	 * 1. Send Server Logout status
	 * <br/>2. Server Updates Connection status
	 * <br/>3. Change Grid to Login Grid
	 * 
	 * @param logoutEvent
	 */
	void logoutHandler(ActionEvent e) {
		try {
			messageList.clear();
			messageList.add("do_logout");
			messageList.add(model.getConnectedName());
			messageListSend.writeObject(messageList);
			messageListSend.flush();
			messageListSend.reset();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().addAll(model.getTitleLabel(), model.getLoginGrid());
	}

	/**
	 * Change to UserManager Grid 
	 * 
	 * @param showUserEvent
	 */
	void showUserHandler(ActionEvent e) {
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().add(model.getChatUserService());
	}
}
