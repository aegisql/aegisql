package com.aegisql.authorization;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.sql.SQLException;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.testing_tools.ClassUtils;
import com.aegisql.testing_tools.YamlAuthorizationTestProvider;

public class AuthorizerTest {

	public final static Logger log = LoggerFactory.getLogger("UnitTestLogger");
	@Rule
	public TestName testName = new TestName();
	@Rule
	public TestRule watchman = new TestWatcher() {
		public void starting(Description description) {
			super.starting(description);
			log.debug("=== START {} ================================\n", description.getMethodName());
		}
		public void finished(Description description) {
			super.finished(description);
			log.debug("=== END {} ================================\n\n", description.getMethodName());
		}
	};
	
	static YamlAuthorizationTestProvider ytp;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String path = ClassUtils.getPath(AuthorizerTest.class) + "tests.yml";
		log.debug("Test file name: {}",path);
		ytp = new YamlAuthorizationTestProvider(path);
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
	public void testSelectSqls() throws SQLException, ParseException {
		runTest("TEST_1");
		runTest("TEST_1.1");
		runTest("TEST_2");
		runTest("TEST_3");
		runTest("TEST_4");
		runTest("TEST_5");
		runTest("TEST_6");
		runTest("TEST_7");
	}

	@Test
	public void testUpdateSqls() throws SQLException, ParseException {
		runTest("TEST_8");
		runTest("TEST_8.1");
		runTest("TEST_8.2");
		runTest("TEST_8.3");
	}

	@Test
	public void testDeleteSqls() throws SQLException, ParseException {
		runTest("TEST_9");
		runTest("TEST_9.1");
		runTest("TEST_9.2");
	}
	
	@Test
	public void testInsertSqls() throws SQLException, ParseException {
		runTest("TEST_10");
		runTest("TEST_10.1");
		runTest("TEST_10.2");
	}

	@Test
	public void testComplexDeleteSqls() throws SQLException, ParseException {
		runTest("TEST_11");
	}

	@Test
	public void testComplexInsertSqls() throws SQLException, ParseException {
		runTest("TEST_12");
		runTest("TEST_12.1");
	}

	@Ignore("Multi-table update is not supported by the parser. Must be fixed there first")
	@Test
	public void testComplexUpdateSqls() throws SQLException, ParseException {
		runTest("TEST_13");
	}

	@Test
	public void testReplaceSqls() throws SQLException, ParseException {
		runTest("TEST_14");
	}

	@Test
	public void testComplexReplaceSqls() throws SQLException, ParseException {
		runTest("TEST_14.1");
		runTest("TEST_14.2");
		runTest("TEST_14.3");
	}
	
	private void runTest(String testName) throws SQLException, ParseException {
		log.debug("Test name: {}",testName);
		String sql            = ytp.getSQL(testName);
		SqlAuthorizer sa      = ytp.getSqlAuthorizer(testName);
		SubmittedBy   sb      = ytp.getSubmittedBy(testName);
		String expected       = ytp.getExpected(testName);
		assertNotNull(sql);
		assertNotNull(sa);
		String sql2 = null;
		try {
			sql2 = sa.buildAuthorizedQuery(sql, sb );
		} catch (Exception e ) {
			if(e.getClass().getCanonicalName().equals(expected)) {
				log.debug("Expected exception {}",e.getMessage());
				return;
			} else {
				throw e;
			}
		}
		log.debug("Original SQL {}",sql);
		log.debug("Authorized SQL {}",sql2);

		CCJSqlParser p1 = new CCJSqlParser(new StringReader( sql2 ));
		Statement st1   = p1.Statement();
	    Statement st2   = ytp.getExpectedStatement(testName);

		log.debug("st1 {}",st1.toString());
		log.debug("st2 {}",st2.toString());

		assertEquals(st1.toString(),st2.toString());
	    
	}
	
}
