package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import enums.MsgKeys;
import javafx.event.ActionEvent;
import model.Model;

public class LogoutService {
	
	private static List<String> messageList = new ArrayList(); 
	private static ObjectOutputStream messageListSend;
	/**
	 * 1. Send Server Logout status
	 * <br/>2. Server Updates Connection status
	 * <br/>3. Change Grid to Login Grid
	 * 
	 * @param logoutEvent
	 */
	static void logout(ActionEvent e, Model model) {
		try {
			messageListSend = model.getMessageListSend();
			messageList.clear();
			messageList.add(MsgKeys.LogoutRequest.getKey());
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
}
