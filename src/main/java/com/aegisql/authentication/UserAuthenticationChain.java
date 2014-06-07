package com.aegisql.authentication;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserAuthenticationChain implements UserAuthentication {

	private final List<UserAuthentication> chain = new ArrayList<>();
	
	private static List<Group> empty = new ArrayList<Group>();
	
	@Override
	public List<Group> getUserGroups(String userName, String password) throws SQLException {
		for( UserAuthentication ua: chain) {
			List<Group> groups = ua.getUserGroups(userName, password);
			if( groups != null && groups.size() > 0 ) {
				return groups;
			}
		}

		return empty;
	}
	
	public void addUserAuthentication( UserAuthentication ua ) {
		chain.add(ua);
	}

}
