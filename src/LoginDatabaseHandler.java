import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles all database-related actions. Uses singleton design pattern.
 *
 * @see LoginServer
 */
public class LoginDatabaseHandler
{

	/** A {@link org.apache.log4j.Logger log4j} logger for debugging. */
	private static Logger log = LogManager.getLogger();

	/** Makes sure only one database handler is instantiated. */
	private static LoginDatabaseHandler singleton = new LoginDatabaseHandler();

	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_SQL = "SHOW TABLES LIKE 'login_users';";

	private static final String SEARCH_TABLE_SQL = "SHOW TABLES LIKE 'search_history'";

	/** Used to create necessary tables for this example. */
	private static final String CREATE_SQL = "CREATE TABLE login_users ("
			+ "userid INTEGER AUTO_INCREMENT PRIMARY KEY, " 
			+ "username VARCHAR(32) NOT NULL UNIQUE, "
			+ "password CHAR(64) NOT NULL, " 
			+ "usersalt CHAR(32) NOT NULL, " 
			+ "lastlogin CHAR(64) NOT NULL);";

	/** Used to insert a new user into the database. */
	private static final String REGISTER_SQL = "INSERT INTO login_users " 
			+ "(username, password, usersalt, lastlogin)"
			+ "VALUES (?, ?, ?, NOW());";

	/** Used to determine if a username already exists. */
	private static final String USER_SQL = "SELECT username FROM login_users WHERE username = ?";

	/** Used to retrieve the salt associated with a specific user. */
	private static final String SALT_SQL = "SELECT usersalt FROM login_users WHERE username = ?";

	/** Used to authenticate a user. */
	private static final String AUTH_SQL = "SELECT username FROM login_users " + "WHERE username = ? AND password = ?";

	/** Used to remove a user from the database. */
	private static final String DELETE_SQL = "DELETE FROM login_users WHERE username = ?";

	/** Used to update login time for a specified used*/
	private static final String UPDATE_LOGIN_TIME_SQL = "UPDATE login_users SET lastlogin = NOW()"
			+ "WHERE username = ?;";

	/** Used to return the last time a specified user logged in*/
	private static final String GET_LOGIN_TIME_SQL = "SELECT DISTINCT(lastlogin) FROM login_users"
			+ "WHERE username= ?;";

	/** Creates a search history table */
	private static final String CREATE_HISTORY_SQL = "CREATE TABLE search_history ("
			+ "userid INTEGER AUTO_INCREMENT PRIMARY KEY," + " username VARCHAR(32) NOT NULL,"
			+ " query CHAR(64) NOT NULL," + " time CHAR(64) NOT NULL);";

	/** Adds queries that a user searched for to their history */
	private static final String ADD_HISTORY_SQL = "INSERT INTO search_history (username, query, time)"
			+ "VALUES (?, ?, NOW());";

	/** Gets the history of a specified user */
	private static final String GET_HISTORY_SQL = "SELECT CONCAT(time, '&nbsp;&nbsp;&nbsp;', query)"
			+ "AS full_history FROM search_history WHERE username= ? ORDER BY time DESC;";

	/** Clears the history of a specified user */
	private static final String REMOVE_HISTORY_SQL = "DELETE FROM search_history WHERE username = ?;";
	
	/** Changes the password for the specified user */
	private static final String CHANGE_PASSWORD_SQL = "UPDATE login_users SET password = ?, usersalt = ? "
			+ "WHERE username = ?;";

	/** Used to configure connection to database. */
	private DatabaseConnector db;

	/** Used to generate password hash salt for user. */
	private Random random;

	/**
	 * Initializes a database handler for the Login example. Private constructor
	 * forces all other classes to use singleton.
	 */
	private LoginDatabaseHandler()
	{
		Status status = Status.OK;
		random = new Random(System.currentTimeMillis());

		try
		{
			db = new DatabaseConnector("database.properties");
			status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;
		}
		catch(FileNotFoundException e)
		{
			status = Status.MISSING_CONFIG;
		}
		catch(IOException e)
		{
			status = Status.MISSING_VALUES;
		}

		if(status != Status.OK)
		{
			log.fatal(status.message());
		}
	}

	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static LoginDatabaseHandler getInstance()
	{
		return singleton;
	}

	/**
	 * Checks to see if a String is null or empty.
	 * 
	 * @param text
	 *            - String to check
	 * @return true if non-null and non-empty
	 */
	public static boolean isBlank(String text)
	{
		return (text == null) || text.trim().isEmpty();
	}

	/**
	 * Returns the hex encoding of a byte array.
	 *
	 * @param bytes
	 *            - byte array to encode
	 * @param length
	 *            - desired length of encoding
	 * @return hex encoded byte array
	 */
	public static String encodeHex(byte[] bytes, int length)
	{
		BigInteger bigint = new BigInteger(1, bytes);
		String hex = String.format("%0" + length + "X", bigint);

		assert hex.length() == length;
		return hex;
	}

	/**
	 * Calculates the hash of a password and salt using SHA-256.
	 *
	 * @param password
	 *            - password to hash
	 * @param salt
	 *            - salt associated with user
	 * @return hashed password
	 */
	public static String getHash(String password, String salt)
	{
		String salted = salt + password;
		String hashed = salted;

		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salted.getBytes());
			hashed = encodeHex(md.digest(), 64);
		}
		catch(Exception ex)
		{
			log.debug("Unable to properly hash password.", ex);
		}
		return hashed;
	}

	/**
	 * Checks if necessary table exists in database, and if not tries to create
	 * it.
	 *
	 * @return {@link Status.OK} if table exists or create is successful
	 */
	private Status setupTables()
	{
		Status status = Status.ERROR;

		try(Connection connection = db.getConnection(); Statement statement = connection.createStatement();)
		{
			if(!statement.executeQuery(TABLES_SQL).next())
			{
				// Table missing, must create
				log.debug("Creating tables...");
				statement.executeUpdate(CREATE_SQL);

				// Check if create was successful
				if(!statement.executeQuery(TABLES_SQL).next())
				{
					System.out.println("here1");
					status = Status.CREATE_FAILED;
				}
				else
				{
					status = Status.OK;
				}
			}
			else
			{
				log.debug("Tables found.");
				status = Status.OK;
			}

			if(!statement.executeQuery(SEARCH_TABLE_SQL).next())
			{
				// Table missing, must create
				log.debug("Creating search history table...");
				statement.executeUpdate(CREATE_HISTORY_SQL);

				// Check if create was successful
				if(!statement.executeQuery(CREATE_HISTORY_SQL).next())
				{
					status = Status.CREATE_FAILED;
				}
				else
				{
					status = Status.OK;
				}
			}
			else
			{
				log.debug("Tables found.");
				status = Status.OK;
			}
		}
		catch(Exception ex)
		{
			status = Status.CREATE_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	/**
	 * Tests if a user already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - username to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status duplicateUser(Connection connection, String user)
	{

		assert connection != null;
		assert user != null;

		Status status = Status.ERROR;

		try(PreparedStatement statement = connection.prepareStatement(USER_SQL);)
		{
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_USER : Status.OK;
		}
		catch(SQLException e)
		{
			log.debug(e.getMessage(), e);
			status = Status.SQL_EXCEPTION;
		}
		return status;
	}

	/**
	 * Tests if a user already exists in the database.
	 *
	 * @see #duplicateUser(Connection, String)
	 * @param user
	 *            - username to check
	 * @return Status.OK if user does not exist in database
	 */
	public Status duplicateUser(String user)
	{
		Status status = Status.ERROR;

		try(Connection connection = db.getConnection();)
		{
			status = duplicateUser(connection, user);
		}
		catch(SQLException e)
		{
			status = Status.CONNECTION_FAILED;
			log.debug(e.getMessage(), e);
		}
		return status;
	}

	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            - username of new user
	 * @param newpass
	 *            - password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	private Status registerUser(Connection connection, String newuser, String newpass)
	{

		Status status = Status.ERROR;

		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);

		String usersalt = encodeHex(saltBytes, 32);
		String passhash = getHash(newpass, usersalt);

		try(PreparedStatement statement = connection.prepareStatement(REGISTER_SQL);)
		{
			statement.setString(1, newuser);
			statement.setString(2, passhash);
			statement.setString(3, usersalt);
			statement.executeUpdate();

			status = Status.OK;
		}
		catch(SQLException ex)
		{
			System.out.println("here");
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}

	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            - username of new user
	 * @param newpass
	 *            - password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	public Status registerUser(String newuser, String newpass)
	{
		Status status = Status.ERROR;
		log.debug("Registering " + newuser + ".");

		// make sure we have non-null and non-emtpy values for login
		if(isBlank(newuser) || isBlank(newpass))
		{
			status = Status.INVALID_LOGIN;
			log.debug(status);
			return status;
		}

		// try to connect to database and test for duplicate user
		try(Connection connection = db.getConnection();)
		{
			status = duplicateUser(connection, newuser);

			// if okay so far, try to insert new user
			if(status == Status.OK)
			{
				status = registerUser(connection, newuser, newpass);
			}
		}
		catch(SQLException ex)
		{
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	/**
	 * Gets the salt for a specific user.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - which user to retrieve salt for
	 * @return salt for the specified user or null if user does not exist
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private String getSalt(Connection connection, String user) throws SQLException
	{
		assert connection != null;
		assert user != null;

		String salt = null;

		try(PreparedStatement statement = connection.prepareStatement(SALT_SQL);)
		{
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();

			if(results.next())
			{
				salt = results.getString("usersalt");
			}
		}
		return salt;
	}

	/**
	 * Checks if the provided username and password match what is stored in the
	 * database. Requires an active database connection.
	 *
	 * @param username
	 *            - username to authenticate
	 * @param password
	 *            - password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 * @throws SQLException
	 */
	private Status authenticateUser(Connection connection, String username, String password) throws SQLException
	{

		Status status = Status.ERROR;

		try(PreparedStatement statement = connection.prepareStatement(AUTH_SQL);)
		{
			String usersalt = getSalt(connection, username);
			String passhash = getHash(password, usersalt);

			statement.setString(1, username);
			statement.setString(2, passhash);

			ResultSet results = statement.executeQuery();
			status = results.next() ? status = Status.OK : Status.INVALID_LOGIN;
		}
		catch(SQLException e)
		{
			log.debug(e.getMessage(), e);
			status = Status.SQL_EXCEPTION;
		}
		return status;
	}

	/**
	 * Checks if the provided username and password match what is stored in the
	 * database. Must retrieve the salt and hash the password to do the
	 * comparison.
	 *
	 * @param username
	 *            - username to authenticate
	 * @param password
	 *            - password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 */
	public Status authenticateUser(String username, String password)
	{
		Status status = Status.ERROR;

		log.debug("Authenticating user " + username + ".");

		try(Connection connection = db.getConnection();)
		{
			status = authenticateUser(connection, username, password);
		}
		catch(SQLException ex)
		{
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username
	 *            - username to remove
	 * @param password
	 *            - password of user
	 * @return {@link Status.OK} if removal successful
	 */
	private Status removeUser(Connection connection, String username, String password)
	{
		Status status = Status.ERROR;

		try(PreparedStatement statement = connection.prepareStatement(DELETE_SQL);)
		{
			statement.setString(1, username);

			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		}
		catch(SQLException ex)
		{
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
		return status;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username
	 *            - username to remove
	 * @param password
	 *            - password of user
	 * @return {@link Status.OK} if removal successful
	 */
	public Status removeUser(String username, String password)
	{
		Status status = Status.ERROR;

		log.debug("Removing user " + username + ".");

		try(Connection connection = db.getConnection();)
		{
			status = authenticateUser(connection, username, password);

			if(status == Status.OK)
			{
				status = removeUser(connection, username, password);
			}
		}
		catch(Exception ex)
		{
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	/**
	 * Updates the last login time for the user who signed in.
	 * 
	 * @param connection
	 * @param user
	 * 			user to update time for
	 * @return
	 */
	private Status updateLastLoginTime(Connection connection, String user)
	{
		Status status = Status.ERROR;

		try(PreparedStatement statement = connection.prepareStatement(UPDATE_LOGIN_TIME_SQL);)
		{

			statement.setString(1, user);
			statement.executeUpdate();

			status = Status.OK;
		}
		catch(SQLException ex)
		{
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}

	/**
	 * Updates the last login time for the user who signed in.
	 * 
	 * @param user
	 * 			user to update time for
	 * @return
	 */
	public Status updateLastLoginTime(String user)
	{
		Status status = Status.ERROR;
		if(isBlank(user))
		{
			status = Status.INVALID_LOGIN;
			return status;
		}

		try(Connection connection = db.getConnection();)
		{
			status = updateLastLoginTime(connection, user);
		}
		catch(SQLException ex)
		{
			status = Status.CONNECTION_FAILED;
		}
		return status;
	}

	/**
	 * Returns the last time a user logged in
	 * 
	 * @param connection
	 * @param username
	 * 				user to show time for
	 * @param out
	 * @return
	 */
	private Status getLastLoginTime(Connection connection, String username, PrintWriter out)
	{
		Status status = Status.ERROR;

		try(PreparedStatement statement = connection.prepareStatement(GET_LOGIN_TIME_SQL);)
		{
			statement.setString(1, username);
			status = Status.OK;
			ResultSet time = statement.executeQuery();
			while((time != null) && time.next())
			{
				out.printf("<p>" + username + ", your last visit was on " + time.getString("lastlogin") + "<p>%n");
			}
		}
		catch(SQLException e)
		{
			status = Status.SQL_EXCEPTION;
			log.debug(e.getMessage(), e);
		}
		return status;
	}

	/**
	 * Returns the last time a user logged in
	 * 
	 * @param connection
	 * @param username
	 * 				user to show time for
	 * @param out
	 * @return
	 */
	public Status getLastLoginTime(String username, PrintWriter out)
	{
		Status status = Status.ERROR;

		if(isBlank(username))
		{
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try(Connection connection = db.getConnection();)
		{
			status = getLastLoginTime(connection, username, out);
		}
		catch(SQLException e)
		{
			status = Status.CONNECTION_FAILED;
			log.debug(status, e);
		}
		return status;
	}

	/**
	 * Adds a particular search query to a particular users history
	 * 
	 * @param connection
	 * @param username
	 * 			user to add query to history
	 * @param query
	 * 			the search query to be added
	 * @return
	 */
	private Status addSearchHistory(Connection connection, String username, String query)
	{
		Status status = Status.ERROR;

		try(PreparedStatement statement = connection.prepareStatement(ADD_HISTORY_SQL);)
		{
			statement.setString(1, username);
			statement.setString(2, query);
			statement.executeUpdate();

			status = Status.OK;
		}
		catch(SQLException e)
		{
			status = Status.SQL_EXCEPTION;
			log.debug(e.getMessage(), e);
		}
		return status;
	}

	/**
	 * Adds a a particular search query to a particular users history
	 * 
	 * @param username
	 * 			user to add query to history
	 * @param query
	 * 			the search query to be added
	 * @return
	 */
	public Status addSearchHistory(String username, String query)
	{
		Status status = Status.ERROR;

		if(isBlank(query) || isBlank(username))
		{
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}

		try(Connection connection = db.getConnection();)
		{
			status = addSearchHistory(connection, username, query);
		}
		catch(SQLException e)
		{
			status = Status.CONNECTION_FAILED;
			log.debug(status, e);
		}
		return status;
	}

	/**
	 * Returns the search history for a specified user
	 * 
	 * @param connection
	 * @param username
	 * 			user who we need to grab the history for
	 * @param out
	 * @return
	 */
	private Status getHistory(Connection connection, String username, PrintWriter out)
	{
		Status status = Status.ERROR;

		try(PreparedStatement statement = connection.prepareStatement(GET_HISTORY_SQL);)
		{

			statement.setString(1, username);

			status = Status.OK;

			ResultSet searchHistory = statement.executeQuery();

			int size = 0;

			while((searchHistory != null) && searchHistory.next())
			{
				out.printf("\t<p>%s </p>", searchHistory.getString("full_history"));
				size++;
			}
			if(size == 0)
			{
				out.printf("%n<p>You have no search history<p>");
			}

		}
		catch(SQLException e)
		{
			status = Status.SQL_EXCEPTION;
			log.debug(e.getMessage(), e);
		}
		return status;
	}

	/**
	 * Returns the search history for a specified user
	 * 
	 * @param username
	 * 			user who we need to grab the history for
	 * @param out
	 * @return
	 */
	public Status getHistory(String username, PrintWriter out)
	{

		Status status = Status.ERROR;

		if(isBlank(username))
		{
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try(Connection connection = db.getConnection();)
		{

			status = getHistory(connection, username, out);

		}
		catch(SQLException ex)
		{
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Clears the history for a specified user
	 * @param connection
	 * @param username
	 * 			User whose history is to be cleared
	 * @return
	 */
	private Status clearHistory(Connection connection, String username)
	{
		Status status = Status.ERROR;
		try(PreparedStatement statement = connection.prepareStatement(REMOVE_HISTORY_SQL);)
		{
			statement.setString(1, username);
			statement.executeUpdate();
			status = Status.OK;
		}
		catch(SQLException e)
		{
			status = Status.SQL_EXCEPTION;
			log.debug(e.getMessage(), e);
		}
		return status;
	}

	/**
	 * Clears the history for a specified user
	 * @param connection
	 * @param username
	 * 			User whose history is to be cleared
	 * @return
	 */
	public Status clearHistory(String username)
	{
		Status status = Status.ERROR;

		if(isBlank(username))
		{
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try(Connection connection = db.getConnection();)
		{
			status = clearHistory(connection, username);
		}
		catch(SQLException ex)
		{
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}
	
	public Status changePassword(String user, String newpass)
	{
		Status status = Status.ERROR;
		
		if (isBlank(user) || isBlank(newpass))
		{
			status = Status.INVALID_LOGIN;
			log.debug(status);
			return status;
		}
		
		try (Connection connection = db.getConnection();)
		{
			status = changePassword(connection, user, newpass);
		} 
		catch (SQLException ex)
		{
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}
	
	private Status changePassword(Connection connection, String user, String newPassword)
	{

		Status status = Status.ERROR;
		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);

		String usersalt = encodeHex(saltBytes, 32);
		String passhash = getHash(newPassword, usersalt);

		try (PreparedStatement statement = connection
				.prepareStatement(CHANGE_PASSWORD_SQL);)
		{
			statement.setString(1, passhash);
			statement.setString(2, usersalt);
			statement.setString(3, user);
			statement.executeUpdate();

			status = Status.OK;
		} 
		catch (SQLException ex)
		{
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}
}