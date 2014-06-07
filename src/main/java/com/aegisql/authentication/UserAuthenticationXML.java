package com.aegisql.authentication;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAuthenticationXML implements UserAuthentication {

	private final Map<String,List<Group>> groups = new HashMap<>();
	
	@Override
	public List<Group> getUserGroups(String userName, String password) throws SQLException {
		return groups.get("USER_"+userName+"_PASS_"+password);
	}

	public void addGroup(String userName, String password, List<Group> g) {
		groups.put("USER_"+userName+"_PASS_"+password, g);
	}
	
}
