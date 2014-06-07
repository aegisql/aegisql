package com.aegisql.dao.sql_information_schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public interface Tables {
	Collection<String> getTables( String schema ) throws SQLException;
	void setConnection(Connection connection);
}
