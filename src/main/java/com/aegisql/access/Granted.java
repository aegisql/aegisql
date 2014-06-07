package com.aegisql.access;

import java.sql.SQLException;

public interface Granted {
	GrantedAccess getGrantedAccess(String schema, String table, AccessPattern accessor) throws SQLException;
	GrantedAccess getGrantedAccess(String table, AccessPattern accessor) throws SQLException;
}
