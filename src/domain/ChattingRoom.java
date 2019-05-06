package domain;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

import enums.Settings;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Model;

import java.io.*;

/**
 * This class consists ChattingRoom Panel and offers Chat service
 * 
 * @author wschoi8640
 * @version 1.0
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

	// brings Name of User and Name of Chatroom
	public ChattingRoom(String userName, String chatroomName, Model echoModel) {
		this.model = echoModel;
		this.userName = userName;
		this.chatroomName = chatroomName;
		initChattingRoom();
	}

	void initChattingRoom() {
		message = new ArrayList<String>();

		// TextArea where text Printed
		textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setPrefSize(600, 800);

		// TextField where text filed
		textField = new TextField();
		// Sent to the Server on submit
		textField.setOnAction(e -> handleTextField(e));
		this.getChildren().addAll(textArea, textField);
		handleConnect();
		handleGetChatData();
	}

	void handleConnect() {
		try {
			// Connects to Server port and IP
			sock = new Socket(Settings.ServerIP.getSetting(), Settings.ChattingServerPort.getNum());
			new ChattingRoomThread(sock).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Brings previous chat 
	void handleGetChatData() {
		try {
			if (sock.isConnected()) {
				OutputStream out = sock.getOutputStream();
				messageOutput = new ObjectOutputStream(out);
				message.clear();

				// Send Server userName and chatroomName
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

	// Send Server new chatData
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

	// Handles Response from the Server
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

					
					String all_member0 = message.get(1) + ", " + message.get(2);
					String all_member1 = userName + ", " + chatroomName;
					String[] messageInfos = all_member0.split(", ");
					String[] chatroomInfos = all_member1.split(", ");

					// Check if response is User's
					// Compare UserName and ChatroomName.
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
						// Print Chats at Chatroom after sending Server
						if (message.get(0).equals(send_key)) {
							if (count == messageInfos.length && count == chatroomInfos.length) {
								textArea.appendText("[" + message.get(1) + "] : " + message.get(3) + "\n");
							}
						}

						// Print Old Chats at Chatroom on rcv success
						if (message.get(0).equals(rcv_key) && isOpen == false) {
							if (count == messageInfos.length && count == chatroomInfos.length) {
								for (int i = 3; i < message.size(); i++) {
									textArea.appendText(message.get(i));
								}
								// Print Old Chat only once
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
