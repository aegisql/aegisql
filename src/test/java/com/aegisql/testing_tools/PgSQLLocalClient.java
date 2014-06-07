package com.aegisql.testing_tools;

import java.sql.SQLException;
import java.sql.Statement;

public class PgSQLLocalClient extends DatabaseClient {

	//jdbc:postgresql://host:port/database  jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true
	public PgSQLLocalClient(String connectionUri) throws Exception {
		super(connectionUri);
	}

	@Override
	public void resetDatabase(String dbName) throws SQLException {
		Statement st = connection.createStatement();
		st.execute("DROP SCHEMA IF EXISTS " + dbName + " CASCADE");
		st.execute("CREATE SCHEMA " + dbName);
		st.close();
	}

	@Override
	public void resetTable(TableCreator tc) throws SQLException {
		Statement st = connection.createStatement();
		st.execute("DROP TABLE IF EXISTS " + tc.getFullTableName());
		st.execute( tc.toString() );
		st.close();
	}

}
