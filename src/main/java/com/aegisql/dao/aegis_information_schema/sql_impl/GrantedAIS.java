package com.aegisql.dao.aegis_information_schema.sql_impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegisql.access.AccessPattern;
import com.aegisql.access.ColumnAccess;
import com.aegisql.access.ColumnQueryType;
import com.aegisql.access.Granted;
import com.aegisql.access.GrantedAccess;
import com.aegisql.access.TableAccess;
import com.aegisql.access.TableQueryType;
import com.aegisql.dao.Connectable;
import com.aegisql.jdbc42.impl.AegisConnection;

public class GrantedAIS implements Granted, Connectable {
	
	public final Logger log = LoggerFactory.getLogger(GrantedAIS.class);

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

	public GrantedAIS() {
		
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

	private final static String SELECT_START = "SELECT * FROM %s.%s_%s WHERE _GROUP_ = '%s' AND _USER_ = '%s' AND _HOST_ = '%s'";

	@Override
	public GrantedAccess getGrantedAccess(String schema, String table, AccessPattern accessor) throws SQLException {
		
		String sql = String.format(SELECT_START, "aegis_information_schema" ,schema,table,accessor.getGroupName(),accessor.getUserName(),accessor.getHostName());
		GrantedAccess ga = null;
		try ( Statement st = connection.createStatement() ){
			ResultSet rs = st.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			if( ! rs.next() ) {
				return null;
			}
			ga = new GrantedAccess(schema,table, rs.getString("_ACCESSOR_"), accessor);
			String tableAccessString = rs.getString("_TABLE_ACCESS_");
			ga.setTableAccess( new TableAccess(table,getTableQueryTypeSet(tableAccessString) ));
			for(int i = 1; i <= rsmd.getColumnCount(); i++) {
				String name = rsmd.getColumnName(i); 
				if(reservedColumnNames.contains(name)) continue;
				String val = rs.getString(i);
				ColumnAccess ca = new ColumnAccess(name, getColumnQueryTypeSet(val));
				ga.addColumnAccess(ca);
			}
		}
		
		return ga;
		
	}

	@Override
	public GrantedAccess getGrantedAccess(String table, AccessPattern accessor) throws SQLException {
		return getGrantedAccess(defaultSchema, table, accessor);
	}

	public static TableQueryType[] getTableQueryTypeSet( String setString ) {
		String[] tqs = setString.split(","); 
		TableQueryType[] tqt = new TableQueryType[ tqs.length ];
		for( int i = 0; i< tqs.length; i++ ){
			try{
				tqt[i] = TableQueryType.valueOf(tqs[i]);
			} catch (Exception e ) {
				tqt[i] = TableQueryType.BLOCKED;
			}
		}
		return tqt;
	}

	public static ColumnQueryType[] getColumnQueryTypeSet( String setString ) {
		String[] cqs          = setString.split(","); 
		ColumnQueryType[] cqt = new ColumnQueryType[ cqs.length ];
		for( int i = 0; i< cqs.length; i++ ){
			try {
				cqt[i] = ColumnQueryType.valueOf(cqs[i]);
			} catch (Exception e ) {
				cqt[i] = ColumnQueryType.BLOCKED;
			}
		}
		return cqt;
	}
	
}
