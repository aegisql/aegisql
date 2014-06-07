package com.aegisql.testing_tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySQLLocalClient extends DatabaseClient {

	public MySQLLocalClient(String connectionUri) throws Exception {
		super(connectionUri);
	}

	@Override
	public void resetDatabase(String dbName) throws SQLException {
		Statement st = connection.createStatement();
		st.execute("DROP DATABASE IF EXISTS " + dbName);
		st.execute("CREATE DATABASE IF NOT EXISTS " + dbName +  " DEFAULT CHARACTER SET = 'utf8'");
		st.close();
	}

	@Override
	public void resetTable( TableCreator tc ) throws SQLException {
		Statement st = connection.createStatement();
		st.execute("DROP TABLE IF EXISTS " + tc.getFullTableName());
		st.execute( tc.toString() );
		st.close();
	}


	
	
	

}
