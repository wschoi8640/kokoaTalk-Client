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
 * 이 클래스는 회원가입 화면을 구성한다.
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

	// 최상위 스테이지와 연결된 소켓을 가져온 후 화면 구성
	public JoinService(Model model) {
		this.model = model;
		this.loginService = model.getLoginService();
		this.sock = model.getSock();
		initialize();
	}

	void initialize() {
		messageList = new ArrayList<String>();

		// 회원가입 용 Grid
		joinGrid = new GridPane();
		joinGrid.setAlignment(Pos.CENTER);
		joinGrid.setHgap(15);
		joinGrid.setVgap(15);

		// 회원가입 용 타이틀 설정
		titleLabel = new Label("Welcome !");
		titleLabel.setFont(new Font("Consolas", 30.0));

		// 이름 입력 필드
		nameField = new TextField();
		nameField.setPromptText("Insert Your Name");
		nameField.setPrefWidth(200);

		// ID 입력 필드
		idField = new TextField();
		idField.setPromptText("Insert New ID");
		idField.setPrefWidth(200);

		// PW 입력 필드
		pwField = new PasswordField();
		pwField.setPromptText("Set Password");
		pwField.setPrefWidth(200);

		// PW 반복 필드
		pw2Field = new PasswordField();
		pw2Field.setPromptText("Repeat Password");
		pw2Field.setPrefWidth(200);

		// 가입하기 버튼
		joinBtn = new Button("join");
		joinBtn.setPrefWidth(200);
		// 가입하기 버튼 이벤트 설정
		joinBtn.setOnAction(e -> joinHandler(e));

		// 리셋 버튼
		resetBtn = new Button("reset");
		resetBtn.setPrefWidth(200);
		// 리셋 버튼 이벤트 설정
		resetBtn.setOnAction(e -> resetHandler(e));

		// 뒤로가기 버튼
		backBtn = new Button("back");
		backBtn.setPrefWidth(200);
		// 뒤로가기 버튼 이벤트 설정
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
	 * 회원가입 버튼 누를시 실행되는 메소드
	 * 
	 * 1. 비어있는 필드가 없는지 확인한다. 2. 비밀 번호 반복 일치하는지 확인한다. 3. 서버에 입력된 이름,ID,비밀번호를 보낸다. 4.
	 * 돌아온 응답에 맞게 사용자에게 알려준다.
	 */
	void joinHandler(ActionEvent event) {
		// 비어있는 필드가 없는지 확인
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

		// 비밀번호 반복 일치 여부를 확인
		if (!pwField.getText().equals(pw2Field.getText())) {
			loginService.alertHandler("Password Not Same!");
			return;
		}

		try {
			// 입력된 값을 서버로 보냄
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

				// 돌아온 응답을 처리
				String rcv_message = messageRcv.readLine();

				// 회원 가입에 성공시
				if (rcv_message.equals("join_ok")) {
					// 로그인 화면으로 돌아감
					model.getLoginService().getChildren().clear();
					model.getLoginService().getChildren().addAll(model.getTitleLabel(), model.getLoginGrid());
					return;
				}

				// 회원가입 실패시
				if (rcv_message.equals("join_fail")) {
					// 입력 필드 비우기
					idField.clear();
					messageList.clear();
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 새로 입력하기 위해 사용되는 메소드
	void resetHandler(ActionEvent event) {
		nameField.clear();
		idField.clear();
		pwField.clear();
		pw2Field.clear();
	}

	// 로그인 화면으로 돌아갈 때 사용하는 메소드
	void backHandler(ActionEvent event) {
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().addAll(model.getTitleLabel(), model.getLoginGrid());
	}

}
