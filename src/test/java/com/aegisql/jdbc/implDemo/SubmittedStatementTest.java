package com.aegisql.jdbc.implDemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.authorization.AuthorizationException;
import com.aegisql.jdbc42.SubmittedStatement;
import com.aegisql.jdbc42.impl.AegisConnection;
import com.aegisql.jdbc42.impl.AegisSimpleDataSource;
import com.aegisql.testing_tools.AegisDataSourceFactory;
import com.aegisql.testing_tools.ClassUtils;
import com.aegisql.testing_tools.MySQLLocalClient;

public class SubmittedStatementTest {

	public final Logger log = LoggerFactory.getLogger("UnitTestLogger");
	@Rule
	public TestName testName = new TestName();
	@Rule
	public TestRule watchman = new TestWatcher() {
		public void starting(Description description) {
			super.starting(description);
			log.debug("START {} ================================\n", description.getMethodName());
		}
		public void finished(Description description) {
			super.finished(description);
			log.debug("END {} ================================\n\n", description.getMethodName());
		}
	};

	DataSource ds = AegisDataSourceFactory.TEST_DEMO_DS;
	SubmittedBy mike = new SubmittedBy();
	SubmittedBy nikita = new SubmittedBy();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		MySQLLocalClient mysql = new MySQLLocalClient("jdbc:mysql://localhost?user=root&password=");
		
		mysql.resetDatabase("aegis_information_schema");
		mysql.resetDatabase("AUTHENTICATION");
		mysql.resetDatabase("TEST_DEMO");
		
		mysql.executeFile(ClassUtils.getAbsolutePath("aegis_information_schema_TEST_DEMO_ACCOUNT.sql"));
		mysql.executeFile(ClassUtils.getAbsolutePath("aegis_information_schema_TEST_DEMO_ORDERS.sql"));
		
		mysql.executeFile(ClassUtils.getAbsolutePath("AUTHENTICATION_USERS.sql"));
		mysql.executeFile(ClassUtils.getAbsolutePath("AUTHENTICATION_GROUPS.sql"));
		mysql.executeFile(ClassUtils.getAbsolutePath("AUTHENTICATION_USER_GROUP_MAP.sql"));

		mysql.executeFile(ClassUtils.getAbsolutePath("TEST_DEMO_ACCOUNT.sql"));
		mysql.executeFile(ClassUtils.getAbsolutePath("TEST_DEMO_ORDERS.sql"));

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		mike.setSubmittedBy("mike");
		mike.setIdentifiedBy("12345");
		nikita.setSubmittedBy("nikita");
		nikita.setIdentifiedBy("12345");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() throws SQLException {
		Connection con = AegisDataSourceFactory.TEST_DEMO_CONNECTION;
		assertNotNull(con);
		assertTrue(con.isWrapperFor(AegisConnection.class));
		assertTrue(con.isWrapperFor(com.mysql.jdbc.JDBC4Connection.class));
		log.debug("connection:" + con.getClass().getName());
		assertEquals(AegisConnection.class, con.getClass());

		try (Statement statement = con.createStatement()) {

			SubmittedStatement st = (SubmittedStatement)statement;
			st.setSubmitter(mike);

			assertNotNull(statement);
			statement.executeUpdate("DELETE FROM ACCOUNT");
			statement.executeUpdate("DELETE FROM ACCOUNT SUBMITTED BY 'nikita' IDENTIFIED BY '12345'");
			
			statement.executeUpdate("DELETE FROM ORDERS");
			statement.executeUpdate("DELETE FROM ORDERS SUBMITTED BY 'nikita' IDENTIFIED BY '12345'");

			statement.executeUpdate("INSERT INTO ACCOUNT(USER_NAME,USER_ADDRESS) VALUES('Nikita','Moscow')  SUBMITTED BY 'nikita'  IDENTIFIED BY '12345'");
			statement.executeUpdate("INSERT INTO ACCOUNT(USER_NAME,USER_ADDRESS) VALUES('Mike','Ramsey')");
			
			ResultSet rs = statement.executeQuery("SELECT ID,USER_NAME,USER_ADDRESS FROM ACCOUNT");
			assertNotNull(rs);
			assertTrue( rs.next() );
			log.debug("ResultSet: " + rs.getString("USER_NAME") + " from " + rs.getString("USER_ADDRESS"));
			assertEquals("Ramsey",rs.getString(3));
			int accountIdMike=rs.getInt(1);
			assertFalse(rs.next());//Only one record visible!!!
			rs.close();

			statement.executeUpdate("INSERT INTO ORDERS(ACCOUNT_ID,DESCRIPTION,ORDER_STATUS_ID) VALUES("+accountIdMike+",'Mike''s Order #1',1)");
			statement.executeUpdate("INSERT INTO ORDERS(ACCOUNT_ID,DESCRIPTION,ORDER_STATUS_ID) VALUES("+accountIdMike+",'Mike''s Order #2',1)");
			statement.executeUpdate("INSERT INTO ORDERS(ACCOUNT_ID,DESCRIPTION,ORDER_STATUS_ID) VALUES("+accountIdMike+",'Mike''s Order #3',1)");

			ResultSet rs2 = statement.executeQuery("SELECT ID FROM ACCOUNT SUBMITTED BY 'nikita' IDENTIFIED BY '12345'");
			assertNotNull(rs2);
			assertTrue( rs2.next() );
			int accountIdNikita=rs2.getInt(1);
			assertFalse(rs2.next());//Only one record visible!!!
			rs2.close();

			statement.executeUpdate("INSERT INTO ORDERS(ACCOUNT_ID,DESCRIPTION,ORDER_STATUS_ID) VALUES("+accountIdNikita+",'Nikita''s Order #1',1) SUBMITTED BY 'nikita' IDENTIFIED BY '12345'");
			statement.executeUpdate("INSERT INTO ORDERS(ACCOUNT_ID,DESCRIPTION,ORDER_STATUS_ID) VALUES("+accountIdNikita+",'Nikita''s Order #2',1) SUBMITTED BY 'nikita' IDENTIFIED BY '12345'");
			
			int res = statement.executeUpdate("UPDATE ACCOUNT SET USER_NAME='Mike Teplitskiy'");
			assertEquals(1,res);
			ResultSet rs21 = statement.executeQuery("SELECT USER_NAME FROM ACCOUNT SUBMITTED BY 'mike' IDENTIFIED BY '12345'");
			assertTrue( rs21.next() );
			assertEquals("Mike Teplitskiy", rs21.getString("USER_NAME"));

			ResultSet rs3 = statement.executeQuery("SELECT A.USER_NAME,A.USER_ADDRESS,O.ORDER_RECEIVED,O.DESCRIPTION FROM ACCOUNT A,ORDERS O /*WHERE A.ID=O.ACCOUNT_ID*/");
			assertNotNull(rs3);
			assertTrue( rs3.next() );
			log.debug("Order: {} {} {} {}",rs3.getString(1),rs3.getString(2),rs3.getDate(3),rs3.getString(4));
			assertTrue( rs3.next() );
			log.debug("Order: {} {} {} {}",rs3.getString(1),rs3.getString(2),rs3.getDate(3),rs3.getString(4));
			assertTrue( rs3.next() );
			log.debug("Order: {} {} {} {}",rs3.getString(1),rs3.getString(2),rs3.getDate(3),rs3.getString(4));
			assertFalse( rs3.next() );
			rs3.close();

			ResultSet rs4 = statement.executeQuery("SELECT A.USER_NAME,A.USER_ADDRESS,O.ORDER_RECEIVED,O.DESCRIPTION FROM ACCOUNT A,ORDERS O /*WHERE A.ID=O.ACCOUNT_ID*/ SUBMITTED BY 'nikita' IDENTIFIED BY '12345'");
			assertNotNull(rs4);
			assertTrue( rs4.next() );
			log.debug("Order: {} {} {} {}",rs4.getString(1),rs4.getString(2),rs4.getDate(3),rs4.getString(4));
			assertTrue( rs4.next() );
			log.debug("Order: {} {} {} {}",rs4.getString(1),rs4.getString(2),rs4.getDate(3),rs4.getString(4));
			assertFalse( rs4.next() );
			rs4.close();

			
		}
	}

	@Test
	public void testWrapper() throws SQLException {
		assertTrue(ds.isWrapperFor(AegisSimpleDataSource.class));
		AegisSimpleDataSource ads = ds.unwrap(AegisSimpleDataSource.class);
		assertNotNull(ads);
	}

	@Test(expected = AuthorizationException.class)
	public void testUnAuthorizedUserFailure() throws SQLException {
		Connection con = ds.getConnection();
		assertNotNull(con);
		try (Statement statement = con.createStatement()) {
			ResultSet rs = statement
					.executeQuery("SELECT ID,USER_NAME,USER_ADDRESS FROM ACCOUNT SUBMITTED BY 'baduser'  IDENTIFIED BY '12345'");
			rs.next();
			log.debug("ResultSet: " + rs.getString("USER_NAME") + " from "
					+ rs.getString("USER_ADDRESS"));
			assertNotNull(rs);
			rs.close();

		}

	}

}
