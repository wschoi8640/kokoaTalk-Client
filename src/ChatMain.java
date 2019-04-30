

import domain.*;
import enums.ClientSettings;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Model;

/**
 * Main Class for KokoaClient
 * <br/>Creates Primary Stage and Adds Login Panel
 * 
 * @author wschoi8640
 * @version 1.0
 */
public class ChatMain extends Application
{
		public static void main(String[] args) 
		{
				launch(args);
		}
	
		@Override
		public void start(Stage primaryStage) throws Exception 
		{
				// Class for saving Values
				Model model = new Model(); 
				
				// Call Class for Login
				LoginService loginService = new LoginService(model);
				model.setLoginService(loginService);
				loginService.parentStage = primaryStage;
				
				// Add Login Panel to Primary Stage
				Scene scene = new Scene(loginService,600,700);
				
				primaryStage.setOnCloseRequest(e-> loginService.closeHandler(e));
				primaryStage.setTitle(ClientSettings.Title.getSetting());
				primaryStage.setScene(scene);
				primaryStage.show();
		}
}
