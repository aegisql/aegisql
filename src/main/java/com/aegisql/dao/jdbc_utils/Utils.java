package com.aegisql.dao.jdbc_utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static List<String> getSingleStringColumn(Connection connection, String sql) throws SQLException {
		List<String> result = new ArrayList<String>();
		try ( Statement st = connection.createStatement() ) {
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				String val = rs.getString(1);
				result.add(val);
			}
		}
		return result;
	}

	public static String getSingleString(Connection connection, String sql) throws SQLException {
		List<String> result = getSingleStringColumn(connection,sql);
		if( result.size() > 1) throw new SQLException("Returned more then 1 records");
		if(result.size() == 0 ) return null;
		return result.get(0);
	}

}
