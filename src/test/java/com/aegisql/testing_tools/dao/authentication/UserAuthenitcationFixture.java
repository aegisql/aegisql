package com.aegisql.testing_tools.dao.authentication;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aegisql.authentication.Group;
import com.aegisql.authentication.UserAuthentication;

public class UserAuthenitcationFixture implements UserAuthentication {
	
	private final Map<String,List<Group>> userGroups = new HashMap<String, List<Group>>();
	
	@Override
	public List<Group> getUserGroups(String userName, String password) throws SQLException {
		List<Group> groups = new ArrayList<Group>();
		String key = userName+":"+password;
		if(userGroups.containsKey(key)) {
			groups = userGroups.get(key);
		}
		return groups;
	}
	
	public UserAuthenitcationFixture addGroup(String userName, String password, Group g) {
		String key = userName+":"+password;
		if( ! userGroups.containsKey(key)) {
			List<Group> groups = new ArrayList<Group>();
			groups.add(g);
			userGroups.put(key, groups);
		} else {
			userGroups.get(key).add(g);
		}
		return this;
	}

	public UserAuthenitcationFixture addGroup(String userName, String password, long id, String groupName, boolean isDefault, String useAccessor, Integer accessorId) {
		Group g = new Group(id,groupName,isDefault,useAccessor,accessorId);
		this.addGroup(userName, password, g);
		return this;
	}
	
	public static UserAuthentication getMikeAndNikita() {
		UserAuthenitcationFixture uaf = new UserAuthenitcationFixture();
		
		uaf
		.addGroup("mike", "12345", 1, "POWER_USER", true, "ACCESSOR_ID", 1001)
		.addGroup("mike", "12345", 1, "USER", false, "ACCESSOR_ID", 1001)

		.addGroup("nikita", "12345", 2, "USER", true, "ACCESSOR_ID", 1002);
		
		return uaf;
	}
	
}
