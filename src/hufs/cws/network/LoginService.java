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
 * �� Ŭ������ �α��� ȭ���� �����ϰ� ä�� ������ �����Ѵ�.
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

	// ȭ���� �����ϰ� ������ ����
	public LoginService(Model model) {
		this.model = model;
		initialize();
		handleConnect();
	}

	void initialize() {
		messageList = new ArrayList<String>();

		// �α��� �� Grid
		loginGrid = new GridPane();
		loginGrid.setAlignment(Pos.CENTER);
		loginGrid.setHgap(15);
		loginGrid.setVgap(15);

		// Grid�� ���α׷� ���� �߰�
		titleLabel = new Label("KokoaTalk");
		titleLabel.setFont(new Font("Consolas", 30.0));

		// ID �Է� �ʵ�
		idField = new TextField();
		idField.setPromptText("Insert ID");
		idField.setPrefWidth(200);

		// Password �Է� �ʵ�
		pwField = new PasswordField();
		pwField.setPromptText("Insert Password");
		pwField.setPrefWidth(200);

		// �α��� ��ư
		loginBtn = new Button("Login");
		loginBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		// �α��� ��ư �̺�Ʈ ����
		loginBtn.setOnAction(e -> loginHandler(e));

		// ȸ������ ��ư
		joinBtn = new Button("join");
		joinBtn.setPrefWidth(200);
		// ȸ������ ��ư �̺�Ʈ ����
		joinBtn.setOnAction(e -> joinHandler(e));

		loginGrid.add(idField, 1, 1);
		loginGrid.add(loginBtn, 2, 1, 2, 2);
		loginGrid.add(pwField, 1, 2);
		loginGrid.add(joinBtn, 1, 3);
		// ȭ�� ��ȯ�� ���� ����
		model.setLoginGrid(loginGrid);
		model.setTitleLabel(titleLabel);

		this.setAlignment(Pos.CENTER);
		this.setSpacing(100);
		this.getChildren().addAll(titleLabel, loginGrid);

	}

	/*
	 * ������ �����ϱ� ���� �޼ҵ�
	 * 
	 * 1. ������ �����Ѵ�. 2. ���� ������, �޽��� ������ ���� ��ü�� �����Ѵ�. 3. �ٸ� Ŭ�������� ����ϱ� ���� �����Ѵ�.
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
	 * �α��� ��ư ������ ����Ǵ� �޼ҵ�
	 * 
	 * 1. ID�� Password�� �ԷµǾ����� Ȯ���Ѵ�. 2. ������ �Էµ� ID�� Password�� ������. 3. �������� ���ƿ� ���信
	 * ���� ����ڿ��� �˷��ش�.(����, ����)
	 * 
	 */
	void loginHandler(ActionEvent event) {
		// �ʵ忡 �Է� ���� Ȯ��
		if (idField.getText().trim().isEmpty()) {
			alertHandler("Enter User ID!");
			return;
		}
		if (pwField.getText().trim().isEmpty()) {
			alertHandler("Enter User Password!");
			return;
		}

		// �Էµ� ���� ������ ����
		if (sock.isConnected()) {
			userID = idField.getText();
			userPW = pwField.getText();

			// ������ ���� List�� ����
			messageList.add(0, "do_login");
			messageList.add(1, userID);
			messageList.add(2, userPW);

			try {
				// List�� ������ ������
				messageListSender.writeObject(messageList);
				messageListSender.flush();
				messageListSender.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
			messageList.clear();

			// ������ ���� ó��
			String response_message;
			try {
				// ������ String ���·� ���ƿ�
				response_message = messageRcv.readLine();

				// �α��ο� ������ ���
				if (response_message.substring(0, 5).equals("hello") || response_message.substring(0, 5).equals("yhell")) {
					alertHandler("Login Success!");

					// �������� ������ ����
					model.setConnectedName(response_message.substring(6, response_message.length()));
					model.setConnectedID(userID);

					// �ֻ��� ���������� ����ϱ� ���� ����
					model.setLoginService(this);
					
					// ģ�� ��� �����ִ� Ŭ���� ȣ��,�߰�
					chatUserService = new ChatUserSevice(model);
					this.getChildren().clear();
					this.getChildren().add(chatUserService);
					return;
				}

				// ��ȣ�� Ʋ�� ���
				if (response_message.equals("wrong_pw")) {
					// ��й�ȣ â ����
					pwField.setText("");
					alertHandler("Wrong Password!");
					return;
				}

				// ID�� ���� ���
				if (response_message.equals("no_id")) {
					// �Է� �ʵ� ��� ����
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
	 * ������ â�� �����ҽ� ����Ǵ� �޼ҵ�
	 * 
	 * 1. ������ �α׾ƿ� �Ѵٴ� �޽��� ���� 2. �� �޽��� ���� ��ü �� ���� �ݱ� 3. â ����
	 * 
	 */
	void closeHandler(WindowEvent e) {
		try {
			// �޽��� ���� (������ ��ٸ��� ����)
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

	// ��� ���� �� â���� �˷��ִ� �޼ҵ�
	void alertHandler(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(message);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}

	// join ��ư ������ ����Ǵ� �޼ҵ�
	void joinHandler(ActionEvent event) {
		// �ֻ��� ���������� ����ϱ� ���� ����
		model.setLoginService(this);

		// ȸ������ Ŭ���� ���� �� �߰�
		joinService = new JoinService(model);
		this.getChildren().clear();
		this.getChildren().add(joinService);
	}
}
