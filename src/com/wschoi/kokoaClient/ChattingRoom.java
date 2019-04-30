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
 *  �� Ŭ������ ä�ù� ����� �����ϰ� ä�� ������ ����Ѵ�. 
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

	// ���� ������� �̸�, ä�ù��̸��� �����´�.
	public ChattingRoom(String userName, String chatroomName, Model echoModel) {
		this.model = echoModel;
		this.userName = userName;
		this.chatroomName = chatroomName;
		initialize();
	}

	void initialize() {
		message = new ArrayList<String>();

		// ä�� ������ ǥ�õǴ°�
		textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setPrefSize(600, 800);

		// ä���� �Է��ϴ� ��
		textField = new TextField();
		// ä���� �Է��ҽ� ������ ���������� �̺�Ʈ �����Ѵ�.
		textField.setOnAction(e -> handleTextField(e));
		this.getChildren().addAll(textArea, textField);
		handleConnect();
		handleGetChatData();
	}

	void handleConnect() {
		try {
			// ���� IP�� ä�ü��� Port�� �����Ѵ�.
			sock = new Socket("192.168.0.43", 10002);
			new ChattingRoomThread(sock).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ������ ä�� ������ �ҷ����� �޼ҵ�
	// ä�ù��� ������ ����ȴ�.
	void handleGetChatData() {
		try {
			if (sock.isConnected()) {
				OutputStream out = sock.getOutputStream();
				messageOutput = new ObjectOutputStream(out);
				message.clear();

				// ����� �̸��� ä�ù� �̸��� ��û�� ��Ƽ� ������.
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

	// ä�� �Է½� ����Ǵ� �޼ҵ�
	// ä�� ������ �Էµ� ������ �����Ѵ�.
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

	// ä�� �������� ���ƿ��� ������ ó���ϴ� ������
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

					// ä�ü������� ������ �޽����� ���� �����, ���� ä�ù濡 �ش��ϴ��� Ȯ���ϱ� ���� Ȯ���Ѵ�.
					String all_member0 = message.get(1) + ", " + message.get(2);
					String all_member1 = userName + ", " + chatroomName;
					String[] messageInfos = all_member0.split(", ");
					String[] chatroomInfos = all_member1.split(", ");

					// ������ �޽����� ���� ä�ù� ������ ���Ѵ�.
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
						// ä�� ���ۿ� ������ ��� ä��â�� ������ ǥ���� �ش�.
						if (message.get(0).equals(send_key)) {
							if (count == messageInfos.length && count == chatroomInfos.length) {
								textArea.appendText("[" + message.get(1) + "] : " + message.get(3) + "\n");
							}
						}

						// ���� ä�� �޾ƿ��⿡ ������ ��� ä��â�� ǥ�����ش�.
						if (message.get(0).equals(rcv_key) && isOpen == false) {
							if (count == messageInfos.length && count == chatroomInfos.length) {
								for (int i = 3; i < message.size(); i++) {
									textArea.appendText(message.get(i));
								}
								// �̹� �����ִ� ä�ù��� ��� ������ ä���� �� ������ �ʿ䰡 ����.
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
