package com.wschoi.kokoaClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

/**
 * �� Ŭ������ ������� ģ�� ����� �����ϰ� �߰�,������ �� �ֵ��� �Ѵ�.
 */
public class ChatUserSevice extends VBox {
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

	// �ֻ��� ���������� ����� ������ �ҷ��� �� ȭ�� ����
	public ChatUserSevice(Model echoModel) {
		this.model = echoModel;
		this.loginService = echoModel.getLoginService();
		this.sock = echoModel.getSock();
		echoModel.setChatUserService(this);
		initialize();
	}

	void initialize() {
		messageList = new ArrayList<String>();
		// ģ�� ��� ����Ʈ
		friendsButtonList = new ArrayList<ToggleButton>();

		// �޴� ������ �ϴ� Grid
		menuGrid = new GridPane();
		menuGrid.setVgap(15);

		// ģ�� ����� ǥ�õ� Grid
		friendGrid = new GridPane();
		friendGrid.setPrefSize(600, 550);
		friendGrid.setHgap(15);
		friendGrid.setVgap(15);

		// �߰� ���� ����� ǥ�õ� Grid
		funcGrid = new GridPane();
		funcGrid.setVgap(15);

		// ���� ������� �̸��� ȭ�鿡 ǥ��
		userNameLabel = new Label(model.getConnectedName());
		userNameLabel.setFont(new Font("Consolas", 30.0));
		userNameLabel.setPrefHeight(btnHeight);

		// ģ�� ��� ���� ��ư
		showUsrBtn = new Button("Show Friends");
		showUsrBtn.setPrefSize(btnWidth, btnHeight);

		// ä�ù� ��� ���� ��ư
		showChatBtn = new Button("Show Chat Rooms");
		showChatBtn.setPrefSize(btnWidth, btnHeight);
		showChatBtn.setOnAction(e -> showChatHandler(e));

		// ���� ���� �ҷ����� ��ư
		refreshBtn = new Button("Refresh Status");
		refreshBtn.setPrefSize(2 * btnWidth, btnHeight);
		refreshBtn.setOnAction(e -> refreshHandler(e));

		// ģ�� �߰� ��ư
		addFriendBtn = new Button("Add Friend");
		addFriendBtn.setPrefSize(btnWidth, btnHeight);
		addFriendBtn.setOnAction(e -> addFriendHandler(e));

		// ģ�� ���� ��ư
		rmvFriendBtn = new Button("Remove Friend");
		rmvFriendBtn.setPrefSize(btnWidth, btnHeight);
		rmvFriendBtn.setOnAction(e -> rmvFriendHandler(e));

		// �α׾ƿ� ��ư
		logoutBtn = new Button("Logout");
		logoutBtn.setPrefSize(2 * btnWidth, btnHeight);
		logoutBtn.setOnAction(e -> logoutHandler(e));

		menuGrid.add(showUsrBtn, 0, 0);
		menuGrid.add(showChatBtn, 1, 0);
		funcGrid.add(addFriendBtn, 0, 0);
		funcGrid.add(rmvFriendBtn, 1, 0);

		// �������� ģ������� �ҷ��� ����
		friendsButtonList = rcvFriendsList();
		if (friendsButtonList == null)
			friendsButtonList = new ArrayList<ToggleButton>();

		// ģ������� Grid�� �߰�
		addFriendsToGrid(friendsButtonList, curGridSize);

		// ģ����� Grid�� Scroll �߰�
		scrollPane = new ScrollPane(friendGrid);
		scrollPane.setStyle("-fx-background-color:transparent;");
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		// âũ�Ⱑ ����� ������ ��ư�̳�,Grid�� ũ�⵵ ����� �� �ֵ��� ����
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

	protected void setHeightProperty(Number newSceneHeight) {
		friendGrid.setPrefHeight(newSceneHeight.doubleValue() * 5.5 / 7);
		showUsrBtn.setPrefHeight(newSceneHeight.doubleValue() / 14);
		showChatBtn.setPrefHeight(newSceneHeight.doubleValue() / 14);
		userNameLabel.setPrefHeight(newSceneHeight.doubleValue() / 14);
	}

	protected void setWidthProperty(Number newSceneWidth, Number oldSceneWidth) {
		friendGrid.setPrefWidth(newSceneWidth.doubleValue());
		friendGrid.getChildren().clear();
		curGridSize = curGridSize * newSceneWidth.doubleValue() / oldSceneWidth.doubleValue();
		addFriendsToGrid(friendsButtonList, curGridSize);
		showUsrBtn.setPrefWidth(newSceneWidth.doubleValue() / 2);
		showChatBtn.setPrefWidth(newSceneWidth.doubleValue() / 2);
		addFriendBtn.setPrefWidth(newSceneWidth.doubleValue() / 2);
		rmvFriendBtn.setPrefWidth(newSceneWidth.doubleValue() / 2);
		logoutBtn.setPrefWidth(newSceneWidth.doubleValue());
		refreshBtn.setPrefWidth(newSceneWidth.doubleValue());
	}

	/**
	 * ģ������ ���ӻ��¸� �������ִ� �޼ҵ�
	 * 
	 * 1. ������ ���� ��û�� ģ�� ����� ������. 2. �������� ���ƿ� ģ�� ����� �޾ƿ´�. 3. �޾ƿ� ����� �������� �α��� �� ���
	 * �α׾ƿ� �� ����� ǥ���Ѵ�.
	 */
	void refreshHandler(ActionEvent e) {
		if (sock.isConnected()) {
			// ģ���� �ִ� ��� ���� ��û�� ����
			if (friendsButtonList.size() > 0) {
				messageListSend = model.getMessageListSend();
				messageList.clear();
				messageList.add("do_refresh");
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

				// �������� ���� ������ ���� ����
				if (messageList.get(0).equals("refresh_ok")) {
					// ���������� �����ϱ� ���� ����Ʈ
					List<ToggleButton> tempList = new ArrayList<ToggleButton>();
					// �α׾ƿ����� �����ϱ� ���� ����Ʈ
					List<ToggleButton> tempList2 = new ArrayList<ToggleButton>();
					tempList.addAll(friendsButtonList);
					tempList2.addAll(friendsButtonList);

					// ��ä ģ�� ��ϰ� ������ ģ�� ����� ����
					for (ToggleButton friendButton : friendsButtonList) {
						for (String connectedFriend : messageList) {
							if (connectedFriend.equals("refresh_ok"))
								continue;
							// ��ġ�� �� �� ����Ʈ�� ������ ����
							if (friendButton.getText().equals(connectedFriend)) {
								// ����Ʈ ����
								friendButton.setStyle("-fx-background-color: Yellow");
								// ��ư  ���ΰ�ħ
								tempList.remove(friendButton);
								tempList.add(friendButton);
								// ����Ʈ���� ��ġ�ϴ� ģ�� ����
								tempList2.remove(friendButton);
							}
						}
					}

					// �α׾ƿ� �� ģ���� ���,��ư�� �ʱ�ȭ
					for (ToggleButton friendButton : friendsButtonList) {
						if (tempList2.contains(friendButton)) {
							friendButton.setStyle(null);
							// ��ư ���ΰ�ħ
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

	// ä�ù� ����� �ҷ����� �޼ҵ�
	void showChatHandler(ActionEvent e) {
		// ä�ù� ����� �����ϴ� Ŭ������ ȣ�� ��,�߰�
		ChatRoomService chatRoomService = new ChatRoomService(model);
		model.setChatRoomService(chatRoomService);
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().add(chatRoomService);
		return;
	}

	// Grid�� ģ�� ��ư�� �߰����ִ� �޼ҵ�
	// ���� â ũ�⿡ ���� �迭 ����� �޶���
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
		// ���Ŀ� ����ϱ� ���ؼ� ����
		model.setFriendGrid(friendGrid);
		model.setFriendsList(friendsButtonList);
	}

	/**
	 * �������� ģ������� �޾ƿ��� �޼ҵ�
	 * 
	 * 1. ģ������� ������ ��û�Ѵ�. 2. ģ������� ������ ���� ���ƿ��� �̸� ��ư���� ����� ��ȯ�Ѵ�.
	 * 
	 */
	private List<ToggleButton> rcvFriendsList() {
		try {
			if (sock.isConnected()) {
				List<ToggleButton> temp_list = new ArrayList<ToggleButton>();

				messageListSend = model.getMessageListSend();
				messageListRcv = model.getMessageListRcv();

				// ��û �޽����� ���� ����� �̸��� ���� ����Ʈ�� ����
				messageList.add(0, "rcv_friends");
				messageList.add(1, model.getConnectedName());

				// ģ�� ��� ��û�� ����
				messageListSend.writeObject(messageList);
				messageListSend.flush();
				messageListSend.reset();

				messageList = (ArrayList<String>) messageListRcv.readObject();

				// ģ�� ����� ������ ��,��ư ����Ʈ���� ����� ��ȯ�Ѵ�.
				if (messageList.get(0).equals("rcv_ok")) {
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
	 * �α׾ƿ� ��ư�� ������ ����Ǵ� �޼ҵ�
	 * 
	 * �α��� ȭ������ ���ư��� ���� ������ ���� ������ �˷���
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
	 * ģ�� ���� ��ư�� ������ ����Ǵ� �޼ҵ�
	 * 
	 * 1. ������ ���� ���õ� ��ư��� ����Ʈ�� �����Ѵ�. 2. ������ ��û�� �Բ� ���� ����Ʈ�� �����Ѵ�. 3. �������� ������ ���ƿ���
	 * Ŭ���̾�Ʈ �󿡼� �����Ѵ�.
	 */
	void rmvFriendHandler(ActionEvent e) {
		if (!friendsButtonList.isEmpty()) {
			List<String> rmvFriendsList = new ArrayList<String>();
			for (ToggleButton friend : friendsButtonList) {
				if (friend.isSelected())
					rmvFriendsList.add(friend.getText());
			}
			if (!rmvFriendsList.isEmpty()) {
				try {
					if (sock.isConnected()) {
						messageListSend = model.getMessageListSend();
						messageRcv = model.getMessageRcv();

						messageList.clear();
						messageList.add(0, "rmv_friend");
						messageList.add(1, model.getConnectedName());

						for (String rmvFriend : rmvFriendsList) {
							messageList.add(rmvFriend);
						}

						messageListSend.writeObject(messageList);
						messageListSend.flush();
						messageListSend.reset();
						messageList.clear();

						if ((message = messageRcv.readLine()) != null) {
							if (message.equals("yrmv_ok") || message.equals("rmv_ok")) {
								List<ToggleButton> tempList = new ArrayList<ToggleButton>();
								tempList.addAll(friendsButtonList);
								for (ToggleButton myFriend : tempList) {
									for (String rmvFriend : rmvFriendsList) {
										if (myFriend.getText().equals(rmvFriend))
											friendsButtonList.remove(myFriend);
									}
								}
								friendGrid.getChildren().clear();
								addFriendsToGrid(friendsButtonList, curGridSize);
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
	 * ģ�� �߰� ��ư�� ������ ����Ǵ� �޼ҵ�
	 * 
	 * 1. �ʵ尡 ����ִ���,�ڱ��ڽ����� ���θ� üũ�Ѵ�. 2. �Էµ� ģ���� ��û�� �Բ� ������ �����Ѵ�. 3. �������� ���ƿ� ���信 �°�
	 * ����ڿ��� �����ش�. - ������ ģ����ư �߰� - �̹� ģ���̰ų�,�������� �ʴ� ������� ��� �޽����� ǥ��)
	 */
	void addFriendHandler(ActionEvent e) {
		// �߰��� ģ���� ID�� �Է� �ޱ�
		TextInputDialog dialog = new TextInputDialog("Insert Friend's ID");

		dialog.setTitle("Add New Friend");
		dialog.setHeaderText("Insert Friend's ID");
		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()) {
			String friend = result.get();

			// �ʵ尡 ����ִ��� Ȯ��
			if (friend.equals("Insert Friend's ID")) {
				loginService.alertHandler("Nothing Inserted!");
				addFriendHandler(e);
			}

			// ����� �ڽ����� Ȯ��
			if (friend.equals(model.getConnectedID())) {
				loginService.alertHandler("Cannot add Yourself!");
				addFriendHandler(e);
			} else {
				try {
					if (sock.isConnected()) {

						messageListSend = model.getMessageListSend();
						messageRcv = model.getMessageRcv();

						// ������ ��û�� �߰��� ģ�� ����
						messageList.add(0, "add_friend");
						messageList.add(1, model.getConnectedName());
						messageList.add(2, friend);

						messageListSend.writeObject(messageList);
						messageListSend.flush();
						messageListSend.reset();
						messageList.clear();

						if ((message = messageRcv.readLine()) != null) {
							// ģ�� �߰��� ������ ���
							if (message.substring(0, 4).equals("yadd") || message.substring(0, 3).equals("add")) {
								if (message.substring(0, 1).equals("y"))
									friend = message.substring(5, message.length());
								else
									friend = message.substring(4, message.length());

								// �ش� ģ���� ģ�� ��Ͽ� �߰�
								tmpFriend = new ToggleButton(friend);
								tmpFriend.setShape(new Circle(10));
								tmpFriend.setPrefSize(btnHeight * 2, btnHeight * 2);

								// ģ�� ��� Grid ����
								friendsButtonList.add(tmpFriend);
								friendGrid.add(tmpFriend, gridX, gridY);

								if (gridX > 4) {
									gridY++;
									gridX = 0;
								} else {
									gridX++;
								}
								return;
							}

							// �������� �ʴ� ������� ���
							if (message.equals("yno_such_user") || message.equals("no_such_user")) {
								loginService.alertHandler("No such User!");
								addFriendHandler(e);
							}

							// �̹� �߰��� ģ���� ���
							if (message.equals("yfriend_exists") || message.equals("friend_exists")) {
								loginService.alertHandler("Already Added!");
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