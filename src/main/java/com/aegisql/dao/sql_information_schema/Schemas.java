package com.aegisql.dao.sql_information_schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public interface Schemas {
	Collection<String> getSchemas() throws SQLException;
	void setConnection( Connection connection);
	String getCurrentSchema() throws SQLException;
}
