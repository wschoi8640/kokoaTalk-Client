package com.wschoi.kokoaClient;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 * 이 클래스는 채팅 클라이언트의 첫 화면인 로그인 화면을 띄운다.
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
				//Getter and Setter 클래스 생성
				Model model = new Model(); 
				
				//Login 클래스 생성 및 실행
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
