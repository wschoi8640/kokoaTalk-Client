package domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

/*
 * �� Ŭ������ ������� ä�ù� ����� �����ϰ� �߰�,������ �� �ֵ��� �Ѵ�.
 */
public class ChatRoomService extends VBox {
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

	public ChatRoomService(Model model) {
		this.model = model;
		this.sock = model.getSock();
		initialize();
	}

	void initialize() {
		messageList = new ArrayList<String>();
		friendsList = new ArrayList<String>();
		chatroomList = new ArrayList<ArrayList<String>>();
		openedChatroomList = new ArrayList<Stage>();
		chatroomButtons = new ArrayList<ToggleButton>();
		selectedFriendList = new ArrayList<String>();

		// �޴� ������ �ϴ� Grid
		menuGrid = new GridPane();
		menuGrid.setAlignment(Pos.TOP_CENTER);
		menuGrid.setVgap(15);

		// ä�ù� ����� ������ Grid
		chatroomGrid = new GridPane();
		chatroomGrid.setPrefSize(600, 550);
		chatroomGrid.setHgap(15);
		chatroomGrid.setVgap(15);

		// �߰�,���� ����� ǥ�õ� Grid
		funcGrid = new GridPane();
		funcGrid.setVgap(15);

		// ģ�� ��� ���� ��ư
		showUsrBtn = new Button("Show Friends");
		showUsrBtn.setPrefWidth(btnWidth);
		showUsrBtn.setPrefHeight(btnHeight);
		showUsrBtn.setOnAction(e -> showUserHandler(e));

		// ä�ù� ���� ��ư
		showChatBtn = new Button("Show Chat Rooms");
		showChatBtn.setPrefWidth(btnWidth);
		showChatBtn.setPrefHeight(btnHeight);

		// ä�ù� ���� ��ư
		openChatBtn = new Button("Open Chatroom");
		openChatBtn.setPrefSize(2 * btnWidth, btnHeight);
		openChatBtn.setOnAction(e -> openChatHandler(e));

		// ���� ������� �̸��� ǥ��
		userNameLabel = new Label(model.getConnectedName());
		userNameLabel.setFont(new Font("Consolas", 30.0));
		userNameLabel.setPrefHeight(btnHeight);

		// ä�ù� �߰� ��ư
		addChatroomBtn = new Button("Add Chatroom");
		addChatroomBtn.setPrefSize(btnWidth, btnHeight);
		addChatroomBtn.setOnAction(e -> addChatroomHandler(e));

		// ä�ù� ���� ��ư
		rmvChatroomBtn = new Button("Remove Chatroom");
		rmvChatroomBtn.setPrefSize(btnWidth, btnHeight);
		rmvChatroomBtn.setOnAction(e -> rmvChatroomHandler(e));

		// �α׾ƿ� ��ư
		logoutBtn = new Button("Logout");
		logoutBtn.setPrefSize(2 * btnWidth, btnHeight);
		logoutBtn.setOnAction(e -> logoutHandler(e));

		menuGrid.add(showUsrBtn, 0, 0);
		menuGrid.add(showChatBtn, 1, 0);

		funcGrid.add(addChatroomBtn, 0, 0);
		funcGrid.add(rmvChatroomBtn, 1, 0);

		// �������� ä�ù� ����� �ҷ��� ����
		chatroomButtons = rcvChatrooms();
		if (chatroomButtons == null)
			chatroomButtons = new ArrayList<ToggleButton>();

		// ä�ù� ����� Grid�� ����
		addChatroomsToGrid(chatroomButtons);

		// ä�ù� ��Ͽ� Grid �߰�
		scrollPane = new ScrollPane(chatroomGrid);
		scrollPane.setStyle("-fx-background-color:transparent;");
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		// âũ�Ⱑ ����� ������ ��ư�̳�,Grid�� ũ�⵵ ����� �� �ֵ��� ����
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

	/*
	 * ä�ù� ���� ��ư�� ������ ����Ǵ� �޼ҵ�
	 * 
	 * 1. ������ ���� ���õ� ��ư��� ����Ʈ�� �����Ѵ�. 2. ������ ��û�� �Բ� ���� ����Ʈ�� �����Ѵ�. 3. �������� ������ ���ƿ���
	 * Ŭ���̾�Ʈ �󿡼� �����Ѵ�.
	 */
	void rmvChatroomHandler(ActionEvent e) {
		if (!chatroomButtons.isEmpty()) {
			List<String> rmvChatroomsList = new ArrayList<String>();
			for (ToggleButton chatroom : chatroomButtons) {
				if (chatroom.isSelected())
					rmvChatroomsList.add(chatroom.getText());
			}
			if (!rmvChatroomsList.isEmpty()) {
				try {
					if (sock.isConnected()) {
						messageListSend = model.getMessageListSend();
						messageRcv = model.getMessageRcv();

						messageList.clear();
						messageList.add(0, "rmv_chatroom");
						messageList.add(1, model.getConnectedName());

						for (String rmvChatroom : rmvChatroomsList) {
							messageList.add(rmvChatroom);
						}

						messageListSend.writeObject(messageList);
						messageListSend.flush();
						messageListSend.reset();
						messageList.clear();

						String message;
						if ((message = messageRcv.readLine()) != null) {
							if (message.equals("yrmv_ok") || message.equals("rmv_ok")) {
								List<ToggleButton> tempList = new ArrayList<ToggleButton>();
								tempList.addAll(chatroomButtons);
								for (ToggleButton myChatrooms : tempList) {
									for (String rmvChatroom : rmvChatroomsList) {
										if (myChatrooms.getText().equals(rmvChatroom))
											chatroomButtons.remove(myChatrooms);
									}
								}
								chatroomGrid.getChildren().clear();
								addChatroomsToGrid(chatroomButtons);
							}
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		}
	}

	/*
	 * ä�ù� ���� ��ư�� ������ ����Ǵ� �޼ҵ�
	 * 
	 * 1. ���õ� ä�ù��� �ִ��� Ȯ���Ѵ�. 2. ���õ� ä�ù��� �����ϱ� ���� Ŭ������ ȣ���ϰ� �̸� �����ϱ� ���� ����Ʈ�� �߰��Ѵ�.
	 * 
	 */
	void openChatHandler(ActionEvent e) {
		int count = 0;

		// ���õ� ä�ù��� �ҷ��´�
		for (ToggleButton chatroom : chatroomButtons) {
			if (chatroom.isSelected()) {
				selectedChatroom = chatroom.getText();
				count++;
			}
		}

		// ä�ù��� ����� ���õǾ����� Ȯ���Ѵ�
		if (count == 0) {
			model.getLoginService().alertHandler("No Chatroom Chosen!");
		}
		if (count > 1) {
			model.getLoginService().alertHandler("Choose one Chatroom!");
		}

		// �̹� ���� �ִ� ä�ù��̶�� return�ϰ� �ƴҽ� ä�ù��� ����.
		if (count == 1) {
			// �����ִ� ä�ù����� Ȯ���Ѵ�.
			for (Stage openedChatroom : openedChatroomList) {
				if (openedChatroom.getTitle().equals(selectedChatroom)) {
					model.getLoginService().alertHandler("Already Opened Chatroom!");
					return;
				}
			}

			// ä�ù��� ���� ���� Ŭ������ ȣ���ϰ�, ���� ������� �̸��� ä�ù� �̸��� �����Ѵ�.
			ChattingRoom chattingRoom = new ChattingRoom(model.getConnectedName(), selectedChatroom, model);

			Stage stage = new Stage();
			stage.setOnCloseRequest(e1 -> closeHandler(e1, stage));
			stage.setTitle(selectedChatroom);
			stage.setScene(new Scene(chattingRoom, 600, 700));
			stage.show();

			// �����ִ� ä�ù濡 ���� ä�ù��� �߰��Ѵ�.
			openedChatroomList.add(stage);
		}
	}

	// ä�ù��� ������ ����Ǵ� �޼ҵ�
	void closeHandler(WindowEvent e, Stage stage) {
		openedChatroomList.remove(stage);
		stage.close();
	}

	/*
	 * �������� ä�ù� ����� �޾ƿ��� �޼ҵ�
	 * 
	 * 1. ä�ù� ����� ������ ��û�Ѵ�. 2. ä�ù� ����� ������ ���� ���ƿ��� �̸� ��ư���� ����� ��ȯ�Ѵ�.
	 * 
	 */
	private List<ToggleButton> rcvChatrooms() {
		if (sock.isConnected()) {
			List<ToggleButton> temp_list = new ArrayList<ToggleButton>();

			messageListSend = model.getMessageListSend();
			messageListRcv = model.getMessageListRcv();

			messageList.add(0, "rcv_chatrooms");
			messageList.add(1, model.getConnectedName());

			try {
				messageListSend.writeObject(messageList);
				messageListSend.flush();
				messageListSend.reset();
				messageList.clear();
				messageList = (ArrayList<String>) messageListRcv.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (messageList.get(0).equals("send_chatrooms")) {
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

	// Grid�� ä�ù��� �߰����ִ� �޼ҵ�
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
	}

	/*
	 * ä�ù� �߰� ��ư�� ������ ����Ǵ� �޼ҵ�
	 * 
	 * 1. ģ�� �߰� ����� �����ϱ� ���� ģ������� �����´� 2. �ΰ��� ����Ʈ�� ����� ģ���� ����� �����ְ� ������ �� �ְ� �Ѵ�. 3.
	 * �ٸ� ����Ʈ������ ���� ���õ� ģ���� �����ش�. 4. Submit ��ư�� Cancel ��ư�� ����� Submit�� �ش� ģ����� ������
	 * ä�ù��� ���鵵�� ������ ��û�Ѵ�. 5. ���������� �߰��� �� ä�ù��� �߰��ϰ�, ���н� �ش� ������ ����ڿ��� �����ش�.
	 *
	 */
	void addChatroomHandler(ActionEvent e) {
		openChatBtn.setVisible(false);
		friendsList.clear();
		chatroomGrid.getChildren().clear();
		chatroomGrid.getColumnConstraints().clear();

		// ģ�� ����� ������ �߰��Ѵ�.
		for (ToggleButton friend : model.getFriendsList()) {
			friendsList.add(friend.getText());
		}
		chatroomGrid.setPadding(new Insets(5));
		chatroomGrid.setHgap(10);
		chatroomGrid.setVgap(10);

		// �ΰ��� ����Ʈ �並 ����� ���� Constraints�� �߰��Ѵ�
		ColumnConstraints column1 = new ColumnConstraints(150, 150, Double.MAX_VALUE);
		ColumnConstraints column2 = new ColumnConstraints(50);
		ColumnConstraints column3 = new ColumnConstraints(150, 150, Double.MAX_VALUE);

		column1.setHgrow(Priority.ALWAYS);
		column3.setHgrow(Priority.ALWAYS);
		chatroomGrid.getColumnConstraints().addAll(column1, column2, column3);

		// ���� ģ�� ��� ���� ǥ�õ� Label
		Label candidatesLbl = new Label("Friends");
		GridPane.setHalignment(candidatesLbl, HPos.CENTER);
		chatroomGrid.add(candidatesLbl, 0, 0);

		// ���õ� ģ����� ���� ǥ�õ� Label
		Label selectedLbl = new Label("Selected");
		chatroomGrid.add(selectedLbl, 2, 0);
		GridPane.setHalignment(selectedLbl, HPos.CENTER);

		// ���� ģ���� ����Ʈ�並 �����Ѵ�
		final ObservableList<String> candidates = FXCollections.observableArrayList(friendsList);
		final ListView<String> candidatesListView = new ListView<>(candidates);
		chatroomGrid.add(candidatesListView, 0, 1);

		// ���õ� ģ���� ����Ʈ�並 �����Ѵ�.
		final ObservableList<String> selected = FXCollections.observableArrayList();
		final ListView<String> selectedListView = new ListView<>(selected);
		chatroomGrid.add(selectedListView, 2, 1);

		// ���� ģ������ ģ���� �߰��ϴ� ��ư
		Button sendRightButton = new Button(" > ");
		sendRightButton.setOnAction((ActionEvent event) -> {
			// ���õ� ģ���� ������ ���� ģ�� ����Ʈ �信 ģ���� �߰��Ѵ�.
			String selectedFriend = candidatesListView.getSelectionModel().getSelectedItem();
			if (selectedFriend != null) {
				candidatesListView.getSelectionModel().clearSelection();
				candidates.remove(selectedFriend);
				selected.add(selectedFriend);
			}
		});

		// ���õ� ģ������ ģ���� �����ϴ� ��ư
		Button sendLeftButton = new Button(" < ");
		sendLeftButton.setOnAction((ActionEvent event) -> {

			// ���õ� ģ���� ������ ���� ģ�� ����Ʈ �信�� ģ���� �����Ѵ�.
			String unselectedFriend = selectedListView.getSelectionModel().getSelectedItem();
			if (unselectedFriend != null) {
				selectedListView.getSelectionModel().clearSelection();
				selected.remove(unselectedFriend);
				candidates.add(unselectedFriend);
			}
		});

		VBox vbox = new VBox(5);
		vbox.getChildren().addAll(sendRightButton, sendLeftButton);

		// ���õ� ģ���� ������ ä�ù��� �߰��ϴ� ��ư
		// ������ �����ϰ�, ���ƿ��� ������ �������� ����ڿ��� ǥ���Ѵ�.
		Button submitButton = new Button("Submit");
		submitButton.setPrefSize(btnWidth, btnHeight);
		submitButton.setOnAction((ActionEvent event) -> {
			if (selected.size() > 0) {
				String temp = "";
				selectedFriendList.clear();
				selectedFriendList = selected.stream().collect(Collectors.toList());
				messageListSend = model.getMessageListSend();

				// ä�ù� ���� ��û�� ���� ����ڸ� �޽��� ����Ʈ�� ��� ����
				messageList.clear();
				messageList.add("add_chatroom");
				messageList.add(model.getConnectedName());

				// ������ ä�ù��� ����� ����Ʈ�� ����
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

					// ä�ù��� ä�ù� Grid�� �߰�
					addChatroomDataHandle(selectedFriendList);
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

	// �������� ���� ������ �� �ÿ� �ش� ä�ù��� ä�ù� ����Ʈ�� �߰� ���ִ� �޼ҵ�
	void addChatroomDataHandle(List<String> selectedList) {
		messageRcv = model.getMessageRcv();

		String line;
		String key = "chatroom_added";

		try {
			if ((line = messageRcv.readLine()) != null) {
				if (line.substring(line.length() - key.length(), line.length()).equals("chatroom_added")) {
					chatroomList.add((ArrayList<String>) selectedList);
					String buttonText = "";
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
				} else {
					model.getLoginService().alertHandler("Already Added!");
					addChatroomHandler(null);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		messageList.clear();

	}

	/*
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

	// ģ�� ����� �ҷ����� �޼ҵ�
	void showUserHandler(ActionEvent e) {
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().add(model.getChatUserService());
	}
}
