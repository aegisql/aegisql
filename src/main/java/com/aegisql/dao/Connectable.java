package com.aegisql.dao;

import java.sql.Connection;

public interface Connectable {
	void setConnection(Connection connection, String defaultSchema);
}
