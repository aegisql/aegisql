package com.aegisql.dao.sql_information_schema.sql_impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.aegisql.dao.jdbc_utils.Utils;
import com.aegisql.dao.sql_information_schema.Tables;

public class TablesIS implements Tables {

	private Connection connection;
	
	private String selectTablesSql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s'";

	public TablesIS() {	}

	@Override
	public Collection<String> getTables( String schema ) throws SQLException {
		String sql = String.format(selectTablesSql, schema);		
		return Utils.getSingleStringColumn(connection, sql);
	}
	
	public void setSelectSchemasSql(String selectTablesSql) {
		this.selectTablesSql = selectTablesSql;
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
