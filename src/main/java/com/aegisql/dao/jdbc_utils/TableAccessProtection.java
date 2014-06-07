package com.aegisql.dao.jdbc_utils;

import java.sql.SQLException;

import com.aegisql.access.AccessPattern;
import com.aegisql.access.ColumnAccess;
import com.aegisql.access.TableAccess;

public interface TableAccessProtection {
		
	void removeProtection(AccessPattern accessor, String schema, String table) throws SQLException;
	void removeProtection(AccessPattern accessor, String table) throws SQLException;
	void addProtection(AccessPattern accessor, String schema, TableAccess tableAccess, ColumnAccess... columns) throws SQLException;
	void addProtection(AccessPattern accessor, TableAccess tableAccess, ColumnAccess... columns) throws SQLException;
	
}
