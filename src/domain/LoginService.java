package domain;

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

import enums.ErrMsgs;
import enums.Settings;
import enums.MsgKeys;
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
import model.Model;
import utils.AlertHandler;
import utils.CompetitionHandler;

/**
 * This class consists Login Panel and Connect to Server
 * 
 * @author wschoi8640
 * @version 1.0
 */
public class LoginService extends VBox {
	public Stage parentStage;
	private Model model;
	private UserManager userManager;
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

	/**
	 * Consist Panel and Connect to Server
	 */  
	public LoginService(Model model) {
		this.model = model;
		initLoginGrid();
		connectToServer();
		model.setLoginService(this);
	}

	
	/**
	 * Consist Login Panel
	 */
	void initLoginGrid() {
		// message List to send Server 
		messageList = new ArrayList<String>();

		// Grid for Login
		loginGrid = new GridPane();
		loginGrid.setAlignment(Pos.CENTER);
		loginGrid.setHgap(15);
		loginGrid.setVgap(15);

		// Title for Grid
		titleLabel = new Label(Settings.Title.getSetting());
		titleLabel.setFont(new Font(Settings.Font.getSetting(), 30.0));

		// ID field
		idField = new TextField();
		idField.setPromptText("Insert ID");
		idField.setPrefWidth(200);

		// Password field
		pwField = new PasswordField();
		pwField.setPromptText("Insert Password");
		pwField.setPrefWidth(200);

		// Login Button
		loginBtn = new Button("Login");
		loginBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		// Set Login Button Event
		loginBtn.setOnAction(e -> loginHandler(e));

		// Join Button
		joinBtn = new Button("join");
		joinBtn.setPrefWidth(200);
		// Set Join Button Event
		joinBtn.setOnAction(e -> joinHandler(e));

		// Add all
		loginGrid.add(idField, 1, 1);
		loginGrid.add(loginBtn, 2, 1, 2, 2);
		loginGrid.add(pwField, 1, 2);
		loginGrid.add(joinBtn, 1, 3);
		
		// save Grid for later
		model.setLoginGrid(loginGrid);
		model.setTitleLabel(titleLabel);

		this.setAlignment(Pos.CENTER);
		this.setSpacing(100);
		this.getChildren().addAll(titleLabel, loginGrid);
	}

	/**
	 * This Method Connects to Server
	 * <br/>
	 * <br/>1. Connects to Server.
	 * <br/>2. Makes Stream Object for Message. 
	 * <br/>3. Save Objects for later.
	 */
	void connectToServer() {
		try {
			sock = new Socket(Settings.ServerIP.getSetting(), Settings.LoginServerPort.getNum());

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

	/**
	 * Execute on Login Btn Event
	 * <br/>
	 * <br/>1. Check if ID, PW field is blank 
	 * <br/>2. Send Server ID, PW. 
	 * <br/>3. Receive Server Response
	 * <br/>4. Show Result (suc/fail)
	 * 
	 * @param loginEvent
	 */
	void loginHandler(ActionEvent event) {
		CompetitionHandler.handle(model.getCurStatus());	

		// Check if ID, PW field is blank 
		if (idField.getText().trim().isEmpty()) {
			AlertHandler.alert(ErrMsgs.BlankIdField.getMsg());
			return;
		}
		if (pwField.getText().trim().isEmpty()) {
			AlertHandler.alert(ErrMsgs.BlankPWField.getMsg());
			return;
		}
		// Send Server ID, PW
		if (sock.isConnected()) {
			userID = idField.getText();
			userPW = pwField.getText();

			// Add key and field Values to Msg List
			messageList.add(0, MsgKeys.LoginRequest.getKey());
			messageList.add(1, userID);
			messageList.add(2, userPW);
			try {
				// Send List to Server
				messageListSender.writeObject(messageList);
				messageListSender.flush();
				messageListSender.reset();
				model.setCurStatus("waiting");
			} catch (IOException e) {
				e.printStackTrace();
			}
			messageList.clear();

			// Receive Server response
			String responseMsg = null;
			try {
				// Wait for response
				messageList = null;
				// Wait for response
				while(messageList == null) {
					try {
						messageList = (ArrayList<String>) messageListRcv.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				model.setCurStatus("available");
				responseMsg = messageList.get(0);
				messageList.clear();
				// Login Successful
				if (responseMsg.substring(0, 5).equals(MsgKeys.LoginSuccess.getKey())) {
					AlertHandler.alert(ErrMsgs.LoginSuccess.getMsg());
					// Save user Data
					model.setConnectedName(responseMsg.substring(6, responseMsg.length()));
					model.setConnectedID(userID);

					// Save this Grid for later
					model.setLoginService(this);
					// Change to ChatUser Grid 
					userManager = new UserManager(model);
					model.setCurStage("userManager");
					this.getChildren().clear();
					this.getChildren().add(userManager);
					return;
				}

				// Wrong Password
				if (responseMsg.equals(MsgKeys.LoginFailByPW.getKey())) {
					// Empty Password Field and Alert
					pwField.setText("");
					AlertHandler.alert(ErrMsgs.WrongPassword.getMsg());
					return;
				}

				// No such ID
				if (responseMsg.equals(MsgKeys.LoginFailByID.getKey())) {
					// Empty Both Fields and Alert
					idField.setText("");
					pwField.setText("");
					AlertHandler.alert(ErrMsgs.NoSuchID.getMsg());
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * When Window is closed without Logout
	 * <br/>
	 * <br/>1. Send Server Logout Msg 
	 * <br/>2. Close Msg-send Objects
	 * <br/>3. Close Window
	 * 
	 * @param WindowEvent
	 */
	public void closeHandler(WindowEvent e) {
		try {
			// Doesn't wait response
			messageList.clear();
			messageList.add(MsgKeys.LogoutRequest.getKey());
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

	/**
	 * Execute on Join Btn Event
	 * 
	 * @param joinEvent
	 */ 
	void joinHandler(ActionEvent event) {
		// Save this Grid for later
		model.setLoginService(this);

		// Change to Join Grid
		joinService = new JoinService(model);
		model.setCurStage("joinService");
		this.getChildren().clear();
		this.getChildren().add(joinService);
	}
}
