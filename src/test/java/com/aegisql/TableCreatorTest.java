package com.aegisql;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aegisql.testing_tools.TableCreator;

public class TableCreatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	public void test1() {
		TableCreator tc = TableCreator.mysqlTableCreator("TEST_DEMO", "ACCOUNT");
		tc.begin();
		tc.ID();
		tc.VARCHAR("USER_NAME", 45, true, null);
		tc.VARCHAR("USER_ADDRESS", 45, true, null);
		tc.INT("ACCESSOR_ID", true, null);
		tc.primaryKey("ID");
		tc.end();
		System.out.println(tc.toString());
	}

	@Test
	public void test2() {
		TableCreator tc = TableCreator.mysqlTableCreator("TEST_DEMO", "ORDERS");
		tc.begin();
		tc.ID();
		tc.INT("ACCOUNT_ID", false, null);
		tc.VARCHAR("DESCRIPTION", 255, true, null);
		tc.TIMESTAMP("ORDER_RECEIVED", true, "CURRENT_TIMESTAMP", null);
		tc.DATETIME("ORDER_PROCESSED", true, "2013-10-10 12:00:00", "CURRENT_TIMESTAMP");
		tc.INT("ORDER_STATUS_ID", true, 1);
		tc.INT("ACCESSOR_ID", true, null);
		tc.primaryKey("ID");
		tc.end();
		System.out.println(tc.toString());
	}

	
}
