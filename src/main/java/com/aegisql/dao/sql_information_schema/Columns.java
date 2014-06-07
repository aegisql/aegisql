package com.aegisql.dao.sql_information_schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface Columns {
	List<String> getColumns(String schema, String table) throws SQLException;
	void setConnection( Connection connection );
}
