package com.aegisql.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;

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
import com.aegisql.access.Granted;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.dao.Connectable;
import com.aegisql.dao.aegis_information_schema.sql_impl.GrantedAIS;
import com.aegisql.dao.authentication.sql_impl.UserAuthenticationDefault;
import com.aegisql.dao.jdbc_utils.TableAccessProtectionAIS;
import com.aegisql.dao.jdbc_utils.TableAccessProtection;
import com.aegisql.jdbc42.impl.AegisConnection;
import com.aegisql.testing_tools.AegisDataSourceFactory;
import com.aegisql.testing_tools.dao.test_demo.Account;
import com.aegisql.testing_tools.dao.test_demo.Orders;

public class SqlAuthorizerWithSubmitterTest {
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
	

	SqlAuthorizer sa;
	SubmittedBy mike = new SubmittedBy();
	SubmittedBy nikita = new SubmittedBy();

	static TableAccessProtection pt = new TableAccessProtectionAIS();
	static Granted gr               = new GrantedAIS();
	static UserAuthentication ua    = new UserAuthenticationDefault();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AegisConnection con = AegisDataSourceFactory.TEST_DEMO_CONNECTION;
		con.setAuthenticationSchema("AUTHENTICATION");
		assertEquals("test_demo",con.getSchema().toLowerCase());
		((Connectable) ua).setConnection(con,"AUTHENTICATION");
		((Connectable) pt).setConnection(con,"test_demo");
		((Connectable) gr).setConnection(con,"test_demo");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		sa = new SqlAuthorizer( gr,ua,"TEST_DEMO" );
		mike.setSubmittedBy("mike");
		mike.setIdentifiedBy("12345");
		nikita.setSubmittedBy("nikita");
		nikita.setIdentifiedBy("12345");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void simpleSelectMikeTest() throws ParseException, SQLException {
		Account.removeProtectionAnyMikeAny(pt);
		Account.addProtectionAnyMikeAny(pt);
		String query = "SELECT * FROM ACCOUNT";
		log.debug("Initial query query: "+query);
		
		String modified = sa.buildAuthorizedQuery(query,mike);
		assertNotNull(modified);

		log.debug("Final query: "+modified);
		//and still can parse
		Reader reader = new StringReader(modified);
		CCJSqlParser parser = new CCJSqlParser(reader);
		parser.Statement();		
	}

	@Test
	public void joinedSelectMikeTest() throws ParseException, SQLException {
		Account.removeProtectionAnyMikeAny(pt);
		Orders.removeProtectionAnyMikeAny(pt);
		Account.addProtectionAnyMikeAny(pt);
		Orders.addProtectionAnyMikeAny(pt);
		String query = "SELECT * FROM ACCOUNT A, ORDERS O WHERE A.ID=O.ID AND A.ID=1";
		log.debug("Initial query query: "+query);
		
		String modified = sa.buildAuthorizedQuery(query,mike);
		assertNotNull(modified);

		log.debug("Final query: "+modified);
		//and still can parse
		Reader reader = new StringReader(modified);
		CCJSqlParser parser = new CCJSqlParser(reader);
		parser.Statement();		
	}

	
	@Test
	public void simpleSelectNikitaTest() throws ParseException, SQLException {
		Account.removeProtectionAnyNikitaAny(pt);
		Account.removeProtectionUserNikitaAny(pt);
		Account.addProtectionUserNikitaAny(pt);
		Account.addProtectionAnyNikitaAny(pt);

		Orders.removeProtectionAnyNikitaAny(pt);
		Orders.removeProtectionUserNikitaAny(pt);
		Orders.addProtectionUserNikitaAny(pt);
		Orders.addProtectionAnyNikitaAny(pt);

		String query = "SELECT * FROM ACCOUNT";
		log.debug("Initial query query: "+query);
		
		String modified = sa.buildAuthorizedQuery(query,nikita);
		assertNotNull(modified);
		log.debug("Final query: "+modified);
		//and still can parse
		Reader reader = new StringReader(modified);
		CCJSqlParser parser = new CCJSqlParser(reader);
		parser.Statement();		
	}

	
	@Test(expected=AuthorizationException.class)
	public void simpleSelectTestFailure1() throws SQLException {
		String query = "SELECT * FROM ACCOUNT";
		log.debug("Initial query query: "+query);
		sa.buildAuthorizedQuery(query,null);
	}

	@Test(expected=AuthorizationException.class)
	public void simpleSelectTestFailure2() throws SQLException {	
		Account.removeProtectionAnyNikitaAny(pt);
		Account.addProtectionAnyNikitaAny(pt);
		String query = "SELECT * FROM ATABLE";
		log.debug("Initial query query: "+query);
		sa.buildAuthorizedQuery(query,nikita);
	}

	@Test(expected=AuthorizationException.class)
	public void simpleSelectTestFailure3() throws SQLException {
		Account.removeProtectionAnyNikitaAny(pt);
		Account.addProtectionAnyNikitaAny(pt);
		String query = "SELECT *,ACCESSOR_ID FROM ACCOUNT";
		log.debug("Initial query query: "+query);
		sa.buildAuthorizedQuery(query,nikita);
	}

	@Test(expected=AuthorizationException.class)
	public void simpleSelectNikitaPowerUserFailureTest() throws ParseException, SQLException {
		Account.removeProtectionAnyNikitaAny(pt);
		Account.addProtectionAnyNikitaAny(pt);
		
		String query = "SELECT * FROM ACCOUNT";
		log.debug("Initial query query: "+query);
		
		SqlAuthorizer sa2;

		UserAuthentication ua2 = new UserAuthenticationDefault();
		((Connectable) ua2).setConnection(AegisDataSourceFactory.TEST_DEMO_CONNECTION_FOR_POWER_USER, "AUTHENTICATION");
		sa2 = new SqlAuthorizer(gr, ua2,"TEST_DEMO");
		sa2.setAllowedGroups(AegisDataSourceFactory.TEST_DEMO_CONNECTION_FOR_POWER_USER.getAllowedGroups());
		
		sa2.buildAuthorizedQuery(query,nikita);

	}
	
	@Test
	public void greedyStarTest() throws SQLException {
		Account.removeProtectionAnyMikeAny(pt);
		Account.removeProtectionGuestAnyAny(pt);
		Account.removeProtectionAdminAnyAny(pt);
		Account.removeProtectionUserAnyAny(pt);
		Account.removeProtectionPowerUserAnyAny(pt);
		
		Account.addProtectionAdminAnyAny(pt);
		Account.addProtectionGuestAnyAny(pt);
		Account.addProtectionPowerUserAnyAny(pt);
		
		String query = "SELECT * FROM ACCOUNT";
		log.debug("Initial query query: "+query);
		
		String modified = sa.buildAuthorizedQuery(query,mike);
		assertNotNull(modified);

		log.debug("Final query: "+modified);
		assertTrue(modified.contains("ACCOUNT.ID")); // not available for guest
		
		Account.addProtectionAnyMikeAny(pt);
	}
	
	@Test
	public void testTablelessQuery() throws SQLException {
		String query = "SELECT 'TEST'"; // this statement does not need authentication. No tables involved
		log.debug("Initial query query: "+query);
		
		String modified = sa.buildAuthorizedQuery(query,null);
		assertNotNull(modified);
		assertEquals(query,modified);

		String modified2 = sa.buildAuthorizedQuery(query,mike);
		assertNotNull(modified2);
		assertEquals(query,modified2);

		
	}

	@Test
	public void testCOUNTQuery() throws SQLException {
		String query = "SELECT COUNT(*) FROM ACCOUNT SUBMITTED BY 'mike' IDENTIFIED BY '12345'"; // this statement does not need authentication. No tables involved
		log.debug("Initial query query: "+query);
		
		String modified = sa.buildAuthorizedQuery(query,null);
		assertNotNull(modified);
		
	}

	
}
