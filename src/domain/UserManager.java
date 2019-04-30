package domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import enums.ErrMsgs;
import enums.Settings;
import enums.MsgKeys;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Model;
import utils.AlertHandler;

/**
 * This class consists UserManage Panel and offers UserManage service 
 * 
 * @author wschoi8640
 * @version 1.0
 */
public class UserManager extends VBox {
	private Model model;
	private LoginService loginService;
	private Button refreshBtn;
	private Button showUsrBtn;
	private Button showChatBtn;
	private Button addFriendBtn;
	private Button rmvFriendBtn;
	private Button logoutBtn;
	private Label userNameLabel;
	private ToggleButton tmpFriend;
	private ScrollPane scrollPane;
	private GridPane menuGrid;
	private GridPane friendGrid;
	private GridPane funcGrid;
	public Stage parentStage;
	private String message;
	private int gridX = 0;
	private int gridY = 0;
	private double curGridSize = 4;
	private double btnHeight = 50;
	private double btnWidth = 300;
	private List<String> messageList;
	private List<ToggleButton> friendsButtonList;
	private Socket sock;
	private BufferedReader messageRcv;
	private ObjectOutputStream messageListSend;
	private ObjectInputStream messageListRcv;

	/**
	 * receive Primary Stage and Socket Object
	 * <br/> consist Panel
	 * @param model
	 */
	public UserManager(Model model) {
		this.model = model;
		this.loginService = model.getLoginService();
		this.sock = model.getSock();
		model.setChatUserService(this);
		initUserManageGrid();
	}

	
	/**
	 * consist user manage Grid
	 */
	void initUserManageGrid() {
		// message List to send Server 
		messageList = new ArrayList<String>();
		// user's Friend List
		friendsButtonList = new ArrayList<ToggleButton>();

		// Menu Grid
		menuGrid = new GridPane();
		menuGrid.setVgap(15);

		// Friend Grid
		friendGrid = new GridPane();
		friendGrid.setPrefSize(600, 550);
		friendGrid.setHgap(15);
		friendGrid.setVgap(15);

		// add / rmv Util Grid
		funcGrid = new GridPane();
		funcGrid.setVgap(15);

		// shows current User Name
		userNameLabel = new Label(model.getConnectedName());
		userNameLabel.setFont(new Font(Settings.Font.getSetting(), 30.0));
		userNameLabel.setPrefHeight(btnHeight);

		// btn for Changing Grid(show Friends)
		showUsrBtn = new Button("Show Friends");
		showUsrBtn.setPrefSize(btnWidth, btnHeight);

		// btn for Changing Grid(show Chat rooms)
		showChatBtn = new Button("Show Chat Rooms");
		showChatBtn.setPrefSize(btnWidth, btnHeight);
		showChatBtn.setOnAction(e -> showChatHandler(e));

		// btn for Update Connection Status
		refreshBtn = new Button("Refresh Status");
		refreshBtn.setPrefSize(2 * btnWidth, btnHeight);
		refreshBtn.setOnAction(e -> refreshHandler(e));

		// btn for Adding Friend
		addFriendBtn = new Button("Add Friend");
		addFriendBtn.setPrefSize(btnWidth, btnHeight);
		addFriendBtn.setOnAction(e -> addFriendHandler(e));

		// btn for Removing Friend
		rmvFriendBtn = new Button("Remove Friend");
		rmvFriendBtn.setPrefSize(btnWidth, btnHeight);
		rmvFriendBtn.setOnAction(e -> rmvFriendHandler(e));

		// btn for Logout
		logoutBtn = new Button("Logout");
		logoutBtn.setPrefSize(2 * btnWidth, btnHeight);
		logoutBtn.setOnAction(e -> LogoutService.logout(e,model));

		menuGrid.add(showUsrBtn, 0, 0);
		menuGrid.add(showChatBtn, 1, 0);
		funcGrid.add(addFriendBtn, 0, 0);
		funcGrid.add(rmvFriendBtn, 1, 0);

		// rcv FriendList from Server 
		friendsButtonList = rcvFriendsList();
		if (friendsButtonList == null)
			friendsButtonList = new ArrayList<ToggleButton>();

		// add Friends to FriendGrid
		addFriendsToGrid(friendsButtonList, curGridSize);

		// Add Scroll to Grid
		scrollPane = new ScrollPane(friendGrid);
		scrollPane.setStyle("-fx-background-color:transparent;");
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		// Set Auto Change by Panel size changes
		model.getLoginService().widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth,
					Number newSceneWidth) {
				setWidthProperty(newSceneWidth, oldSceneWidth);
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
		this.getChildren().addAll(menuGrid, userNameLabel, refreshBtn, friendGrid, scrollPane, funcGrid, logoutBtn);

	}

	/**
	 * set Height of inner Children by Panel Height Change
	 * 
	 * @param panelHeight
	 */
	protected void setHeightProperty(Number panelHeight) {
		friendGrid.setPrefHeight(panelHeight.doubleValue() * 5.5 / 7);
		showUsrBtn.setPrefHeight(panelHeight.doubleValue() / 14);
		showChatBtn.setPrefHeight(panelHeight.doubleValue() / 14);
		userNameLabel.setPrefHeight(panelHeight.doubleValue() / 14);
	}

	/**
	 * set Width of inner Children by Panel Width Change
	 * 
	 * @param panelWidth
	 */
	protected void setWidthProperty(Number newPanelWidth, Number oldPanelWidth) {
		friendGrid.setPrefWidth(newPanelWidth.doubleValue());
		friendGrid.getChildren().clear();
		curGridSize = curGridSize * newPanelWidth.doubleValue() / oldPanelWidth.doubleValue();
		addFriendsToGrid(friendsButtonList, curGridSize);
		showUsrBtn.setPrefWidth(newPanelWidth.doubleValue() / 2);
		showChatBtn.setPrefWidth(newPanelWidth.doubleValue() / 2);
		addFriendBtn.setPrefWidth(newPanelWidth.doubleValue() / 2);
		rmvFriendBtn.setPrefWidth(newPanelWidth.doubleValue() / 2);
		logoutBtn.setPrefWidth(newPanelWidth.doubleValue());
		refreshBtn.setPrefWidth(newPanelWidth.doubleValue());
	}

	/**
	 * Updates Friends Connection Status
	 * <br/>
	 * <br/>1. Request Server new Connection Status 
	 * <br/>2. Receive Current Friend Status 
	 * <br/>3. Update Grid by new Friend Status
	 * 
	 * @param refreshAction
	 */
	void refreshHandler(ActionEvent e) {
		if (sock.isConnected()) {
			// request when user has friend  
			if (friendsButtonList.size() > 0) {
				messageListSend = model.getMessageListSend();
				messageList.clear();
				messageList.add(MsgKeys.RefreshRequest.getKey());
				try {
					messageListSend.writeObject(messageList);
					messageListSend.flush();
					messageListSend.reset();
					messageList = (ArrayList<String>) messageListRcv.readObject();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				// Request successful
				if (messageList.get(0).equals(MsgKeys.RefreshSuccess.getKey())) {
					// for Merge Connected Friends
					List<ToggleButton> tempList = new ArrayList<ToggleButton>();
					// for Merge Not Connected Friends
					List<ToggleButton> tempList2 = new ArrayList<ToggleButton>();
					tempList.addAll(friendsButtonList);
					tempList2.addAll(friendsButtonList);

					// Merge Connected Friends
					for (ToggleButton friendButton : friendsButtonList) {
						for (String connectedFriend : messageList) {
							if (connectedFriend.equals(MsgKeys.RefreshSuccess.getKey()))
								continue;
							// Check if Friend exists
							if (friendButton.getText().equals(connectedFriend)) {
								// Update Color
								friendButton.setStyle(Settings.ConnectedFriendColor.getSetting());
								// Apply new Style
								tempList.remove(friendButton);
								tempList.add(friendButton);
								// Rmv Connected Friend
								tempList2.remove(friendButton);
							}
						}
					}

					// Update Color of Disconnected Friends
					for (ToggleButton friendButton : friendsButtonList) {
						if (tempList2.contains(friendButton)) {
							// Update Color
							friendButton.setStyle(null);
							// Apply new Style
							tempList.remove(friendButton);
							tempList.add(friendButton);
						}

					}
					friendGrid.getChildren().clear();
					addFriendsToGrid(tempList, curGridSize);
					friendsButtonList.clear();
					friendsButtonList.addAll(tempList);
				}
			}
		}
	}
	/**
	 * Change to ChatRoomManager Grid 
	 * 
	 * @param showChatEvent
	 */
	void showChatHandler(ActionEvent e) {
		// Add ChatRoomService Panel to Primary Stage and save it for later
		ChatRoomManager chatRoomService = new ChatRoomManager(model);
		model.setChatRoomService(chatRoomService);
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().add(chatRoomService);
	}

	/**
	 * Add Friends to Friend Grid
	 * <br/>grid size changes col length
	 * @param friendButtons
	 * @param gridSize
	 */
	private void addFriendsToGrid(List<ToggleButton> friendButtons, double gridSize) {
		gridX = 0;
		gridY = 0;
		friendGrid.getChildren().clear();
		if (friendButtons != null) {
			for (ToggleButton friendButton : friendButtons) {
				friendGrid.add(friendButton, gridX, gridY);
				if (gridX >= gridSize) {
					gridY++;
					gridX = 0;
				} else {
					gridX++;
				}
			}
		}
		// save modifications
		model.setFriendGrid(friendGrid);
		model.setFriendsList(friendsButtonList);
	}

	/**
	 * Receive user Friends from Server
	 * <br/>
	 * <br/>1. Request Server User Friend List.
	 * <br/>2. Change List into Buttons and return.
	 * 
	 * @return List<Friends>
	 */
	private List<ToggleButton> rcvFriendsList() {
		try {
			if (sock.isConnected()) {
				List<ToggleButton> temp_list = new ArrayList<ToggleButton>();

				messageListSend = model.getMessageListSend();
				messageListRcv = model.getMessageListRcv();

				// Add Key and UserName to Msg List
				messageList.add(0, MsgKeys.ReceiveFriends.getKey());
				messageList.add(1, model.getConnectedName());

				// Send Request to Server
				messageListSend.writeObject(messageList);
				messageListSend.flush();
				messageListSend.reset();

				// rcv Server response
				messageList = (ArrayList<String>) messageListRcv.readObject();

				// make FriendBtnList from response
				if (messageList.get(0).equals(MsgKeys.ReceiveSuccess.getKey())) {
					for (int i = 1; i < messageList.size(); i++) {
						tmpFriend = new ToggleButton(messageList.get(i));
						tmpFriend.setShape(new Circle(10));
						tmpFriend.setPrefSize(btnHeight * 2, btnHeight * 2);

						temp_list.add(tmpFriend);
					}
					return temp_list;
				}
				messageList.clear();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return null;
	}


	/**
	 * Removes Selected Friends from Grid
	 * <br/>
	 * <br/>1. Make List from selected Friends. 
	 * <br/>2. Send Server key and List. 
	 * <br/>3. Remove Selected Friends by Server response.
	 * 
	 * @param removeEvent
	 */
	void rmvFriendHandler(ActionEvent e) {
		// Remove when having any Friend
		if (!friendsButtonList.isEmpty()) {
			List<String> rmvFriendsList = new ArrayList<String>();
			// Add Selected Friends to Remove List
			for (ToggleButton friend : friendsButtonList) {
				if (friend.isSelected())
					rmvFriendsList.add(friend.getText());
			}
			
			// Send Server when having any Selected Friends
			if (!rmvFriendsList.isEmpty()) {
				try {
					if (sock.isConnected()) {
						messageListSend = model.getMessageListSend();

						// Add key and UserName and List to Msg List 
						messageList.clear();
						messageList.add(0, MsgKeys.RemoveRequest.getKey());
						messageList.add(1, model.getConnectedName());
						for (String rmvFriend : rmvFriendsList) {
							messageList.add(rmvFriend);
						}

						// Send Server Msg
						messageListSend.writeObject(messageList);
						messageListSend.flush();
						messageListSend.reset();
						messageList.clear();

						// Receive Server Response
						messageRcv = model.getMessageRcv();
						if ((message = messageRcv.readLine()) != null) {
							// Remove Successful
							if (message.equals(MsgKeys.RemoveSuccess.getKey()) || message.equals("yrmv_ok")) {
								List<ToggleButton> tempList = new ArrayList<ToggleButton>();
								tempList.addAll(friendsButtonList);
								// Remove Selected Friends from Friend List
								for (ToggleButton myFriend : tempList) {
									for (String rmvFriend : rmvFriendsList) {
										if (myFriend.getText().equals(rmvFriend))
											friendsButtonList.remove(myFriend);
									}
								}
								// Update Grid
								friendGrid.getChildren().clear();
								addFriendsToGrid(friendsButtonList, curGridSize);
								// Save Modification
								model.setFriendGrid(friendGrid);
								model.setFriendsList(friendsButtonList);
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
	 * Add new Friend to Grid
	 * <br/>
	 * <br/>1. Read ID of new Friend
	 * <br/>2. Check if ID is User's and if field is blank 
	 * <br/>3. Send Server Request to add Friend
	 * <br/>4. Receive Server Response 
	 * <br/>5. Show Result (Success/Already added/No Such ID)
	 * 
	 * @param addEvent
	 */
	void addFriendHandler(ActionEvent e) {
		// Read Friend ID
		TextInputDialog dialog = new TextInputDialog("Insert Friend's ID");

		dialog.setTitle("Add New Friend");
		dialog.setHeaderText("Insert Friend's ID");
		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()) {
			String friend = result.get();

			// Check if Field is blank
			if (friend.equals("Insert Friend's ID")) {
				AlertHandler.alert(ErrMsgs.NothingInserted.getMsg());
				addFriendHandler(e);
				return;
			}

			// Check if ID is User's
			if (friend.equals(model.getConnectedID())) {
				AlertHandler.alert(ErrMsgs.AddMySelf.getMsg());
				addFriendHandler(e);
				return;
			} else {
				try {
					if (sock.isConnected()) {

						messageListSend = model.getMessageListSend();
						messageRcv = model.getMessageRcv();

						// Add Key and Friend to Msg List
						messageList.add(0, MsgKeys.AddRequest.getKey());
						messageList.add(1, model.getConnectedName());
						messageList.add(2, friend);

						messageListSend.writeObject(messageList);
						messageListSend.flush();
						messageListSend.reset();
						messageList.clear();

						if ((message = messageRcv.readLine()) != null) {
							// Add Friend Successful
							if (message.substring(0, 3).equals(MsgKeys.AddSuccess.getKey()) || message.substring(0, 4).equals("yadd")) {
								if (message.substring(0, 1).equals("y"))
									friend = message.substring(5, message.length());
								else
									friend = message.substring(4, message.length());

								// make new Friend Button
								tmpFriend = new ToggleButton(friend);
								tmpFriend.setShape(new Circle(10));
								tmpFriend.setPrefSize(btnHeight * 2, btnHeight * 2);

								// Add Friend to Friend Grid
								friendsButtonList.add(tmpFriend);
								friendGrid.add(tmpFriend, gridX, gridY);

								if (gridX > curGridSize) {
									gridY++;
									gridX = 0;
								} else {
									gridX++;
								}
								return;
							}

							// When such id not Exist
							if (message.equals(MsgKeys.AddFailByID.getKey()) || message.equals("yno_such_user")) {
								AlertHandler.alert(ErrMsgs.NoSuchUser.getMsg());
								addFriendHandler(e);
							}

							// When Already added Friend
							if (message.equals(MsgKeys.AddFailByDupli.getKey()) || message.equals("yfriend_exists")) {
								AlertHandler.alert(ErrMsgs.AlreadyAdded.getMsg());
								addFriendHandler(e);
							}
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}