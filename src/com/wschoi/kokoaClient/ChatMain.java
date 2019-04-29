package com.wschoi.kokoaClient;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 * �� Ŭ������ ä�� Ŭ���̾�Ʈ�� ù ȭ���� �α��� ȭ���� ����.
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
				//Getter and Setter Ŭ���� ����
				Model model = new Model(); 
				
				//Login Ŭ���� ���� �� ����
				LoginService loginService = new LoginService(model);
				model.setLoginService(loginService);
				loginService.parentStage = primaryStage;
				
				Scene scene = new Scene(loginService,600,700);
				
				primaryStage.setOnCloseRequest(e-> loginService.closeHandler(e));
				primaryStage.setTitle("KokoaTalk");
				primaryStage.setScene(scene);
				primaryStage.show();
		}
}
