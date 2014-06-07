package com.aegisql.dao.sql_information_schema.sql_impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.aegisql.dao.jdbc_utils.Utils;
import com.aegisql.dao.sql_information_schema.Schemas;

public class SchemasIS implements Schemas {
	
	Connection connection;
	
	private String selectSchemasSql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA";
	
	public SchemasIS() {	}

	@Override
	public Collection<String> getSchemas() throws SQLException {
		return Utils.getSingleStringColumn(connection, selectSchemasSql);
	}

	public void setSelectSchemasSql(String selectSchemasSql) {
		this.selectSchemasSql = selectSchemasSql;
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public String getCurrentSchema() throws SQLException {
		return Utils.getSingleString(connection, "SELECT SCHEMA()");
	}
}
