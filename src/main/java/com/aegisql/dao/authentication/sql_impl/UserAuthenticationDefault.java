package com.aegisql.dao.authentication.sql_impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.aegisql.authentication.Group;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.dao.Connectable;
import com.aegisql.jdbc42.impl.AegisConnection;

public class UserAuthenticationDefault implements UserAuthentication, Connectable {

	Connection connection;
	private String authenticationSchemaName = "";
	
	public UserAuthenticationDefault() {
		// TODO Auto-generated constructor stub
	}
	/*
	 * 
	 * First group taht match all column requirements win
	 * If statement contains *
	 * group with most matching columns win
	 * 
select G.ID,G.GROUP_NAME,G.DESCRIPTION,IF(G.ID=U.DEFAULT_GROUP_ID,1,0) AS DEFAULT_GROUP from USERS U INNER JOIN GROUPS G INNER JOIN USER_GROUP_MAP UG ON (U.ID = UG.USER_ID AND G.ID = UG.GROUP_ID)
WHERE U.USER_NAME='mike' AND U.PASSWORD='12345' AND U.ENABLED=1 AND G.ENABLED=1 AND (U.PASSWORD_EXPIRATION IS NULL OR U.PASSWORD_EXPIRATION > NOW())
ORDER BY DEFAULT_GROUP DESC;  
*/

	private final static String SELECT_GROUP = 
			"SELECT G.ID,G.GROUP_NAME,IF(G.ID=U.DEFAULT_GROUP_ID,1,0) AS DEFAULT_GROUP, G.ACCESSOR, UG.ACCESSOR_ID "
	       +"FROM %1$sUSERS U INNER JOIN %1$sGROUPS G INNER JOIN %1$sUSER_GROUP_MAP UG "
		   +"ON (U.ID = UG.USER_ID AND G.ID = UG.GROUP_ID) WHERE "
	       +"U.USER_NAME='%2$s' AND U.PASSWORD='%3$s' AND U.ENABLED=1 AND G.ENABLED=1 AND (U.PASSWORD_EXPIRATION IS NULL OR U.PASSWORD_EXPIRATION > NOW()) "
		   +" ORDER BY DEFAULT_GROUP DESC"
	       ;
	
	@Override
	public List<Group> getUserGroups(String userName, String password) throws SQLException {
		List<Group> groups = new ArrayList<Group>();
		String sql = String.format(SELECT_GROUP,authenticationSchemaName,userName,password);
		try( Statement st = connection.createStatement() ) {
			ResultSet rs = st.executeQuery(sql);
			while( rs.next() ) {
				int accessorId = rs.getInt(5);
				Integer accessorIdIneger = rs.wasNull() ? null:accessorId;
				groups.add( new Group(rs.getInt(1),rs.getString(2),rs.getBoolean(3),rs.getString(4),accessorIdIneger));
			}
		}
		
		return groups;
	}

	@Override
	public void setConnection(Connection connection, String authenticationSchemaName) {
		if( connection instanceof AegisConnection ) {
			this.connection = ((AegisConnection) connection).getInnerConnection();			
		} else {
			this.connection = connection;			
		}
		if(authenticationSchemaName.endsWith(".")) {
			this.authenticationSchemaName = authenticationSchemaName;
		} else {
			this.authenticationSchemaName = authenticationSchemaName + ".";			
		}
	}

	@Override
	public List<Group> getUserGroups(String userName, String password, String managedUser) throws SQLException {
		return null;
	}

}
