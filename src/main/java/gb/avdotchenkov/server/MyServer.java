package gb.avdotchenkov.server;

import gb.avdotchenkov.server.authentication.AuthenticationService;
import gb.avdotchenkov.server.authentication.BaseAuthenticationService;
import gb.avdotchenkov.server.authentication.DBAuthenticationService;
import gb.avdotchenkov.server.handler.ClientHandler;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

public class MyServer {

	public static final Logger logger = Logger.getLogger("server");
	private final ServerSocket serverSocket;
	private final AuthenticationService authenticationService;
	private final List<ClientHandler> clients;

	public MyServer(int port) throws IOException {


		Handler handler = new FileHandler("src/main/resources/Logger/logs/LogFromLogger.log");
		logger.addHandler(handler);
		handler.setLevel(Level.ALL);
		handler.setFormatter(new SimpleFormatter());
//		handler.setFormatter(new Formatter() {
//			@Override public String format (LogRecord record) {
//				return String.format("%s\t%s\t%s%n", record.getLevel(), new Data(record.getMillis()),
//				                     record.getMessage());
//
//			}
//		});


		serverSocket = new ServerSocket(port);
		authenticationService = new DBAuthenticationService();
		clients = new ArrayList<>();
	}


	public void start() {
		authenticationService.startAuthentication();
		logger.log(Level.SEVERE, "СЕРВЕР ЗАПУЩЕН");

		System.out.println("----------------");

		try {
			while (true) {
				waitAndProcessNewClientConnection();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			authenticationService.endAuthentication();
		}
	}

	private void waitAndProcessNewClientConnection() throws IOException {
		logger.log(Level.SEVERE, "ОЖИДАНИЕ КЛИЕНТА!");
		Socket socket = serverSocket.accept();
		logger.log(Level.SEVERE, "Клиент подключился!");
		processClientConnection(socket);
	}

	private void processClientConnection(Socket socket) throws IOException {
		ClientHandler handler = new ClientHandler(this, socket);
		handler.handle();
	}

	public synchronized void subscribe(ClientHandler clientHandler) {
		clients.add(clientHandler);
	}

	public synchronized void unSubscribe(ClientHandler clientHandler) {
		clients.remove(clientHandler);
	}

	public synchronized boolean isUsernameBusy(String username) {
		for (ClientHandler client : clients) {
			if (client.getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public synchronized void broadcastMessage(String message, ClientHandler sender, boolean isServerMessage)
			throws IOException {
		for (ClientHandler client : clients) {
			if (client == sender) {
				continue;
			}
			client.sendMessage(isServerMessage ? null : sender.getUsername(), message);
		}
	}

	public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
		broadcastMessage(message, sender, false);
		sender.chatHistory(message, sender);

	}

	public synchronized void sendPrivateMessage(ClientHandler sender, String recipient, String privateMessage)
			throws IOException {
		for (ClientHandler client : clients) {
			if (client.getUsername().equals(recipient)) {
				client.sendMessage(sender.getUsername(), privateMessage);
			}
		}
	}

	public synchronized void broadcastClients(ClientHandler sender) throws IOException {
		for (ClientHandler client : clients) {

			client.sendServerMessage(String.format("%s присоединился к чату", sender.getUsername()));
			client.sendClientsList(clients);
		}
	}

	public synchronized void broadcastClientDisconnected(ClientHandler sender) throws IOException {
		for (ClientHandler client : clients) {
			if (client == sender) {
				continue;
			}
			client.sendServerMessage(String.format("%s отключился", sender.getUsername()));
			client.sendClientsList(clients);

		}
	}

	public void broadcastMessage(String format) {
	}

	public void updateUsername(ClientHandler clientHandler, String newUsername) {
	}
}
