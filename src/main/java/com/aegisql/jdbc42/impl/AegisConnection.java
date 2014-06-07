package com.aegisql.jdbc42.impl;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.access.Granted;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.authorization.Authorizer;
import com.aegisql.authorization.SqlAuthorizer;
import com.aegisql.dao.Connectable;
import com.aegisql.dao.aegis_information_schema.sql_impl.GrantedAIS;
import com.aegisql.dao.jdbc_utils.Utils;
import com.aegisql.jdbc42.impl.proxy.CallableStatementProxy;
import com.aegisql.jdbc42.impl.proxy.PreparedStatementProxy;
import com.aegisql.jdbc42.impl.proxy.StatementProxy;

public class AegisConnection implements java.sql.Connection {
	
	public final org.slf4j.Logger log = LoggerFactory.getLogger(AegisConnection.class);

	
	private final Connection inner;
	
	private DatabaseMetaData dbMetaData          = null;
	private String schemaName                    = null;
	
	private final Granted granted                = new GrantedAIS();             //STRONG DEPENDENCY! REFACTOR
	private UserAuthentication ua                = null;
	private SqlAuthorizer authorizer             = null;
	private String authenticationSchemaName      = "";
	private Set<String> allowedGroups            = new HashSet<String>();
	private Map<String,String> urlProp           = new HashMap<>();
	private final String schema;
	
	private final SubmittedBy connectionSubmitter;
	
	public Granted getGranted() {
		return granted;
	}
	
	public UserAuthentication getUserAuthentication() {
		return ua;
	}

	public AegisConnection( Connection connection, SubmittedBy connectionSubmitter ) throws SQLException {	
		this.inner               = connection;
		this.connectionSubmitter = connectionSubmitter;
		((Connectable) this.granted).setConnection(this,getSchema());
		this.schema = this.getSchema();
	}
	
	public Connection getInnerConnection() {
		return inner;
	}
	
	public Authorizer getAuthorizer() {
		if(authorizer == null) {
			authorizer = new SqlAuthorizer( this.granted, this.getUserAuthentication(), schema );
		}
		return authorizer;
	}

	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if( isWrapperFor(iface)) return (T)this;
		else if( inner.isWrapperFor(iface)) return (T)inner.unwrap(iface);
		else throw new SQLException("Cannot unwrap connection "+iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		if(this.getClass().equals(iface)) return true;
		else if(inner.getClass().equals(iface)) return true;
		else return false;
	}

	// BEGIN STATEMENT WRAPPERS
	
	//STATEMENT
	public Statement createStatement() throws SQLException {
		Statement s = StatementProxy.wrap( this, inner.createStatement() );
		return s;
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		Statement s = StatementProxy.wrap( this, inner.createStatement(resultSetType, resultSetConcurrency) );
		return s;
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		Statement s = StatementProxy.wrap( this, inner.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability) );
		return s;
	}

	//CALLABLE STATEMENT
	public CallableStatement prepareCall(String sql) throws SQLException {
		String prepared = authorizer.buildAuthorizedQuery( sql, connectionSubmitter );
		CallableStatement cs = CallableStatementProxy.wrap( this, inner.prepareCall(prepared),sql );
		return cs;
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		String prepared = authorizer.buildAuthorizedQuery( sql, connectionSubmitter );
		CallableStatement cs = CallableStatementProxy.wrap( this, inner.prepareCall(prepared,resultSetType,resultSetConcurrency),sql );
		return cs;
	}

	public CallableStatement prepareCall(String sql, int resultSetType,	int resultSetConcurrency, int resultSetHoldability)	throws SQLException {
		String prepared = authorizer.buildAuthorizedQuery( sql, connectionSubmitter );
		CallableStatement cs = CallableStatementProxy.wrap( this, inner.prepareCall(prepared, resultSetType, resultSetConcurrency, resultSetHoldability),sql );
		return cs;
	}

	//PREPARED STATEMENT
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		String prepared = authorizer.buildAuthorizedQuery( sql, connectionSubmitter );
		PreparedStatement ps = PreparedStatementProxy.wrap( this, inner.prepareStatement(prepared),sql );
		return ps;
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		String prepared = authorizer.buildAuthorizedQuery( sql, connectionSubmitter );
		PreparedStatement ps = PreparedStatementProxy.wrap( this, inner.prepareStatement(prepared,resultSetType,resultSetConcurrency),sql );
		return ps;
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		String prepared = authorizer.buildAuthorizedQuery( sql, connectionSubmitter );
		PreparedStatement ps = PreparedStatementProxy.wrap( this, inner.prepareStatement(prepared, autoGeneratedKeys),sql );
		return ps;
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		String prepared = authorizer.buildAuthorizedQuery( sql, connectionSubmitter );
		return PreparedStatementProxy.wrap( this, inner.prepareStatement(prepared, resultSetType, resultSetConcurrency, resultSetHoldability),sql );
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		String prepared = authorizer.buildAuthorizedQuery( sql, connectionSubmitter );
		PreparedStatement ps = PreparedStatementProxy.wrap( this, inner.prepareStatement(prepared, columnIndexes),sql );
		return ps;
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		String prepared = authorizer.buildAuthorizedQuery( sql, connectionSubmitter );
		PreparedStatement ps = PreparedStatementProxy.wrap( this, inner.prepareStatement(prepared, columnNames),sql );
		return ps;
	}

	// END STATEMENT WRAPPERS
	
	public String nativeSQL(String sql) throws SQLException {
		return inner.nativeSQL(authorizer.buildAuthorizedQuery( sql, connectionSubmitter ));
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		inner.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit() throws SQLException {
		return inner.getAutoCommit();
	}

	public void commit() throws SQLException {
		inner.commit();
	}

	public void rollback() throws SQLException {
		inner.rollback();
	}

	public void close() throws SQLException {
		inner.close();
	}

	public boolean isClosed() throws SQLException {
		return inner.isClosed();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		if( dbMetaData == null ) {
			dbMetaData = inner.getMetaData(); 
		}
		return dbMetaData;
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		inner.setReadOnly(readOnly);
	}

	public boolean isReadOnly() throws SQLException {
		return inner.isReadOnly();
	}

	public void setCatalog(String catalog) throws SQLException {
		inner.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException {
		return inner.getCatalog();
	}

	public void setTransactionIsolation(int level) throws SQLException {
		inner.setTransactionIsolation(level);
	}

	public int getTransactionIsolation() throws SQLException {
		return inner.getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		return inner.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		inner.clearWarnings();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return inner.getTypeMap();
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		inner.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		inner.setHoldability(holdability);
	}

	public int getHoldability() throws SQLException {
		return inner.getHoldability();
	}

	public Savepoint setSavepoint() throws SQLException {
		return inner.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return inner.setSavepoint(name);
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		inner.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		inner.releaseSavepoint(savepoint);
	}

	public Clob createClob() throws SQLException {
		return inner.createClob();
	}

	public Blob createBlob() throws SQLException {
		return inner.createBlob();
	}

	public NClob createNClob() throws SQLException {
		return inner.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return inner.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		return inner.isValid(timeout);
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		inner.setClientInfo(name, value);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		inner.setClientInfo(properties);
	}

	public String getClientInfo(String name) throws SQLException {
		return inner.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		return inner.getClientInfo();
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return inner.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return inner.createStruct(typeName, attributes);
	}

	public void setSchema(String schema) throws SQLException {
		this.schemaName = schema;
	}

	public String getSchema() throws SQLException {
		if(schemaName == null) schemaName = Utils.getSingleString(inner, "SELECT SCHEMA()"); 
		return schemaName;
	}

	public void abort(Executor executor) throws SQLException {
		inner.abort(executor);
	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		inner.setNetworkTimeout(executor, milliseconds);
	}

	public int getNetworkTimeout() throws SQLException {
		return inner.getNetworkTimeout();
	}

	public Set<String> getAllowedGroups() {
		return allowedGroups;
	}

	public void setAllowedGroups(Set<String> allowedGroups) {
		this.allowedGroups = allowedGroups;
		this.getAuthorizer().setAllowedGroups(allowedGroups);
	}

	public void setAuthenticationSchema(String name) {
		if(name.endsWith(".")) {
			this.authenticationSchemaName = name;			
		} else {
			this.authenticationSchemaName = name+".";			
		}
	}
	
	public String getAuthenticationSchema() {
		return authenticationSchemaName;
	}
	
	public SubmittedBy getConnectionSubmitter() {
		return connectionSubmitter;
	}

	public Map<String, String> getUrlProperties() {
		return urlProp;
	}

	public void setUrlProperties(Map<String, String> urlProp) {
		this.urlProp = urlProp;
	}
	
	public void setUserAuthentication( UserAuthentication ua ) {
		this.ua = ua;
	}
	
}
