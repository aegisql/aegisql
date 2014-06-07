package com.aegisql;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aegisql.testing_tools.MySQLLocalClient;
import com.aegisql.testing_tools.TableCreator;

public class MySqlClientTest {

	private static MySQLLocalClient mysql;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mysql = new MySQLLocalClient("jdbc:mysql://localhost/?user=root&password=");
		mysql.resetDatabase("AUTHENTICATION_TEST");
		mysql.resetDatabase("TEST_DEMO");

		TableCreator tcTestDemoAccount = TableCreator.mysqlTableCreator("TEST_DEMO", "ACCOUNT");
		tcTestDemoAccount.begin();
		tcTestDemoAccount.ID();
		tcTestDemoAccount.VARCHAR("USER_NAME", 45, true, null);
		tcTestDemoAccount.VARCHAR("USER_ADDRESS", 45, true, null);
		tcTestDemoAccount.INT("ACCESSOR_ID", true, null);
		tcTestDemoAccount.primaryKey("ID");
		tcTestDemoAccount.end();
		System.out.println(tcTestDemoAccount.toString());
		
		mysql.resetTable(tcTestDemoAccount);

		TableCreator tcTestDemoOrder = TableCreator.mysqlTableCreator("TEST_DEMO", "ORDERS");
		tcTestDemoOrder.begin();
		tcTestDemoOrder.ID();
		tcTestDemoOrder.INT("ACCOUNT_ID", false, null);		
		tcTestDemoOrder.VARCHAR("DESCRIPTION", 255, true, null);
		tcTestDemoOrder.TIMESTAMP("ORDER_RECEIVED", true, "CURRENT_TIMESTAMP", null);
		tcTestDemoOrder.DATETIME("ORDER_PROCESSED", true, "CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP");
		tcTestDemoOrder.INT("ORDER_STATUS_ID", true, 1);
		tcTestDemoOrder.INT("ACCESSOR_ID", true, null);
		tcTestDemoOrder.primaryKey("ID");
		tcTestDemoOrder.end();
		System.out.println(tcTestDemoOrder.toString());

		mysql.resetTable(tcTestDemoOrder);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTableCreator() throws Exception {
	}

}
