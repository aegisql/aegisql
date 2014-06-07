package com.aegisql.authorization;

import java.sql.SQLException;
import java.util.Set;

import com.aegis.submitter.SubmittedBy;

public interface Authorizer {
	public String buildAuthorizedQuery(String query, SubmittedBy statementSubmitter) throws SQLException;
	public void setAllowedGroups(Set<String> allowedGroups);
}
