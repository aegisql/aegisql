package com.aegisql.jdbc42.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.conf.Configurator;
import com.aegisql.dao.authentication.sql_impl.UserAuthenticationDefault;
import com.aegisql.utils.ConnectionUtils;

public class AegisSimpleDataSource implements DataSource {
	
	public final org.slf4j.Logger log = LoggerFactory.getLogger(AegisSimpleDataSource.class);

	private final static Logger logger = Logger.getLogger("com.aegisql.jdbc");
	
	private String username;
	private String password;
	private String driverClassName;
	private String url;
	private String validationQuery        = "SELECT 1";
	private long validationQueryTimeout   = 60*1000;	
	private String aegisInformationSchema = "aegis_information_schema";
	private String authenticationSchema    = "AUTHENTICATION.";
	private Set<String> allowedGroups     = new HashSet<String>();
	private int loginTimeout              = 0;
	
	private final Configurator conf       = new Configurator();
	
	public AegisSimpleDataSource() {
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) throws ClassNotFoundException {
		Class.forName(driverClassName);
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public org.slf4j.Logger getLog() {
		return log;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}//

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.loginTimeout = seconds;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return loginTimeout;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return logger;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if( isWrapperFor(iface) )return (T)this;
		else throw new SQLException("DataSource is not wrapped for " + iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getClass().equals(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return this.getConnection(username, password);
	}

	/*
	 * Here we should support 3 connections:
	 * AUTHENTICATION
	 * AEGIS_INFORMATION_SCHEMA
	 * & CONNECTION itself
	 * For demo assume all three schemas coexist on the same server
	 * And this should probably be the most common case
	 * */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		Connection inner;
		DatabaseMetaData dmd;
		SubmittedBy connectionSubmitter = new SubmittedBy();
		Map<String,String> prop = ConnectionUtils.parseConnectionUrl(url);
		if( username == null ) { // user and pass can come from URL
			inner = DriverManager.getConnection(url);
			dmd = inner.getMetaData();
			String[] userhost = dmd.getUserName().split("@");

			if( username == null ) {
				username = userhost[0];
				password = prop.get("password");
			}
			if( userhost.length == 2 ) {
				connectionSubmitter.setHost(userhost[1]);
			}

		} else {
			inner = DriverManager.getConnection(url, username, password);
			dmd = inner.getMetaData();
			String[] userhost = dmd.getUserName().split("@");
			if( userhost.length == 2 ) {
				connectionSubmitter.setHost(userhost[1]);
			}
		}

		connectionSubmitter.setIdentifiedBy(password);
		connectionSubmitter.setSubmittedBy(username);

		AegisConnection connection = new AegisConnection(inner,connectionSubmitter);
		if(prop.containsKey("authentication_provider")) {
			UserAuthentication ua = conf.getUserAuthentication(prop.get("authentication_provider"));
			if(ua == null) {
				throw new SQLException("Configuration for authentication_provider '"+prop.get("authentication_provider")+"' not found");
			} else {
				connection.setUserAuthentication(ua);
			}
		} else {
			UserAuthenticationDefault ua = new UserAuthenticationDefault();
			ua.setConnection(connection, authenticationSchema);
			connection.setUserAuthentication(ua);
			connection.setAuthenticationSchema(authenticationSchema);
		}
		connection.setAllowedGroups(allowedGroups);
		connection.setUrlProperties(prop);
		return connection;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public long getValidationQueryTimeout() {
		return validationQueryTimeout;
	}

	public void setValidationQueryTimeout(long validationQueryTimeout) {
		this.validationQueryTimeout = validationQueryTimeout;
	}

	public String getAegisInformationSchema() {
		return aegisInformationSchema;
	}

	public void setAegisInformationSchema(String aegisInformationSchema) {
		this.aegisInformationSchema = aegisInformationSchema;
	}

	public String getAutenticationSchema() {
		return authenticationSchema;
	}

	public void setAutenticationSchema(String autenticationSchema) {
		if(autenticationSchema.endsWith(".")){
			this.authenticationSchema = autenticationSchema;
		} else {
			this.authenticationSchema = autenticationSchema+".";			
		}
	}

	public Set<String> getAllowedGroups() {
		return allowedGroups;
	}

	public void setAllowedGroups(Set<String> allowedGroups) {
		this.allowedGroups = allowedGroups;
	}

	public void setAllowedGroups(String allowedGroups) {
		String[] groups = allowedGroups.split(",");
		for( String group: groups) {
			this.allowedGroups.add(group.trim());
		}
	}

	
}
