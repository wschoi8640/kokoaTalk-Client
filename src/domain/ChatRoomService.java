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
 * 이 클래스는 사용자의 채팅방 목록을 구성하고 추가,삭제할 수 있도록 한다.
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

		// 메뉴 역할을 하는 Grid
		menuGrid = new GridPane();
		menuGrid.setAlignment(Pos.TOP_CENTER);
		menuGrid.setVgap(15);

		// 채팅방 목록을 보여줄 Grid
		chatroomGrid = new GridPane();
		chatroomGrid.setPrefSize(600, 550);
		chatroomGrid.setHgap(15);
		chatroomGrid.setVgap(15);

		// 추가,삭제 기능이 표시될 Grid
		funcGrid = new GridPane();
		funcGrid.setVgap(15);

		// 친구 목록 보기 버튼
		showUsrBtn = new Button("Show Friends");
		showUsrBtn.setPrefWidth(btnWidth);
		showUsrBtn.setPrefHeight(btnHeight);
		showUsrBtn.setOnAction(e -> showUserHandler(e));

		// 채팅방 보기 버튼
		showChatBtn = new Button("Show Chat Rooms");
		showChatBtn.setPrefWidth(btnWidth);
		showChatBtn.setPrefHeight(btnHeight);

		// 채팅방 열기 버튼
		openChatBtn = new Button("Open Chatroom");
		openChatBtn.setPrefSize(2 * btnWidth, btnHeight);
		openChatBtn.setOnAction(e -> openChatHandler(e));

		// 현재 사용자의 이름을 표시
		userNameLabel = new Label(model.getConnectedName());
		userNameLabel.setFont(new Font("Consolas", 30.0));
		userNameLabel.setPrefHeight(btnHeight);

		// 채팅방 추가 버튼
		addChatroomBtn = new Button("Add Chatroom");
		addChatroomBtn.setPrefSize(btnWidth, btnHeight);
		addChatroomBtn.setOnAction(e -> addChatroomHandler(e));

		// 채팅방 제거 버튼
		rmvChatroomBtn = new Button("Remove Chatroom");
		rmvChatroomBtn.setPrefSize(btnWidth, btnHeight);
		rmvChatroomBtn.setOnAction(e -> rmvChatroomHandler(e));

		// 로그아웃 버튼
		logoutBtn = new Button("Logout");
		logoutBtn.setPrefSize(2 * btnWidth, btnHeight);
		logoutBtn.setOnAction(e -> logoutHandler(e));

		menuGrid.add(showUsrBtn, 0, 0);
		menuGrid.add(showChatBtn, 1, 0);

		funcGrid.add(addChatroomBtn, 0, 0);
		funcGrid.add(rmvChatroomBtn, 1, 0);

		// 서버에서 채팅방 목록을 불러와 저장
		chatroomButtons = rcvChatrooms();
		if (chatroomButtons == null)
			chatroomButtons = new ArrayList<ToggleButton>();

		// 채팅방 목록을 Grid에 저장
		addChatroomsToGrid(chatroomButtons);

		// 채팅방 목록에 Grid 추가
		scrollPane = new ScrollPane(chatroomGrid);
		scrollPane.setStyle("-fx-background-color:transparent;");
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		// 창크기가 변경될 때마다 버튼이나,Grid의 크기도 변경될 수 있도록 설정
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
	 * 채팅방 삭제 버튼을 누르면 실행되는 메소드
	 * 
	 * 1. 삭제를 위해 선택된 버튼들로 리스트를 구성한다. 2. 서버에 요청과 함께 삭제 리스트도 전송한다. 3. 서버에서 응답이 돌아오면
	 * 클라이언트 상에서 삭제한다.
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
	 * 채팅방 열기 버튼을 누르면 실행되는 메소드
	 * 
	 * 1. 선택된 채팅방이 있는지 확인한다. 2. 선택된 채팅방을 생성하기 위한 클래스를 호출하고 이를 관리하기 위해 리스트에 추가한다.
	 * 
	 */
	void openChatHandler(ActionEvent e) {
		int count = 0;

		// 선택된 채팅방을 불러온다
		for (ToggleButton chatroom : chatroomButtons) {
			if (chatroom.isSelected()) {
				selectedChatroom = chatroom.getText();
				count++;
			}
		}

		// 채팅방이 제대로 선택되었는지 확인한다
		if (count == 0) {
			model.getLoginService().alertHandler("No Chatroom Chosen!");
		}
		if (count > 1) {
			model.getLoginService().alertHandler("Choose one Chatroom!");
		}

		// 이미 열려 있는 채팅방이라면 return하고 아닐시 채팅방을 띄운다.
		if (count == 1) {
			// 열려있는 채팅방인지 확인한다.
			for (Stage openedChatroom : openedChatroomList) {
				if (openedChatroom.getTitle().equals(selectedChatroom)) {
					model.getLoginService().alertHandler("Already Opened Chatroom!");
					return;
				}
			}

			// 채팅방을 띄우기 위해 클래스를 호출하고, 현재 사용자의 이름과 채팅방 이름을 전송한다.
			ChattingRoom chattingRoom = new ChattingRoom(model.getConnectedName(), selectedChatroom, model);

			Stage stage = new Stage();
			stage.setOnCloseRequest(e1 -> closeHandler(e1, stage));
			stage.setTitle(selectedChatroom);
			stage.setScene(new Scene(chattingRoom, 600, 700));
			stage.show();

			// 열려있는 채팅방에 현재 채팅방을 추가한다.
			openedChatroomList.add(stage);
		}
	}

	// 채팅방을 닫을시 실행되는 메소드
	void closeHandler(WindowEvent e, Stage stage) {
		openedChatroomList.remove(stage);
		stage.close();
	}

	/*
	 * 서버에서 채팅방 목록을 받아오는 메소드
	 * 
	 * 1. 채팅방 목록을 서버에 요청한다. 2. 채팅방 목록이 서버로 부터 돌아오면 이를 버튼으로 만들어 반환한다.
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

	// Grid에 채팅방을 추가해주는 메소드
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
	 * 채팅방 추가 버튼을 누르면 실행되는 메소드
	 * 
	 * 1. 친구 추가 양식을 구성하기 위해 친구목록을 가져온다 2. 두개의 리스트를 만들어 친구들 목록을 보여주고 선택할 수 있게 한다. 3.
	 * 다른 리스트에서는 현재 선택된 친구를 보여준다. 4. Submit 버튼과 Cancel 버튼을 만들고 Submit시 해당 친구들로 구성된
	 * 채팅방을 만들도록 서버에 요청한다. 5. 성공적으로 추가될 시 채팅방을 추가하고, 실패시 해당 내용을 사용자에게 보여준다.
	 *
	 */
	void addChatroomHandler(ActionEvent e) {
		openChatBtn.setVisible(false);
		friendsList.clear();
		chatroomGrid.getChildren().clear();
		chatroomGrid.getColumnConstraints().clear();

		// 친구 목록을 가져와 추가한다.
		for (ToggleButton friend : model.getFriendsList()) {
			friendsList.add(friend.getText());
		}
		chatroomGrid.setPadding(new Insets(5));
		chatroomGrid.setHgap(10);
		chatroomGrid.setVgap(10);

		// 두개의 리스트 뷰를 만들기 위해 Constraints를 추가한다
		ColumnConstraints column1 = new ColumnConstraints(150, 150, Double.MAX_VALUE);
		ColumnConstraints column2 = new ColumnConstraints(50);
		ColumnConstraints column3 = new ColumnConstraints(150, 150, Double.MAX_VALUE);

		column1.setHgrow(Priority.ALWAYS);
		column3.setHgrow(Priority.ALWAYS);
		chatroomGrid.getColumnConstraints().addAll(column1, column2, column3);

		// 현재 친구 목록 위에 표시될 Label
		Label candidatesLbl = new Label("Friends");
		GridPane.setHalignment(candidatesLbl, HPos.CENTER);
		chatroomGrid.add(candidatesLbl, 0, 0);

		// 선택된 친구목록 위에 표시될 Label
		Label selectedLbl = new Label("Selected");
		chatroomGrid.add(selectedLbl, 2, 0);
		GridPane.setHalignment(selectedLbl, HPos.CENTER);

		// 현재 친구의 리스트뷰를 생성한다
		final ObservableList<String> candidates = FXCollections.observableArrayList(friendsList);
		final ListView<String> candidatesListView = new ListView<>(candidates);
		chatroomGrid.add(candidatesListView, 0, 1);

		// 선택된 친구의 리스트뷰를 생성한다.
		final ObservableList<String> selected = FXCollections.observableArrayList();
		final ListView<String> selectedListView = new ListView<>(selected);
		chatroomGrid.add(selectedListView, 2, 1);

		// 현재 친구에서 친구를 추가하는 버튼
		Button sendRightButton = new Button(" > ");
		sendRightButton.setOnAction((ActionEvent event) -> {
			// 선택된 친구가 있을시 선택 친구 리스트 뷰에 친구를 추가한다.
			String selectedFriend = candidatesListView.getSelectionModel().getSelectedItem();
			if (selectedFriend != null) {
				candidatesListView.getSelectionModel().clearSelection();
				candidates.remove(selectedFriend);
				selected.add(selectedFriend);
			}
		});

		// 선택된 친구에서 친구를 삭제하는 버튼
		Button sendLeftButton = new Button(" < ");
		sendLeftButton.setOnAction((ActionEvent event) -> {

			// 선택된 친구가 있을시 선택 친구 리스트 뷰에서 친구를 삭제한다.
			String unselectedFriend = selectedListView.getSelectionModel().getSelectedItem();
			if (unselectedFriend != null) {
				selectedListView.getSelectionModel().clearSelection();
				selected.remove(unselectedFriend);
				candidates.add(unselectedFriend);
			}
		});

		VBox vbox = new VBox(5);
		vbox.getChildren().addAll(sendRightButton, sendLeftButton);

		// 선택된 친구로 구성된 채팅방을 추가하는 버튼
		// 서버에 전송하고, 돌아오는 응답을 바탕으로 사용자에게 표시한다.
		Button submitButton = new Button("Submit");
		submitButton.setPrefSize(btnWidth, btnHeight);
		submitButton.setOnAction((ActionEvent event) -> {
			if (selected.size() > 0) {
				String temp = "";
				selectedFriendList.clear();
				selectedFriendList = selected.stream().collect(Collectors.toList());
				messageListSend = model.getMessageListSend();

				// 채팅방 생성 요청과 현재 사용자를 메시지 리스트에 담아 전송
				messageList.clear();
				messageList.add("add_chatroom");
				messageList.add(model.getConnectedName());

				// 생성할 채팅방의 멤버를 리스트에 담음
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

					// 채팅방을 채팅방 Grid에 추가
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

	// 서버에서 성공 응답이 올 시에 해당 채팅방을 채팅방 리스트에 추가 해주는 메소드
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
	 * 로그아웃 버튼을 누르면 실행되는 메소드
	 * 
	 * 로그인 화면으로 돌아가되 상태 갱신을 위해 서버에 알려줌
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

	// 친구 목록을 불러오는 메소드
	void showUserHandler(ActionEvent e) {
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().add(model.getChatUserService());
	}
}
