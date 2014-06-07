package com.aegisql.dao.jdbc_utils;

import java.sql.SQLException;
import java.util.Set;

public interface TableRegistration {
	void removeTableRegistration(String schema, String... tables);
	void addTableRegistration(Set<String> accessors, String schema, String... tables) throws SQLException;
}
