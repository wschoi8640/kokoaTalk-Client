package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertHandler {
	/**
	 * Shows results using Alert
	 * 
	 * @param message
	 */
	public static void alert(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(message);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}
}
