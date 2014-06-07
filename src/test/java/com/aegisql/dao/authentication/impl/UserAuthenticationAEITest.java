package com.aegisql.dao.authentication.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
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

import com.aegisql.authentication.Group;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.dao.Connectable;
import com.aegisql.dao.authentication.sql_impl.UserAuthenticationDefault;
import com.aegisql.testing_tools.MySQLAuthenticationAssistant;

public class UserAuthenticationAEITest {

	public final Logger log = LoggerFactory.getLogger("UnitTestLogger");
	@Rule
	public TestName testName = new TestName();
	@Rule
	public TestRule watchman = new TestWatcher() {
		public void starting(Description description) {
			super.starting(description);
			log.debug("{} ================================", description.getMethodName());
		}
	};
	
	static BasicDataSource ds = new BasicDataSource();
	static Connection con;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ds.setUrl("jdbc:mysql://localhost:3306/TEST_DEMO");
		ds.setUsername("root");
		ds.setPassword("");
		con = ds.getConnection();
		MySQLAuthenticationAssistant aa = new MySQLAuthenticationAssistant("AUTHENTICATION_TEST");
		aa.setConnection(con);
		aa.resetAuthenticationSchema();
		aa.createGroup("ADMIN", "Admin User", null, true);
		aa.createGroup("USER", "Simple User", "ACCESSOR_ID", true);
		aa.createGroup("POWER_USER", "Power User", "ACCESSOR_ID", true);
		aa.createGroup("GUEST", "Guest User", null, true);
		
		aa.createUser("mike", "12345", "2014-12-31 23:59:59", true, 3);
		aa.createUser("nikita", "12345", "2014-12-31 23:59:59", true, 2);

		aa.mapUserToGroup(1, 2, null);
		aa.mapUserToGroup(1, 3, 1001);
		aa.mapUserToGroup(2, 2, 1002);
		
	}

	UserAuthentication ua;
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ua = new UserAuthenticationDefault();
		((Connectable) ua).setConnection(con,"AUTHENTICATION");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws SQLException {
		List<Group> groups = ua.getUserGroups("mike", "12345");
		log.debug("Groups found {}",groups);
		assertEquals(2,groups.size());
		Group one = groups.get(0);
		assertTrue(one.isDefault());
		assertFalse(one.isGroupManager());
		assertEquals("POWER_USER",one.getGroupName());
		assertEquals(new Integer(1001),one.getAccessorId());
		Group two = groups.get(1);
		assertFalse(two.isDefault());
		assertTrue(two.isGroupManager());
		assertEquals("USER",two.getGroupName());
		
	}

}
