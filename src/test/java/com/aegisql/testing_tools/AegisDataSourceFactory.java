package com.aegisql.testing_tools;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.aegisql.jdbc42.impl.AegisConnection;
import com.aegisql.jdbc42.impl.AegisSimpleDataSource;

public class AegisDataSourceFactory {

	public static AegisSimpleDataSource ROOT_DS = new AegisSimpleDataSource();
	public static AegisConnection ROOT_CONNECTION;

	public static AegisSimpleDataSource TEST_DEMO_DS = new AegisSimpleDataSource();
	public static AegisConnection TEST_DEMO_CONNECTION;
	public static AegisConnection TEST_DEMO_CONNECTION_FOR_POWER_USER;
	static {
		ROOT_DS.setUrl("jdbc:mysql://localhost:3306/");
		ROOT_DS.setUsername("root");
		ROOT_DS.setPassword("");
		ROOT_DS.setAutenticationSchema("AUTHENTICATION");
		
		TEST_DEMO_DS.setUrl("jdbc:mysql://localhost:3306/TEST_DEMO?authentication_provider=default_authentication");
		TEST_DEMO_DS.setUsername("root");
		TEST_DEMO_DS.setPassword("");
		TEST_DEMO_DS.setAutenticationSchema("AUTHENTICATION");
		try {
			ROOT_CONNECTION      = (AegisConnection) ROOT_DS.getConnection();
			TEST_DEMO_CONNECTION = (AegisConnection) TEST_DEMO_DS.getConnection();
			TEST_DEMO_CONNECTION_FOR_POWER_USER = (AegisConnection) TEST_DEMO_DS.getConnection();
			Set<String> powerSet = new HashSet<String>();
			powerSet.add("POWER_USER");
			TEST_DEMO_CONNECTION_FOR_POWER_USER.setAllowedGroups(powerSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
