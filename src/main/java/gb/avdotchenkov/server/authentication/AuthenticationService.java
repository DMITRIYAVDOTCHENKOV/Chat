package gb.avdotchenkov.server.authentication;

import java.sql.SQLException;

public interface AuthenticationService {
	
	String getUsernameByLoginAndPassword (String login, String password) throws SQLException;
	
	void createUser (String login, String password, String username);
	
	//не хочет работать данный метот, не могу понять в чем дело, если просто делаю void, то в классе ClientHandler он
// ругается говорит статик подовай...
//	static void updateUsername (String login, String newUsername) throws SQLException;
//
	Boolean checkLoginByFree (String login);
	
	void startAuthentication ();
	
	void endAuthentication ();
	
}
