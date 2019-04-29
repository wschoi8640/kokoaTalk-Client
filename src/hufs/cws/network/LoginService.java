package hufs.cws.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/*
 * 이 클래스는 로그인 화면을 구성하고 채팅 서버에 연결한다.
 */
public class LoginService extends VBox {
	public Stage parentStage;
	private Model model;
	private ChatUserSevice chatUserService;
	private JoinService joinService;
	private BufferedReader messageRcv;
	private ObjectOutputStream messageListSender;
	private ObjectInputStream messageListRcv;
	private PrintWriter messageSend;
	private List<String> messageList;
	private GridPane loginGrid;
	private TextField idField;
	private PasswordField pwField;
	private Label titleLabel;
	private Button loginBtn;
	private Button joinBtn;
	private String userID;
	private String userPW;
	private Socket sock;
	private String serverIP = "172.30.1.42";
	private int serverPort = 10001;

	// 화면을 구성하고 서버에 연결
	public LoginService(Model model) {
		this.model = model;
		initialize();
		handleConnect();
	}

	void initialize() {
		messageList = new ArrayList<String>();

		// 로그인 용 Grid
		loginGrid = new GridPane();
		loginGrid.setAlignment(Pos.CENTER);
		loginGrid.setHgap(15);
		loginGrid.setVgap(15);

		// Grid에 프로그램 제목 추가
		titleLabel = new Label("KokoaTalk");
		titleLabel.setFont(new Font("Consolas", 30.0));

		// ID 입력 필드
		idField = new TextField();
		idField.setPromptText("Insert ID");
		idField.setPrefWidth(200);

		// Password 입력 필드
		pwField = new PasswordField();
		pwField.setPromptText("Insert Password");
		pwField.setPrefWidth(200);

		// 로그인 버튼
		loginBtn = new Button("Login");
		loginBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		// 로그인 버튼 이벤트 설정
		loginBtn.setOnAction(e -> loginHandler(e));

		// 회원가입 버튼
		joinBtn = new Button("join");
		joinBtn.setPrefWidth(200);
		// 회원가입 버튼 이벤트 설정
		joinBtn.setOnAction(e -> joinHandler(e));

		loginGrid.add(idField, 1, 1);
		loginGrid.add(loginBtn, 2, 1, 2, 2);
		loginGrid.add(pwField, 1, 2);
		loginGrid.add(joinBtn, 1, 3);
		// 화면 전환을 위해 저장
		model.setLoginGrid(loginGrid);
		model.setTitleLabel(titleLabel);

		this.setAlignment(Pos.CENTER);
		this.setSpacing(100);
		this.getChildren().addAll(titleLabel, loginGrid);

	}

	/*
	 * 서버에 연결하기 위한 메소드
	 * 
	 * 1. 서버에 연결한다. 2. 서버 연결후, 메시지 전송을 위한 객체를 생성한다. 3. 다른 클래스에서 사용하기 위해 저장한다.
	 * 
	 */
	void handleConnect() {
		try {
			sock = new Socket(serverIP, serverPort);

			InputStream in = sock.getInputStream();
			OutputStream out = sock.getOutputStream();

			messageListSender = new ObjectOutputStream(out);
			messageListRcv = new ObjectInputStream(in);
			messageSend = new PrintWriter(new OutputStreamWriter(out));
			messageRcv = new BufferedReader(new InputStreamReader(in));

			model.setSock(sock);
			model.setMessageRcv(messageRcv);
			model.setMessageListSend(messageListSender);
			model.setMessageListRcv(messageListRcv);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 로그인 버튼 누를시 실행되는 메소드
	 * 
	 * 1. ID와 Password가 입력되었는지 확인한다. 2. 서버에 입력된 ID와 Password를 보낸다. 3. 서버에서 돌아온 응답에
	 * 따라 사용자에게 알려준다.(성공, 실패)
	 * 
	 */
	void loginHandler(ActionEvent event) {
		// 필드에 입력 여부 확인
		if (idField.getText().trim().isEmpty()) {
			alertHandler("Enter User ID!");
			return;
		}
		if (pwField.getText().trim().isEmpty()) {
			alertHandler("Enter User Password!");
			return;
		}

		// 입력된 값을 서버로 보냄
		if (sock.isConnected()) {
			userID = idField.getText();
			userPW = pwField.getText();

			// 서버에 보낼 List에 저장
			messageList.add(0, "do_login");
			messageList.add(1, userID);
			messageList.add(2, userPW);

			try {
				// List를 서버로 보내기
				messageListSender.writeObject(messageList);
				messageListSender.flush();
				messageListSender.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
			messageList.clear();

			// 서버의 응답 처리
			String response_message;
			try {
				// 응답은 String 형태로 돌아옴
				response_message = messageRcv.readLine();

				// 로그인에 성공한 경우
				if (response_message.substring(0, 5).equals("hello") || response_message.substring(0, 5).equals("yhell")) {
					alertHandler("Login Success!");

					// 접속자의 데이터 저장
					model.setConnectedName(response_message.substring(6, response_message.length()));
					model.setConnectedID(userID);

					// 최상위 스테이지로 사용하기 위해 저장
					model.setLoginService(this);
					
					// 친구 목록 보여주는 클래스 호출,추가
					chatUserService = new ChatUserSevice(model);
					this.getChildren().clear();
					this.getChildren().add(chatUserService);
					return;
				}

				// 암호가 틀릴 경우
				if (response_message.equals("wrong_pw")) {
					// 비밀번호 창 비우기
					pwField.setText("");
					alertHandler("Wrong Password!");
					return;
				}

				// ID가 없는 경우
				if (response_message.equals("no_id")) {
					// 입력 필드 모두 비우기
					idField.setText("");
					pwField.setText("");
					alertHandler("No Such ID exists!");
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * 강제로 창을 종료할시 실행되는 메소드
	 * 
	 * 1. 서버에 로그아웃 한다는 메시지 전송 2. 각 메시지 전송 객체 및 소켓 닫기 3. 창 종료
	 * 
	 */
	void closeHandler(WindowEvent e) {
		try {
			// 메시지 전송 (응답은 기다리지 않음)
			messageList.clear();
			messageList.add("do_logout");
			messageListSender.writeObject(messageList);
			messageListSender.flush();
			messageListSender.reset();
			messageListSender.close();
			messageRcv.close();
			sock.close();
			Platform.exit();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	// 결과 값을 새 창으로 알려주는 메소드
	void alertHandler(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(message);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}

	// join 버튼 누를시 실행되는 메소드
	void joinHandler(ActionEvent event) {
		// 최상위 스테이지로 사용하기 위해 저장
		model.setLoginService(this);

		// 회원가입 클래스 생성 및 추가
		joinService = new JoinService(model);
		this.getChildren().clear();
		this.getChildren().add(joinService);
	}
}
