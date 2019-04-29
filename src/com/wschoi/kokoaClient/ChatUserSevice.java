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
 * 이 클래스는 사용자의 친구 목록을 구성하고 추가,삭제할 수 있도록 한다.
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

	// 최상위 스테이지와 연결된 소켓을 불러온 후 화면 구성
	public ChatUserSevice(Model echoModel) {
		this.model = echoModel;
		this.loginService = echoModel.getLoginService();
		this.sock = echoModel.getSock();
		echoModel.setChatUserService(this);
		initialize();
	}

	void initialize() {
		messageList = new ArrayList<String>();
		// 친구 목록 리스트
		friendsButtonList = new ArrayList<ToggleButton>();

		// 메뉴 역할을 하는 Grid
		menuGrid = new GridPane();
		menuGrid.setVgap(15);

		// 친구 목록이 표시될 Grid
		friendGrid = new GridPane();
		friendGrid.setPrefSize(600, 550);
		friendGrid.setHgap(15);
		friendGrid.setVgap(15);

		// 추가 삭제 기능이 표시될 Grid
		funcGrid = new GridPane();
		funcGrid.setVgap(15);

		// 현재 사용자의 이름을 화면에 표시
		userNameLabel = new Label(model.getConnectedName());
		userNameLabel.setFont(new Font("Consolas", 30.0));
		userNameLabel.setPrefHeight(btnHeight);

		// 친구 목록 보기 버튼
		showUsrBtn = new Button("Show Friends");
		showUsrBtn.setPrefSize(btnWidth, btnHeight);

		// 채팅방 목록 보기 버튼
		showChatBtn = new Button("Show Chat Rooms");
		showChatBtn.setPrefSize(btnWidth, btnHeight);
		showChatBtn.setOnAction(e -> showChatHandler(e));

		// 접속 상태 불러오기 버튼
		refreshBtn = new Button("Refresh Status");
		refreshBtn.setPrefSize(2 * btnWidth, btnHeight);
		refreshBtn.setOnAction(e -> refreshHandler(e));

		// 친구 추가 버튼
		addFriendBtn = new Button("Add Friend");
		addFriendBtn.setPrefSize(btnWidth, btnHeight);
		addFriendBtn.setOnAction(e -> addFriendHandler(e));

		// 친구 삭제 버튼
		rmvFriendBtn = new Button("Remove Friend");
		rmvFriendBtn.setPrefSize(btnWidth, btnHeight);
		rmvFriendBtn.setOnAction(e -> rmvFriendHandler(e));

		// 로그아웃 버튼
		logoutBtn = new Button("Logout");
		logoutBtn.setPrefSize(2 * btnWidth, btnHeight);
		logoutBtn.setOnAction(e -> logoutHandler(e));

		menuGrid.add(showUsrBtn, 0, 0);
		menuGrid.add(showChatBtn, 1, 0);
		funcGrid.add(addFriendBtn, 0, 0);
		funcGrid.add(rmvFriendBtn, 1, 0);

		// 서버에서 친구목록을 불러와 저장
		friendsButtonList = rcvFriendsList();
		if (friendsButtonList == null)
			friendsButtonList = new ArrayList<ToggleButton>();

		// 친구목록을 Grid에 추가
		addFriendsToGrid(friendsButtonList, curGridSize);

		// 친구목록 Grid에 Scroll 추가
		scrollPane = new ScrollPane(friendGrid);
		scrollPane.setStyle("-fx-background-color:transparent;");
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		// 창크기가 변경될 때마다 버튼이나,Grid의 크기도 변경될 수 있도록 설정
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
	 * 친구들의 접속상태를 갱신해주는 메소드
	 * 
	 * 1. 서버에 갱신 요청과 친구 목록을 보낸다. 2. 서버에서 돌아온 친구 목록을 받아온다. 3. 받아온 목록을 바탕으로 로그인 한 사람
	 * 로그아웃 한 사람을 표시한다.
	 */
	void refreshHandler(ActionEvent e) {
		if (sock.isConnected()) {
			// 친구가 있는 경우 갱신 요청은 보냄
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

				// 정상적인 응답 도착시 상태 갱신
				if (messageList.get(0).equals("refresh_ok")) {
					// 접속중으로 변경하기 위한 리스트
					List<ToggleButton> tempList = new ArrayList<ToggleButton>();
					// 로그아웃으로 변경하기 위한 리스트
					List<ToggleButton> tempList2 = new ArrayList<ToggleButton>();
					tempList.addAll(friendsButtonList);
					tempList2.addAll(friendsButtonList);

					// 현채 친구 목록과 도착한 친구 목록을 비교함
					for (ToggleButton friendButton : friendsButtonList) {
						for (String connectedFriend : messageList) {
							if (connectedFriend.equals("refresh_ok"))
								continue;
							// 일치할 시 두 리스트의 내용을 변경
							if (friendButton.getText().equals(connectedFriend)) {
								// 리스트 갱신
								friendButton.setStyle("-fx-background-color: Yellow");
								// 버튼  새로고침
								tempList.remove(friendButton);
								tempList.add(friendButton);
								// 리스트에서 일치하는 친구 삭제
								tempList2.remove(friendButton);
							}
						}
					}

					// 로그아웃 한 친구인 경우,버튼색 초기화
					for (ToggleButton friendButton : friendsButtonList) {
						if (tempList2.contains(friendButton)) {
							friendButton.setStyle(null);
							// 버튼 새로고침
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

	// 채팅방 목록을 불러오는 메소드
	void showChatHandler(ActionEvent e) {
		// 채팅방 목록을 구성하는 클래스를 호출 후,추가
		ChatRoomService chatRoomService = new ChatRoomService(model);
		model.setChatRoomService(chatRoomService);
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().add(chatRoomService);
		return;
	}

	// Grid에 친구 버튼을 추가해주는 메소드
	// 현재 창 크기에 따라서 배열 방식이 달라짐
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
		// 추후에 사용하기 위해서 저장
		model.setFriendGrid(friendGrid);
		model.setFriendsList(friendsButtonList);
	}

	/**
	 * 서버에서 친구목록을 받아오는 메소드
	 * 
	 * 1. 친구목록을 서버에 요청한다. 2. 친구목록이 서버로 부터 돌아오면 이를 버튼으로 만들어 반환한다.
	 * 
	 */
	private List<ToggleButton> rcvFriendsList() {
		try {
			if (sock.isConnected()) {
				List<ToggleButton> temp_list = new ArrayList<ToggleButton>();

				messageListSend = model.getMessageListSend();
				messageListRcv = model.getMessageListRcv();

				// 요청 메시지와 현재 사용자 이름을 전송 리스트에 담음
				messageList.add(0, "rcv_friends");
				messageList.add(1, model.getConnectedName());

				// 친구 목록 요청을 전송
				messageListSend.writeObject(messageList);
				messageListSend.flush();
				messageListSend.reset();

				messageList = (ArrayList<String>) messageListRcv.readObject();

				// 친구 목록이 도착할 시,버튼 리스트으로 만들어 반환한다.
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

	/**
	 * 친구 삭제 버튼을 누르면 실행되는 메소드
	 * 
	 * 1. 삭제를 위해 선택된 버튼들로 리스트를 구성한다. 2. 서버에 요청과 함께 삭제 리스트도 전송한다. 3. 서버에서 응답이 돌아오면
	 * 클라이언트 상에서 삭제한다.
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
	 * 친구 추가 버튼을 누르면 실행되는 메소드
	 * 
	 * 1. 필드가 비어있는지,자기자신인지 여부를 체크한다. 2. 입력된 친구를 요청과 함께 서버로 전송한다. 3. 서버에서 돌아온 응답에 맞게
	 * 사용자에게 보여준다. - 성공시 친구버튼 추가 - 이미 친구이거나,존재하지 않는 사용자일 경우 메시지로 표시)
	 */
	void addFriendHandler(ActionEvent e) {
		// 추가할 친구의 ID를 입력 받기
		TextInputDialog dialog = new TextInputDialog("Insert Friend's ID");

		dialog.setTitle("Add New Friend");
		dialog.setHeaderText("Insert Friend's ID");
		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()) {
			String friend = result.get();

			// 필드가 비어있는지 확인
			if (friend.equals("Insert Friend's ID")) {
				loginService.alertHandler("Nothing Inserted!");
				addFriendHandler(e);
			}

			// 사용자 자신인지 확인
			if (friend.equals(model.getConnectedID())) {
				loginService.alertHandler("Cannot add Yourself!");
				addFriendHandler(e);
			} else {
				try {
					if (sock.isConnected()) {

						messageListSend = model.getMessageListSend();
						messageRcv = model.getMessageRcv();

						// 서버에 요청과 추가할 친구 전송
						messageList.add(0, "add_friend");
						messageList.add(1, model.getConnectedName());
						messageList.add(2, friend);

						messageListSend.writeObject(messageList);
						messageListSend.flush();
						messageListSend.reset();
						messageList.clear();

						if ((message = messageRcv.readLine()) != null) {
							// 친구 추가에 성공한 경우
							if (message.substring(0, 4).equals("yadd") || message.substring(0, 3).equals("add")) {
								if (message.substring(0, 1).equals("y"))
									friend = message.substring(5, message.length());
								else
									friend = message.substring(4, message.length());

								// 해당 친구를 친구 목록에 추가
								tmpFriend = new ToggleButton(friend);
								tmpFriend.setShape(new Circle(10));
								tmpFriend.setPrefSize(btnHeight * 2, btnHeight * 2);

								// 친구 목록 Grid 갱신
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

							// 존재하지 않는 사용자인 경우
							if (message.equals("yno_such_user") || message.equals("no_such_user")) {
								loginService.alertHandler("No such User!");
								addFriendHandler(e);
							}

							// 이미 추가한 친구인 경우
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