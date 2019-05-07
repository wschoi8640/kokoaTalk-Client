package domain;

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

import enums.ErrMsgs;
import enums.MsgKeys;
import enums.Settings;
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
import model.Model;
import utils.AlertHandler;
import utils.CompetitionHandler;
import utils.ValidChecker;

/**
 * This class consists Join Panel
 * 
 * @author wschoi8640
 * @version 1.0
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

	/**
	 * receive Primary Stage and Socket Object
	 *<br/> consist Panel
	 *
	 * @param model
	 */
	public JoinService(Model model) {
		this.model = model;
		this.loginService = model.getLoginService();
		this.sock = model.getSock();
		initJoinGrid();
	}

	void initJoinGrid() {
		// message List to send Server 
		messageList = new ArrayList<String>();

		// Grid for Join
		joinGrid = new GridPane();
		joinGrid.setAlignment(Pos.CENTER);
		joinGrid.setHgap(15);
		joinGrid.setVgap(15);

		// Title for Join Grid
		titleLabel = new Label(Settings.WelcomeMsg.getSetting());
		titleLabel.setFont(new Font(Settings.Font.getSetting(), 30.0));

		// Name Field
		nameField = new TextField();
		nameField.setPromptText("Insert Your Name");
		nameField.setPrefWidth(200);

		// ID Field
		idField = new TextField();
		idField.setPromptText("Insert New ID");
		idField.setPrefWidth(200);

		// PW Field
		pwField = new PasswordField();
		pwField.setPromptText("Set Password");
		pwField.setPrefWidth(200);

		// PW Repeat Field
		pw2Field = new PasswordField();
		pw2Field.setPromptText("Repeat Password");
		pw2Field.setPrefWidth(200);

		// Join Btn
		joinBtn = new Button("join");
		joinBtn.setPrefWidth(200);
		// Set Join Button Event
		joinBtn.setOnAction(e -> joinHandler(e));

		// Reset Btn
		resetBtn = new Button("reset");
		resetBtn.setPrefWidth(200);
		// Set Reset Button Event
		resetBtn.setOnAction(e -> resetHandler(e));

		// Back Btn
		backBtn = new Button("back");
		backBtn.setPrefWidth(200);
		// Set Back Button Event
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
	 * Execute on Login Btn Event
	 * <br/>
	 * <br/>1. Check if any Field is blank
	 * <br/>2. Check if PW and PW2 same 
	 * <br/>3. Send Server field Vals 
	 * <br/>4. Receive Server response
	 * <br/>5. Show user Result
	 */
	void joinHandler(ActionEvent event) {
		CompetitionHandler.handle(model.getCurStatus());	
		// Check if any Field is blank
		if (nameField.getText().trim().isEmpty()) {
			AlertHandler.alert(ErrMsgs.BlankNameField.getMsg());
			return;
		}
		if (idField.getText().trim().isEmpty()) {
			AlertHandler.alert(ErrMsgs.BlankIdField.getMsg());
			return;
		}
		if (pwField.getText().trim().isEmpty()) {
			AlertHandler.alert(ErrMsgs.BlankPWField.getMsg());
			return;
		}
		if (pw2Field.getText().trim().isEmpty()) {
			AlertHandler.alert(ErrMsgs.BlankRepeatPW.getMsg());
			return;
		}

		// Check if PW and PW2 same 
		if (!pwField.getText().equals(pw2Field.getText())) {
			AlertHandler.alert(ErrMsgs.WrongPWRepeat.getMsg());
			return;
		}
		
		if(!ValidChecker.joinIDCheck(idField.getText())) {
			AlertHandler.alert(ErrMsgs.WrongIDFormat.getMsg());
			idField.clear();
			return;
		}
		
		if(!ValidChecker.joinPWCheck(pwField.getText())) {
			AlertHandler.alert(ErrMsgs.WrongPWFormat.getMsg());
			pwField.clear();
			pw2Field.clear();
			return;
		}
		

		try {
			if (sock.isConnected()) {
				messageListSend = model.getMessageListSend();
				messageRcv = model.getMessageRcv();

				userName = nameField.getText();
				userID = idField.getText();
				userPW = pwField.getText();

				// Add key and Field Vals to List
				messageList.add(0, MsgKeys.JoinRequest.getKey());
				messageList.add(1, userName);
				messageList.add(2, userID);
				messageList.add(3, userPW);

				// Send Server List
				messageListSend.writeObject(messageList);
				messageListSend.flush();
				messageListSend.reset();
				messageList.clear();
				model.setCurStatus("waiting");
				
				// Receive response
				String rcv_message = null;
				
				// Wait for response
				while(rcv_message == null) {
					// Response Key (suc/fail)
					rcv_message = messageRcv.readLine();
				}
				model.setCurStatus("available");

				
				// Join Successful
				if (rcv_message.equals(MsgKeys.JoinSuccess.getKey())) {
					AlertHandler.alert(ErrMsgs.JoinSuccess.getMsg());
					// Return to Login Grid
					model.getLoginService().getChildren().clear();
					model.getLoginService().getChildren().addAll(model.getTitleLabel(), model.getLoginGrid());
					return;
				}

				// Join Fail
				if (rcv_message.equals(MsgKeys.JoinFail.getKey())) {
					// Empty Fields
					AlertHandler.alert(ErrMsgs.AlreadyExistingID.getMsg());
					nameField.clear();
					idField.clear();
					pwField.clear();
					pw2Field.clear();
					messageList.clear();
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Execute on Reset Btn Event
	 * <br/> Empty every fields
	 * 
	 * @param resetEvent
	 */
	void resetHandler(ActionEvent event) {
		nameField.clear();
		idField.clear();
		pwField.clear();
		pw2Field.clear();
	}

	/**
	 * Execute on Back Btn Event
	 * <br/> Go back to Login Grid
	 * 
	 * @param backEvent
	 */
	void backHandler(ActionEvent event) {
		model.setCurStage("loginService");
		model.getLoginService().getChildren().clear();
		model.getLoginService().getChildren().addAll(model.getTitleLabel(), model.getLoginGrid());
	}

}
