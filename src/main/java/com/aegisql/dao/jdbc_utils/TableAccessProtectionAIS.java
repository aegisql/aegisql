package com.aegisql.dao.jdbc_utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegisql.access.AccessPattern;
import com.aegisql.access.ColumnAccess;
import com.aegisql.access.TableAccess;
import com.aegisql.dao.Connectable;
import com.aegisql.jdbc42.impl.AegisConnection;

public class TableAccessProtectionAIS implements TableAccessProtection, Connectable {
	
	public final Logger log = LoggerFactory.getLogger(TableAccessProtectionAIS.class);

	public final static Set<String> reservedColumnNames = new HashSet<String>(){
		private static final long serialVersionUID = 1L;
	{
		add("_ID_");
		add("_GROUP_");
		add("_USER_");
		add("_HOST_");
		add("_DEVICE_");
		add("_ACCESSOR_");		
		add("_TABLE_ACCESS_");
	}};

	private Connection connection;
	private String defaultSchema = null;
	
	public TableAccessProtectionAIS() {	}

	@Override
	public void removeProtection(AccessPattern accessor, String schema, String table) throws SQLException {
		try ( Statement st = connection.createStatement() ) {
			st.execute( buildRemoveProtectionSql(accessor,schema,table) );
		}
	}
	
	@Override
	public void addProtection(AccessPattern accessor, String schema, TableAccess tableAccess, ColumnAccess... columns) throws SQLException {
		try ( Statement st = connection.createStatement() ) {
			st.execute( buildAddProtectionSql(accessor,schema,tableAccess,columns) );
		}
	}
	
	private final static String DELETE_START = "DELETE FROM %s.%s_%s WHERE ";
	public static String buildRemoveProtectionSql(AccessPattern accessor, String schema, String table) {
		StringBuffer sb = new StringBuffer( String.format(DELETE_START, "aegis_information_schema",schema,table));
		sb.append(accessor.whereString());
		return sb.toString();
	}
	
	private final static String INSERT_START  = "INSERT INTO %s.%s_%s (_GROUP_,_USER_,_HOST_,_DEVICE_,_ACCESSOR_,_TABLE_ACCESS_";
	private final static String INSERT_VALUES = "VALUES ('%s','%s','%s','%s','%s','%s'";
	
	public static String buildAddProtectionSql(AccessPattern accessor, String schema, TableAccess tableAccess, ColumnAccess... columns) {
		StringBuffer sb = new StringBuffer( String.format(INSERT_START, "aegis_information_schema",schema,tableAccess.getTableName()));
		if((columns == null) || (columns.length == 0) ) {
			sb
			.append(") ")
			.append( String.format(INSERT_VALUES, accessor.getGroupName(),accessor.getUserName(),accessor.getHostName(),"%",accessor.getAccessorField(),tableAccess.getAccessSet()))
			.append(")");
		} else {
			sb.append(",");
			for(ColumnAccess col:columns) {
				sb
				.append(col.getColName())
				.append(",");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb
			.append(") ")
			.append( String.format(INSERT_VALUES, accessor.getGroupName(),accessor.getUserName(),accessor.getHostName(),"%",accessor.getAccessorField(),tableAccess.getAccessSet()))
			.append(",");
			for(ColumnAccess col:columns) {
				sb
				.append("'")
				.append(col.getAccessSet())
				.append("'")
				.append(",");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb.append(")");
			
		}
		return sb.toString();
	}

	@Override
	public void setConnection(Connection connection, String defaultSchema) {
		this.defaultSchema = defaultSchema;
		if( connection instanceof AegisConnection ) {
			this.connection = ((AegisConnection) connection).getInnerConnection();			
		} else {
			this.connection = connection;			
		}
	}

	@Override
	public void removeProtection(AccessPattern accessor, String table) throws SQLException {
		removeProtection(accessor, defaultSchema, table);
	}

	@Override
	public void addProtection(AccessPattern accessor, TableAccess tableAccess, ColumnAccess... columns) throws SQLException {
		addProtection(accessor, defaultSchema, tableAccess, columns);
		
	}

}
