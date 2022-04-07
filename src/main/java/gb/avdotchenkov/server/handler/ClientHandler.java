package gb.avdotchenkov.server.handler;

import gb.avdotchenkov.server.MyServer;
import gb.avdotchenkov.server.authentication.AuthenticationService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class ClientHandler {
	
	private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
	private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
	private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
	private static final String CLIENT_MSG_CMD_PREFIX = "/cm"; // + msg
	private static final String SERVER_MSG_CMD_PREFIX = "/sm"; // + msg
	private static final String PRIVATE_MSG_CMD_PREFIX = "/pm"; // + client + msg
	private static final String STOP_SERVER_CMD_PREFIX = "/stop";
	private static final String END_CLIENT_CMD_PREFIX = "/end"; // + msg
	private static final Object GET_CLIENTS_CMD_PREFIX = "/getclients";//+ username;
	private static final String UPDATE_NEW_USERNAME_CMD_PREFIX = "/upd"; // + login + username
	private static final String NEW_USER_REGISTRATION_CMD_PREFIX = "/reg"; // + login + password + username
	
	private MyServer myServer;
	private Socket clientSocket;
	private DataOutputStream out;
	private DataInputStream in;
	private String username;
	
	public ClientHandler (MyServer myServer, Socket socket) {
		
		
		this.myServer = myServer;
		clientSocket = socket;
	}
	
	public void handle () throws IOException {
		out = new DataOutputStream(clientSocket.getOutputStream());
		in = new DataInputStream(clientSocket.getInputStream());
		
		new Thread(() -> {
			try {
				authentication();
				readMessage();
			} catch (IOException | SQLException e) {
				e.printStackTrace();
				myServer.unSubscribe(this);
				try {
					myServer.broadcastClientDisconnected(this);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}).start();
	}
	
	private void authentication () throws IOException, SQLException {
		while (true) {
			String message = in.readUTF();
			if (message.startsWith(AUTH_CMD_PREFIX)) {
				boolean isSuccessAuth = processAuthentication(message);
				if (isSuccessAuth) {
					break;
				}
				
			} else {
				out.writeUTF(AUTHERR_CMD_PREFIX + " Ошибка аутентификации");
				System.out.println("Неудачная попытка аутентификации");
			}
		}
	}
	
	private boolean processAuthentication (String message) throws IOException, SQLException {
		String[] parts = message.split("\\s+");
		if (parts.length != 3) {
			out.writeUTF(AUTHERR_CMD_PREFIX + " Ошибка аутентификации");
		}
		String login = parts[1];
		String password = parts[2];
		
		AuthenticationService auth = myServer.getAuthenticationService();
		
		username = auth.getUsernameByLoginAndPassword(login, password);
		
		if (username != null) {
			if (myServer.isUsernameBusy(username)) {
				out.writeUTF(AUTHERR_CMD_PREFIX + " Логин уже используется");
				return false;
			}
			
			out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
			
			connectUser(username);
			
			return true;
		} else {
			out.writeUTF(AUTHERR_CMD_PREFIX + " Логин или пароль не соответствуют действительности");
			return false;
		}
	}
	
	private void connectUser (String username) throws IOException {
		myServer.subscribe(this);
		System.out.println("Пользователь " + username + " подключился к чату");
		myServer.broadcastClients(this);
	}
	
	private void readMessage () throws IOException {
		while (true) {
			String message = in.readUTF();
			System.out.println("message | " + username + ": " + message);
			if (message.startsWith(STOP_SERVER_CMD_PREFIX)) {
				System.exit(0);
				//почему данный метот не работает ?  он хочет что бы метод был статик, но тогда он не работает ни где
				// , постоянно ркансым, и требует что бы я стелал какойто метод в классе AuthenticationService,но он
				// же интерфейс, почему я там должен что то делать ? ничего не понятно...
//			}else if (message.startsWith(UPDATE_NEW_USERNAME_CMD_PREFIX)){
//		String[] part = message.split("\\s", 4);
//		String login = part[1];
//		String newUsername = part[2];
//				try {
//					AuthenticationService.updateUsername(login, newUsername);
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//				myServer.broadcastMessage(String.format("Пользователь %s сменил ни на %s", login, newUsername));
			
			} else if (message.startsWith(END_CLIENT_CMD_PREFIX)) {
				return;
			} else if (message.startsWith(PRIVATE_MSG_CMD_PREFIX)) {
				String[] parts = message.split("\\s+", 3);
				String recipient = parts[1];
				String privateMessage = parts[2];
				
				myServer.sendPrivateMessage(this, recipient, privateMessage);
			} else {
				myServer.broadcastMessage(message, this);
			}
			
		}
	}
	
	public void sendMessage (String sender, String message) throws IOException {
		if (sender != null) {
			sendClientMessage(sender, message);
		} else {
			sendServerMessage(message);
		}
	}
	
	public void sendServerMessage (String message) throws IOException {
		out.writeUTF(String.format("%s %s", SERVER_MSG_CMD_PREFIX, message));
	}
	
	public void sendClientMessage (String sender, String message) throws IOException {
		out.writeUTF(String.format("%s %s %s", CLIENT_MSG_CMD_PREFIX, sender, message));
	}
	
	public String getUsername () {
		return username;
	}
	
	
	public void sendClientsList (List <ClientHandler> clients) throws IOException {
		String msg = String.format("%s %s", GET_CLIENTS_CMD_PREFIX, clients.toString());
		out.writeUTF(msg);
		System.out.println(msg);
	}
	
	@Override public String toString () {
		return username;
	}
}