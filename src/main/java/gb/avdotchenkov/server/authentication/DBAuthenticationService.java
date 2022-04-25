package gb.avdotchenkov.server.authentication;

import java.sql.*;

import static gb.avdotchenkov.server.MyServer.logger;


public class DBAuthenticationService implements AuthenticationService {

	private static final String SQLAUTH_BD = "jdbc:sqlite:src/main/resources/bd/AuthenticationBD.db";
	private static Statement stmt;
	private static ResultSet rs;
	private static Connection connection;


	@Override
	public String getUsernameByLoginAndPassword(String login, String password) {
		String passwordDB = null;
		String username = null;

		try {
			PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM auth WHERE login = ?");
			pstmt.setString(1, login);
			rs = pstmt.executeQuery();
			if (rs.isClosed()) {
				return null;
			}

			username = rs.getString("username");
			passwordDB = rs.getString("password");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ((passwordDB != null) && (passwordDB.equals(password))) ? username : null;
	}

	@Override
	public void createUser(String login, String password, String username) {
		try {
			PreparedStatement pstmt =
					connection.prepareStatement("INSERT INTO auth (login, password, username) VALUES (?, ?, ?)");

			pstmt.setString(1, login);
			pstmt.setString(2, password);
			pstmt.setString(3, username);

			pstmt.addBatch();

			pstmt.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}
	}

//	@Override public void updateUsername (String login, String newUsername) throws SQLException {
//
//	}


	@Override
	public Boolean checkLoginByFree(String login) {
		String username = null;
		try {
			PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM auth WHERE login = ?");
			pstmt.setString(1, login);
			rs = pstmt.executeQuery();
			if (rs.isClosed()) {
				return true;
			}

			username = rs.getString("username");
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}

		return username == null;
	}

	@Override
	public void startAuthentication() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection(SQLAUTH_BD);
			stmt = connection.createStatement();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}
	}

	@Override
	public void endAuthentication() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}
	}
}