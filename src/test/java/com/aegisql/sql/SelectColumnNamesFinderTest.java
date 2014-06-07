package com.aegisql.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.select.Select;

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

import com.aegisql.testing_tools.ParserUtils;

public class SelectColumnNamesFinderTest {

	QueryAnalizer cnf;
	
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

	private Map<String,Set<String>> runSelectTest(String query) throws ParseException {
		Select select = (Select) ParserUtils.parseQuery(query);	
		cnf = new QueryAnalizer(select);
		Map<String,Set<String>> columns = cnf.getAllColumns();
		assertNotNull(columns);
		log.debug("Query: {}",query);
		log.debug("Tables: {}",columns.keySet());
		log.debug("Columns: {}",columns);	
		return columns;
	}
	
	private void assertContinsColumns(Map<String,Set<String>> columns,String table,String... col) {
		assertTrue("Should contain "+table,columns.containsKey(table));
		Set<String> colNames = columns.get(table);
		assertEquals("Expected "+col.length+" columns",col.length,colNames.size());
		for(String c:col) {
			assertTrue("Contains "+c,colNames.contains(c));
		}
	}
	
	@Test
	public void testSimpleQuery() throws ParseException {
		String query = "select * from my_table where id=15";
		Map<String,Set<String>> columns = runSelectTest(query);
		assertEquals(1,columns.size());
		assertContinsColumns(columns,"my_table","*","id");
	}

	@Test
	public void testSimpleJoinQuery() throws ParseException {
		String query = "select a.id,a.name,b.age from table_a a,table_b b where a.id=b.a_id";
		Map<String,Set<String>> columns = runSelectTest(query);
		assertEquals(2,columns.size());
		assertContinsColumns(columns,"table_a","a.id","a.name");
		assertContinsColumns(columns,"table_b","b.age","b.a_id");
	}
	
	@Test
	public void testSimpleSubQuery() throws ParseException {
		String query = "select a.name,a.age from table_a a where a.id in(select a_id from table_b where age > 10)";
		Map<String,Set<String>> columns = runSelectTest(query);
		assertEquals(2,columns.size());
		assertContinsColumns(columns,"table_a","a.age","a.name","a.id");
		assertContinsColumns(columns,"table_b","a_id","age");
	}

	
	@Test
	public void testSimpleSubQuery2() throws ParseException {
		String query = "select name,age from table_a where id in(select a_id from table_b where hight > 10)";
		Map<String,Set<String>> columns = runSelectTest(query);
		assertEquals(2,columns.size());
		assertContinsColumns(columns,"table_a","age","name","id");
		assertContinsColumns(columns,"table_b","a_id","hight");
	}
	
	@Test
	public void testOneStartQuery() throws ParseException {
		String query = "select * from table_a a,table_b b";
		Map<String,Set<String>> columns = runSelectTest(query);
		assertEquals(2,columns.size());
		assertContinsColumns(columns,"table_a","*");
		assertContinsColumns(columns,"table_b","*");
	}

	@Test
	public void testTwoStartQuery() throws ParseException {
		String query = "select a.*,b.* from table_a a,table_b b";
		Map<String,Set<String>> columns = runSelectTest(query);
		assertEquals(2,columns.size());
		assertContinsColumns(columns,"table_a","a.*");
		assertContinsColumns(columns,"table_b","b.*");
	}
	
}
