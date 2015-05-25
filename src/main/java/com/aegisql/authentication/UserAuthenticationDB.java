package com.aegisql.authentication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aegisql.authentication.Group;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.dao.Connectable;
import com.aegisql.jdbc42.impl.AegisConnection;

public class UserAuthenticationDB implements UserAuthentication, Connectable {

	public final org.slf4j.Logger log = LoggerFactory.getLogger(UserAuthenticationDB.class);
	
	Connection connection;
	private String authenticationSchemaName = "";
	
	public UserAuthenticationDB() {
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

	private String selectUsersGroupsSql = "/*SQL NOT SET*/";
	private PreparedStatement pst = null;
	
	@Override
	public List<Group> getUserGroups(String userName, String password) throws SQLException {
		List<Group> groups = new ArrayList<Group>();
		
		if( pst == null ) {
			pst = connection.prepareStatement(selectUsersGroupsSql);
		}
		
		pst.setString(1, userName);
		pst.setString(2, password);
		ResultSet rs = pst.executeQuery();
		while( rs.next() ) {
			int accessorId = rs.getInt(5);
			Integer accessorIdIneger = rs.wasNull() ? null:accessorId;
			groups.add( new Group(rs.getInt(1),rs.getString(2),rs.getBoolean(3),rs.getString(4),accessorIdIneger));
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
	
	public void setSelectUsersGroupsSql( String sql ) {
		this.selectUsersGroupsSql = sql;
		if(this.pst != null) {
			PreparedStatement opst = pst;
			pst = null;
			try {
				opst.closeOnCompletion();
			} catch (SQLException e) {
				log.error("setSelectUsersGroupsSql was changed while PreparedStatement was already in use. Closing previous statement failed: "+e.getMessage()+" code: "+e.getErrorCode());
			}
		}
	}

	@Override
	public List<Group> getUserGroups(String userName, String password, String managedUser) throws SQLException {
		return null;
	}

}
