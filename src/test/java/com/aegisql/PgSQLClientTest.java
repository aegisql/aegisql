package com.aegisql;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aegisql.testing_tools.MySQLLocalClient;
import com.aegisql.testing_tools.PgSQLLocalClient;
import com.aegisql.testing_tools.TableCreator;

public class PgSQLClientTest {

	private static PgSQLLocalClient pgsql;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		pgsql = new PgSQLLocalClient("jdbc:postgresql://localhost:5432?user=postgres&password=root");
		pgsql.resetDatabase("AUTHENTICATION_TEST");
		pgsql.resetDatabase("TEST_DEMO");

		TableCreator tcTestDemoAccount = TableCreator.pgTableCreator("TEST_DEMO", "ACCOUNT");
		tcTestDemoAccount.begin();
		tcTestDemoAccount.ID();
		tcTestDemoAccount.VARCHAR("USER_NAME", 45, true, null);
		tcTestDemoAccount.VARCHAR("USER_ADDRESS", 45, true, null);
		tcTestDemoAccount.INT("ACCESSOR_ID", true, null);
		tcTestDemoAccount.primaryKey("ID");
		tcTestDemoAccount.end();
		System.out.println(tcTestDemoAccount.toString());
		
		pgsql.resetTable(tcTestDemoAccount);

		TableCreator tcTestDemoOrder = TableCreator.pgTableCreator("TEST_DEMO", "ORDERS");
		tcTestDemoOrder.begin();
		tcTestDemoOrder.ID();
		tcTestDemoOrder.INT("ACCOUNT_ID", false, null);		
		tcTestDemoOrder.VARCHAR("DESCRIPTION", 255, true, null);
		tcTestDemoOrder.TIMESTAMP("ORDER_RECEIVED", true, "CURRENT_TIMESTAMP", null);
		tcTestDemoOrder.DATETIME("ORDER_PROCESSED", true, "CURRENT_TIMESTAMP", null);
		tcTestDemoOrder.INT("ORDER_STATUS_ID", true, 1);
		tcTestDemoOrder.INT("ACCESSOR_ID", true, null);
		tcTestDemoOrder.primaryKey("ID");
		tcTestDemoOrder.end();
		System.out.println(tcTestDemoOrder.toString());

		pgsql.resetTable(tcTestDemoOrder);

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
