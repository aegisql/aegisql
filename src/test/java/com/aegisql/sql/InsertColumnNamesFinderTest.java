package com.aegisql.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;

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

public class InsertColumnNamesFinderTest {

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

	private Map<String,Set<String>> runInsertTest(String query) throws ParseException {
		Statement insert = ParserUtils.parseQuery(query);	
		cnf = new QueryAnalizer(insert);
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
	public void testSimpleInsert() throws ParseException {
		String query = "insert into my_table (id,name,age) values(1,'mike',43)";
		Map<String,Set<String>> columns = runInsertTest(query);
		assertEquals(1,columns.size());
		assertContinsColumns(columns,"my_table","name","id","age");
	}

}
