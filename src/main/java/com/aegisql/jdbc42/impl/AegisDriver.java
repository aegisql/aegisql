package com.aegisql.jdbc42.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.conf.Configurator;
import com.aegisql.dao.authentication.sql_impl.UserAuthenticationDefault;
import com.aegisql.utils.ConnectionUtils;

public class AegisDriver implements java.sql.Driver {

	public final org.slf4j.Logger log = LoggerFactory.getLogger(AegisDriver.class);
	private final Configurator conf   = new Configurator();

	static {
		try {
			java.sql.DriverManager.registerDriver(new AegisDriver());
		} catch (SQLException e) {
			throw new RuntimeException("Can't register driver! ",e);
		}
	}

	private static final String URL_PREFIX = "jdbc:aegisql:";
	
	private java.sql.Driver inner = null;
	
	/**
	 * Construct a new driver and register it with DriverManager
	 * 
	 * @throws SQLException
	 *             if a database error occurs.
	 */
	public AegisDriver() throws SQLException {
		// Required for Class.forName().newInstance()
	}
	
	private void init( String url ) throws SQLException {
		inner =	DriverManager.getDriver( innerUrl(url) );
	}
	
	private String innerUrl( String url ) {
		if( url.startsWith(URL_PREFIX) ) {
			return url.replace(URL_PREFIX, "jdbc:");
		} else {
			return url;
		}
	}
	
	public Connection connect(String url, Properties info) throws SQLException {
		if(inner == null) {
			init( url );
		}
		if( url.startsWith(URL_PREFIX)) {
			String innerUrl = innerUrl(url);
			Map<String,String> prop = ConnectionUtils.parseConnectionUrl(innerUrl);
			Connection innerConnection = inner.connect( innerUrl, info );
			DatabaseMetaData dmd = innerConnection.getMetaData();
			SubmittedBy connectionSubmitter = new SubmittedBy();
			String[] userhost = dmd.getUserName().split("@");
			connectionSubmitter.setSubmittedBy(userhost[0]);
			if( userhost.length == 2 ) {
				connectionSubmitter.setHost(userhost[1]);
			}
			// if password is null, but we passed up to this point, it must be in the URL. Need some parser
			String password = info.getProperty("password");
			if( password == null ) {
				password = prop.get("password");
			}
			connectionSubmitter.setIdentifiedBy( password );
			log.debug("Connection Submitter: "+connectionSubmitter);
			AegisConnection connection = new AegisConnection( innerConnection, connectionSubmitter );
			connection.setUrlProperties(prop);
			if(prop.containsKey("authentication_provider")) {
				UserAuthentication ua = conf.getUserAuthentication(prop.get("authentication_provider"));
				if(ua == null) {
					throw new SQLException("Configuration for authentication_provider '"+prop.get("authentication_provider")+"' not found");
				} else {
					connection.setUserAuthentication(ua);
				}
			}else {
				UserAuthenticationDefault ua = new UserAuthenticationDefault();
				ua.setConnection(connection, "AUTHENTICATION");
				connection.setUserAuthentication(ua);
				connection.setAuthenticationSchema("AUTHENTICATION");
			}
			return connection;
		} else {
			return null;
		}
	}

	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith(URL_PREFIX);//inner.acceptsURL( innerUrl(url) );
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		if(inner == null) {
			init( url );
		}
		return inner.getPropertyInfo( innerUrl(url), info);
	}

	public int getMajorVersion() {
		return 0;
	}

	public int getMinorVersion() {
		return 1;
	}

	public boolean jdbcCompliant() {
		return inner.jdbcCompliant();
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return Logger.getLogger("com.aegisql");
	}

	public Configurator getConfigurator() {
		return conf;
	}
	
}
