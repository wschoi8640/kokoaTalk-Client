package com.wschoi.kokoaClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * �� Ŭ������ ȸ������ ȭ���� �����Ѵ�.
 */
public class JoinService extends VBox {
	private LoginService loginService;
	private Model model;
	public Stage parentStage;
	private GridPane joinGrid;
	private Label titleLabel;
	private TextField nameField;
	private TextField idField;
	private TextField pwField;
	private TextField pw2Field;
	private Button joinBtn;
	private Button resetBtn;
	private Button backBtn;
	private Socket sock;
	private List<String> messageList;
	private String userName;
	private String userID;
	private String userPW;
	private BufferedReader messageRcv;
	private ObjectOutputStream messageListSend;

	// �ֻ��� ���������� ����� ������ ������ �� ȭ�� ����
	public JoinService(Model model) {
		this.model = model;
		this.loginService = model.getLoginService();
		this.sock = model.getSock();
		initialize();
	}

	void initialize() {
		messageList = new ArrayList<String>();

		// ȸ������ �� Grid
		joinGrid = new GridPane();
		joinGrid.setAlignment(Pos.CENTER);
		joinGrid.setHgap(15);
		joinGrid.setVgap(15);

		// ȸ������ �� Ÿ��Ʋ ����
		titleLabel = new Label("Welcome !");
		titleLabel.setFont(new Font("Consolas", 30.0));

		// �̸� �Է� �ʵ�
		nameField = new TextField();
		nameField.setPromptText("Insert Your Name");
		nameField.setPrefWidth(200);

		// ID �Է� �ʵ�
		idField = new TextField();
		idField.setPromptText("Insert New ID");
		idField.setPrefWidth(200);

		// PW �Է� �ʵ�
		pwField = new PasswordField();
		pwField.setPromptText("Set Password");
		pwField.setPrefWidth(200);

		// PW �ݺ� �ʵ�
		pw2Field = new PasswordField();
		pw2Field.setPromptText("Repeat Password");
		pw2Field.setPrefWidth(200);

		// �����ϱ� ��ư
		joinBtn = new Button("join");
		joinBtn.setPrefWidth(200);
		// �����ϱ� ��ư �̺�Ʈ ����
		joinBtn.setOnAction(e -> joinHandler(e));

		// ���� ��ư
		resetBtn = new Button("reset");
		resetBtn.setPrefWidth(200);
		// ���� ��ư �̺�Ʈ ����
		resetBtn.setOnAction(e -> resetHandler(e));

		// �ڷΰ��� ��ư
		backBtn = new Button("back");
		backBtn.setPrefWidth(200);
		// �ڷΰ��� ��ư �̺�Ʈ ����
		backBtn.setOnAction(e -> backHandler(e));

		joinGrid.add(nameField, 0, 1);
		joinGrid.add(idField, 0, 2);
		joinGrid.add(pwField, 0, 3);
		joinGrid.add(pw2Field, 0, 4);
		joinGrid.add(joinBtn, 0, 5);
		joinGrid.add(resetBtn, 0, 6);
		joinGrid.add(backBtn, 0, 7);

		this.setAlignment(Pos.CENTER);
		this.setSpacing(100);
		this.getChildren().addAll(titleLabel, joinGrid);
	}

	/**
	 * ȸ������ ��ư ������ ����Ǵ� �޼ҵ�
	 * 
	 * 1. ����ִ� �ʵ尡 ������ Ȯ���Ѵ�. 2. ��� ��ȣ �ݺ� ��ġ�ϴ��� Ȯ���Ѵ�. 3. ������ �Էµ� �̸�,ID,��й�ȣ�� ������. 4.
	 * ���ƿ� ���信 �°� ����ڿ��� �˷��ش�.
	 */
	void joinHandler(ActionEvent event) {
		// ����ִ� �ʵ尡 ������ Ȯ��
		if (nameField.getText().trim().isEmpty()) {
			loginService.alertHandler("Enter User Name!");
			return;
		}
		if (idField.getText().trim().isEmpty()) {
			loginService.alertHandler("Enter User ID!");
			return;
		}
		if (pwField.getText().trim().isEmpty()) {
			loginService.alertHandler("Enter User Password!");
			return;
		}
		if (pw2Field.getText().trim().isEmpty()) {
			loginService.alertHandler("Enter User Password!");
			return;
		}

		// ��й�ȣ �ݺ� ��ġ ���θ� Ȯ��
		if (!pwField.getText().equals(pw2Field.getText())) {
			loginService.alertHandler("Password Not Same!");
			return;
		}

		try {
			// �Էµ� ���� ������ ����
			if (sock.isConnected()) {
				messageListSend = model.getMessageListSend();
				messageRcv = model.getMessageRcv();

				userName = nameField.getText();
				userID = idField.getText();
				userPW = pwField.getText();

				messageList.add(0, "do_join");
				messageList.add(1, userName);
				messageList.add(2, userID);
				messageList.add(3, userPW);

				messageListSend.writeObject(messageList);
				messageListSend.flush();
				messageListSend.reset();
				messageList.clear();

				// ���ƿ� ������ ó��
				String rcv_message = messageRcv.readLine();

				// ȸ�� ���Կ� ������
				if (rcv_message.equals("join_ok")) {
					// �α��� ȭ������ ���ư�
					model.getLoginService().getChildren().clear();
					model.getLoginService().getChildren().addAll(model.getTitleLabel(), model.getLoginGrid());
					return;
				}

				// ȸ������ ���н�
				if (rcv_message.equals("join_fail")) {
					// �Է� �ʵ� ����
					idField.clear();
					messageList.clear();
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ���� �Է��ϱ� ���� ���Ǵ� �޼ҵ�
	void resetHandler(ActionEvent event) {
		nameField.clear();
		idField.clear();
		pwField.clear();
		pw2Field.clear();
	}

	// �α��� ȭ������ ���ư� �� ����ϴ� �޼ҵ�
	void backHandler(ActionEvent event) {
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().addAll(model.getTitleLabel(), model.getLoginGrid());
	}

}
