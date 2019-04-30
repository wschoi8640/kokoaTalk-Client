package com.wschoi.kokoaClient;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;

/*
 *  이 클래스는 채팅방 기능을 구성하고 채팅 서버와 통신한다. 
 */
public class ChattingRoom extends VBox {
	public Stage parentStage;
	private TextArea textArea;
	private TextField textField;
	private String echo;
	private Socket sock;
	private ObjectOutputStream messageOutput;
	private ObjectInputStream messageInput;
	private String userName;
	private String chatroomName;
	private List<String> message;
	private Model model;
	private boolean isOpen = false;

	// 현재 사용자의 이름, 채팅방이름을 가져온다.
	public ChattingRoom(String userName, String chatroomName, Model echoModel) {
		this.model = echoModel;
		this.userName = userName;
		this.chatroomName = chatroomName;
		initialize();
	}

	void initialize() {
		message = new ArrayList<String>();

		// 채팅 내용이 표시되는곳
		textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setPrefSize(600, 800);

		// 채팅을 입력하는 곳
		textField = new TextField();
		// 채팅을 입력할시 서버로 보내지도록 이벤트 설정한다.
		textField.setOnAction(e -> handleTextField(e));
		this.getChildren().addAll(textArea, textField);
		handleConnect();
		handleGetChatData();
	}

	void handleConnect() {
		try {
			// 서버 IP의 채팅서버 Port로 연결한다.
			sock = new Socket("192.168.0.43", 10002);
			new ChattingRoomThread(sock).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 과거의 채팅 내용을 불러오는 메소드
	// 채팅방이 열릴시 실행된다.
	void handleGetChatData() {
		try {
			if (sock.isConnected()) {
				OutputStream out = sock.getOutputStream();
				messageOutput = new ObjectOutputStream(out);
				message.clear();

				// 사용자 이름과 채팅방 이름을 요청에 담아서 보낸다.
				message.add("rcv_chatData");
				message.add(userName);
				message.add(chatroomName);

				List<String> tempList = new ArrayList<String>();
				tempList.addAll(message);
				messageOutput.writeObject(tempList);
				messageOutput.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 채팅 입력시 실행되는 메소드
	// 채팅 서버로 입력된 내용을 전송한다.
	void handleTextField(ActionEvent event) {
		try {
			if (sock.isConnected()) {
				OutputStream out = sock.getOutputStream();
				messageOutput = new ObjectOutputStream(out);
				echo = textField.getText();

				message.clear();
				message.add("send_chatData");
				message.add(userName);
				message.add(chatroomName);
				message.add(echo);
				messageOutput.writeObject(message);
				messageOutput.flush();

				textField.setText("");
				textField.requestFocus();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 채팅 서버에서 돌아오는 응답을 처리하는 쓰레드
	private class ChattingRoomThread extends Thread {
		Socket sock;

		ChattingRoomThread(Socket sock) {
			this.sock = sock;
		}

		@Override
		public void run() {
			try {
				InputStream in = sock.getInputStream();
				messageInput = new ObjectInputStream(in);

				while (true) {
					try {
						message.clear();
						message = (ArrayList<String>) messageInput.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					int count = 0;

					// 채팅서버에서 도착한 메시지가 현재 사용자, 현재 채팅방에 해당하는지 확인하기 위해 확인한다.
					String all_member0 = message.get(1) + ", " + message.get(2);
					String all_member1 = userName + ", " + chatroomName;
					String[] messageInfos = all_member0.split(", ");
					String[] chatroomInfos = all_member1.split(", ");

					// 도착한 메시지와 현재 채팅방 정보를 비교한다.
					for (String messageInfo : messageInfos) {
						for (String chatroomInfo : chatroomInfos) {
							if (messageInfo.equals(chatroomInfo))
								count++;

						}
					}

					textArea.setFont(new Font("Consolas", 20.0));
					String send_key = "send_chatData";
					String rcv_key = "rcv_chatData";

					if (message != null) {
						// 채팅 전송에 성공할 경우 채팅창에 내용을 표시해 준다.
						if (message.get(0).equals(send_key)) {
							if (count == messageInfos.length && count == chatroomInfos.length) {
								textArea.appendText("[" + message.get(1) + "] : " + message.get(3) + "\n");
							}
						}

						// 과거 채팅 받아오기에 성공할 경우 채팅창에 표시해준다.
						if (message.get(0).equals(rcv_key) && isOpen == false) {
							if (count == messageInfos.length && count == chatroomInfos.length) {
								for (int i = 3; i < message.size(); i++) {
									textArea.appendText(message.get(i));
								}
								// 이미 열려있는 채팅방의 경우 과거의 채팅을 또 보여줄 필요가 없다.
								isOpen = true;
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
