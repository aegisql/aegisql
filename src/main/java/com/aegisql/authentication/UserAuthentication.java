package com.aegisql.authentication;

import java.sql.SQLException;
import java.util.List;

public interface UserAuthentication {
	List<Group> getUserGroups(String userName, String password) throws SQLException;
	List<Group> getUserGroups(String userName, String password, String managedUser) throws SQLException;
}
