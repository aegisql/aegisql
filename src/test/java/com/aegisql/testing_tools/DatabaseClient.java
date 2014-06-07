package com.aegisql.testing_tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DatabaseClient {
	
	public final Logger log = LoggerFactory.getLogger("UnitTestLogger");

	Connection connection = null;

	public DatabaseClient( String connectionUri ) throws Exception {
		connection = DriverManager.getConnection( connectionUri );
	}
	
	public abstract void resetDatabase( String dbName ) throws SQLException;
	
	public abstract void resetTable( TableCreator tc ) throws SQLException;
	
	public void executeFile( String path ) throws SQLException {
	    
	    try(BufferedReader br = new BufferedReader(new FileReader( path ))) {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        String everything = sb.toString();
			Statement st = connection.createStatement();
			
			String sqls[] = everything.split(";");
			for(String sql:sqls) {
				String SQL = sql.trim();
				if("".equals(SQL)) continue;
				System.out.println("Execute SQL "+SQL);
				st.execute( SQL );
			}
			st.close();
	    } catch(IOException e){
	    	log.debug("IO error: {}",e.getMessage());
	    }
	}
	
}
