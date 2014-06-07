package com.aegisql.authorization;

import java.sql.SQLException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.access.Granted;
import com.aegisql.authentication.UserAuthentication;

public class PreparedAuthorizer implements Authorizer {

	public final Logger log = LoggerFactory.getLogger(PreparedAuthorizer.class);
	
	private final Granted granted;
	private final UserAuthentication ua;
	private final String defaultSchema;
	private Set<String> allowedGroups = null;

	public PreparedAuthorizer(Granted granted, UserAuthentication ua) {
		this(granted,ua,null);
	}
	
	public PreparedAuthorizer(Granted granted, UserAuthentication ua, String defaultSchema) {
		this.granted        = granted;
		this.ua             = ua;
		this.defaultSchema  = defaultSchema;
	}
	
	@Override
	public String buildAuthorizedQuery(String query, SubmittedBy statementSubmitter) throws SQLException {
		return null;
	}

	@Override
	public void setAllowedGroups(Set<String> allowedGroups) {
		this.allowedGroups = allowedGroups;
	}

}
