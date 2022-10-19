package gb.avdotchenkov.client;


import gb.avdotchenkov.client.controllers.AuthController;
import gb.avdotchenkov.client.controllers.ChatController;
import gb.avdotchenkov.client.models.Network;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
public class StartClient extends Application {
	public static final Logger logger = Logger.getLogger("client");
	public TextField loginField;
	public PasswordField passwordField;
	private Network network;
	private Stage primaryStage;
	private Stage authStage;
	private ChatController chatController;

	@Override public void start (Stage stage) throws IOException {
	Handler handler = new FileHandler("src/main/resources/Logger/logs/LogFromLoggerClient.log");
	logger.addHandler(handler);
	handler.setLevel(Level.ALL);

		this.primaryStage = stage;
		
		network = new Network();
		network.connect();
		
		openAuthDialog();
		createChatDialog();
		
	}

	private void openAuthDialog () throws IOException {
		FXMLLoader authLoader = new FXMLLoader(StartClient.class.getResource("auth-view.fxml"));
		authStage = new Stage();
		Scene scene = new Scene(authLoader.load());

		authStage.setScene(scene);
		authStage.initModality(Modality.WINDOW_MODAL);
		authStage.initOwner(primaryStage);
		authStage.setTitle("Authentication");
		authStage.setAlwaysOnTop(true);
		authStage.show();
		
		AuthController chatController = authLoader.getController();
		
		chatController.setNetwork(network);
		chatController.setStartClient(this);
		
	}
	
	private void createChatDialog () throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(StartClient.class.getResource("chat-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 600, 400);
		primaryStage.setScene(scene);
		primaryStage.setAlwaysOnTop(true);
//        stage.show();
		
		chatController = fxmlLoader.getController();
		
		chatController.setNetwork(network);
	}
	
	public static void main (String[] args) {
		launch();
	}
	
	public void openChatDialog () {
		
		authStage.close();
		primaryStage.show();
		primaryStage.setTitle(network.getUsername());
		network.waitMessage(chatController);
		chatController.setUsernameTitle(network.getUsername());
	}
	
	public void showErrorAlert (String title, String errorMessage) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(errorMessage);
		alert.show();
	}
}