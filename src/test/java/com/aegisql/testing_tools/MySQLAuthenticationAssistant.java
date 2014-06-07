package com.aegisql.testing_tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import com.aegisql.jdbc42.impl.AegisConnection;

public class MySQLAuthenticationAssistant {

	private Connection connection;
	private String authenticationSchemaName = "AUTHENTICATION";

	private final static String USERS = ""
			+ "CREATE TABLE IF NOT EXISTS `%s`.`USERS` ("
			+ "	`ID` INT NOT NULL AUTO_INCREMENT,"
			+ "	`USER_NAME` VARCHAR(45) NOT NULL,"
			+ "	`DEFAULT_GROUP_ID` INT NULL,"
			+ "	`ENABLED` TINYINT NOT NULL DEFAULT 0,"
			+ "	`PASSWORD` VARCHAR(45) NOT NULL,"
			+ "	`PASSWORD_EXPIRATION` DATETIME NULL,"
			+ "	`CREATION_TIMESTAMP` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
			+ "	PRIMARY KEY (`ID`),"
			+ "	UNIQUE INDEX `USER_NAME_UNIQUE` (`USER_NAME` ASC))"
			+ " ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8";
	private final static String GROUPS = ""
			+ "CREATE TABLE IF NOT EXISTS `%s`.`GROUPS` ("
			+ "	`ID` int(11) NOT NULL AUTO_INCREMENT,"
			+ "	`GROUP_NAME` varchar(45) NOT NULL,"
			+ "	`ENABLED` tinyint(4) NOT NULL DEFAULT '0',"
			+ "	`DESCRIPTION` varchar(45) NOT NULL DEFAULT '',"
			+ "	`ACCESSOR` varchar(45) DEFAULT NULL,"
			+ "	`CREATION_TIMESTAMP` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
			+ "	PRIMARY KEY (`ID`)"
			+ ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8";
	private final static String USER_GROUP_MAP = ""
			+ "CREATE TABLE IF NOT EXISTS `%s`.`USER_GROUP_MAP` ("
			+ " `ID` INT NOT NULL AUTO_INCREMENT," + " `USER_ID` INT NOT NULL,"
			+ " `GROUP_ID` INT NOT NULL," + " `ACCESSOR_ID` INT NULL,"
			+ " PRIMARY KEY (`ID`)," + " INDEX `USER_IDX` (`USER_ID` ASC))"
			+ " ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8";

	public MySQLAuthenticationAssistant() {
		authenticationSchemaName = "AUTHENTICATION";
	}

	public MySQLAuthenticationAssistant( String authenticationSchema ) {
		authenticationSchemaName = authenticationSchema;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String getAuthenticationSchemaName() {
		return authenticationSchemaName;
	}

	public void setAuthenticationSchemaName(String authenticationSchemaName) {
		this.authenticationSchemaName = authenticationSchemaName;
	}

	public void setConnection(Connection connection,
			String authenticationSchemaName) {
		if (connection instanceof AegisConnection) {
			this.connection = ((AegisConnection) connection)
					.getInnerConnection();
		} else {
			this.connection = connection;
		}
		this.authenticationSchemaName = authenticationSchemaName;
	}

	public void dropAuthenticationSchema() {

	}

	public void resetAuthenticationSchema() throws SQLException {
		try (Statement st = connection.createStatement()) {
			st.execute("DROP DATABASE IF EXISTS " + authenticationSchemaName);
			st.execute("CREATE DATABASE IF NOT EXISTS "
					+ authenticationSchemaName
					+ " DEFAULT CHARACTER SET = 'utf8'");
		}
		createTable(String.format(USERS, authenticationSchemaName));
		createTable(String.format(GROUPS, authenticationSchemaName));
		createTable(String.format(USER_GROUP_MAP, authenticationSchemaName));
	}

	public void createUser(String userName, String password,
			String passwordExpirationDate, boolean enabled, int defaultGroupId)
			throws SQLException {
		String SQL = "INSERT INTO "+authenticationSchemaName+".USERS (USER_NAME,PASSWORD,PASSWORD_EXPIRATION,ENABLED,DEFAULT_GROUP_ID) VALUES(?,?,?,?,?)";
		int e = enabled ? 1 : 0;
		try (PreparedStatement st = connection.prepareStatement(SQL)) {
			st.setString(1, userName);
			st.setString(2, password);
			st.setTimestamp(3, Timestamp.valueOf(passwordExpirationDate));
			st.setInt(4, e);
			st.setInt(5, defaultGroupId);
			st.executeUpdate();
		}
	}

	public void createGroup(String groupName, String description,
			String accessor, boolean enabled) throws SQLException {
		String SQL = "INSERT INTO "+authenticationSchemaName+".GROUPS (GROUP_NAME,DESCRIPTION,ACCESSOR,ENABLED) VALUES(?,?,?,?)";
		int e = enabled ? 1 : 0;
		try (PreparedStatement st = connection.prepareStatement(SQL)) {
			st.setString(1, groupName);
			st.setString(2, description);
			st.setString(3, accessor);
			st.setInt(4, e);
			st.executeUpdate();
		}
	}

	public void mapUserToGroup(int userId, int groupId, Integer accessorId)
			throws SQLException {
		String SQL = "INSERT INTO "+authenticationSchemaName+".USER_GROUP_MAP (USER_ID,GROUP_ID,ACCESSOR_ID) VALUES(?,?,?)";
		try (PreparedStatement st = connection.prepareStatement(SQL)) {
			st.setInt(1, userId);
			st.setInt(2, groupId);
			if (accessorId != null) {
				st.setInt(3, accessorId);
			} else {
				st.setNull(3, Types.INTEGER);
			}
			st.executeUpdate();
		}
	}

	public void dropTable(String table) throws SQLException {
		try (Statement st = connection.createStatement()) {
			st.execute("DROP TABLE IF EXISTS " + table);
		}
	}

	private void createTable(String createTable) throws SQLException {
		try (Statement st = connection.createStatement()) {
			st.execute(createTable);
		}
	}

}
