package com.aegisql.dao.sql_information_schema.sql_impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.aegisql.dao.jdbc_utils.Utils;
import com.aegisql.dao.sql_information_schema.Columns;

public class ColumnsIS implements Columns{

	private Connection connection;
	
	private String selectColumnsSql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'";

	public void setSelectColumnsSql(String selectColumnsSql) {
		this.selectColumnsSql = selectColumnsSql;
	}

	public ColumnsIS() {	}

	@Override
	public List<String> getColumns(String schema, String table) throws SQLException {
		String sql = String.format(selectColumnsSql, schema,table);		
		return Utils.getSingleStringColumn(connection, sql);
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
