package gb.avdotchenkov.client.controllers;

import gb.avdotchenkov.client.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class ChatController {
	
	@FXML private ListView <String> userList;
	
	@FXML private Label usernameTitle;
	
	@FXML private TextArea chatHistory;
	
	@FXML private TextField inputField;
	
	@FXML private Button sendButton;
	private String selectedRecipient;
	
	@FXML public void initialize () {

//        usersList.setItems(FXCollections.observableArrayList("Тимофей", "Дмитрий", "Диана", "Арман"));
		
		sendButton.setOnAction(event -> sendMessage());
		inputField.setOnAction(event -> sendMessage());
		
		userList.setCellFactory(lv -> {
			MultipleSelectionModel <String> selectionModel = userList.getSelectionModel();
			ListCell <String> cell = new ListCell <>();
			cell.textProperty().bind(cell.itemProperty());
			cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
				userList.requestFocus();
				if (! cell.isEmpty()) {
					int index = cell.getIndex();
					if (selectionModel.getSelectedIndices().contains(index)) {
						selectionModel.clearSelection(index);
						selectedRecipient = null;
					} else {
						selectionModel.select(index);
						selectedRecipient = cell.getItem();
					}
					event.consume();
				}
			});
			return cell;
		});
		
	}
	
	private Network network;
	
	public void setNetwork (Network network) {
		this.network = network;
	}
	
	private void sendMessage () {
		String message = inputField.getText().trim();
		inputField.clear();
		
		
		if (message.trim().isEmpty()) {
			return;
		}
		
		if (selectedRecipient != null) {
			network.sendPrivateMessage(selectedRecipient, message);
		} else {
			network.sendMessage(message);
		}
		
		appendMessage("Я: " + message);
	}
	
	public void appendMessage (String message) {
		String timeStamp = DateFormat.getInstance().format(new Date());
		
		chatHistory.appendText(timeStamp);
		chatHistory.appendText(System.lineSeparator());
		chatHistory.appendText(message);
		chatHistory.appendText(System.lineSeparator());
		chatHistory.appendText(System.lineSeparator());
	}
	
	public void appendServerMessage (String serverMessage) {
		chatHistory.appendText("ВНИМАНИЕ: " + serverMessage);
		chatHistory.appendText(System.lineSeparator());
		chatHistory.appendText(System.lineSeparator());
	}
	
	public void setUsernameTitle (String username) {
		this.usernameTitle.setText(username);
	}
	
	
	public void updateUsersList (String[] users) {
		
		Arrays.sort(users);
		
		for (int i = 0; i < users.length; i++) {
			if (users[i].equals(network.getUsername())) {
				users[i] = ">>> " + users[i];
			}
		}
		
		userList.getItems().clear();
		Collections.addAll(userList.getItems(), users);
		
	}
}
