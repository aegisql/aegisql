package com.aegisql.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;

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

import com.aegisql.access.TableQueryType;
import com.aegisql.testing_tools.ParserUtils;

public class SqlEditorTestTest {

	QueryAnalizer cnf;
	QueryEditor rac;
	
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
		rac = new QueryEditor(TableQueryType.SELECT);
	}

	@After
	public void tearDown() throws Exception {
	}

	Select select;
	
	private Map<String,Set<String>> runColumnFinderTest(String query) throws ParseException {
		select = (Select) ParserUtils.parseQuery(query);	
		cnf = new QueryAnalizer(select);
		Map<String,Set<String>> columns = cnf.getAllColumns();
		assertNotNull(columns);
		log.debug("Query: {}",query);
		log.debug("Tables: {}",columns.keySet());
		log.debug("Columns: {}",columns);	
		return columns;
	}

	private void runColumnReplacerTest(String query,Map<String,List<String>> replaceColumns) throws ParseException {
		select = (Select) ParserUtils.parseQuery(query);	
		rac.selectReplaceStar(select, replaceColumns);
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
	public void testLearningReplaceStar() throws ParseException {
		
		// Class AllColumns
		// Class AllTableColumns
		// Replace to SelectExpressionItem
		
		String query = "select * from my_table where ID=14 AND ACCESSOR_ID=1001";
		Map<String,Set<String>> columns = runColumnFinderTest(query);
		assertEquals(1,columns.size());
		assertContinsColumns(columns,"my_table","*","ID","ACCESSOR_ID");
		log.debug("Statement before: {}",select.toString());
		
		PlainSelect p = (PlainSelect) select.getSelectBody();
		List<SelectItem> si = p.getSelectItems();
		SelectItem i = si.get(0);
		log.debug("Select item {} type {}",i.toString(),i.getClass().getSimpleName());
		SelectExpressionItem e1 = new SelectExpressionItem();
		SelectExpressionItem e2 = new SelectExpressionItem();
		Column c1 = new Column();
		Column c2 = new Column();
		Table t = new Table();
		c1.setColumnName("ID");
		c2.setColumnName("NAME");
		t.setName("my_table");
		c1.setTable(t);
		c2.setTable(t);
		e1.setExpression(c1);
		e2.setExpression(c2);
		si.set(0, e1);
		si.add(e2);
		log.debug("Statement after: {}",select.toString());
        assertEquals("SELECT my_table.ID, my_table.NAME FROM my_table WHERE ID = 14 AND ACCESSOR_ID = 1001",select.toString());
        
        Map<String,Set<String>> columns2 = runColumnFinderTest(select.toString());
		assertEquals(1,columns2.size());
		assertContinsColumns(columns2,"my_table","my_table.NAME","my_table.ID","ID","ACCESSOR_ID");
        
	}

	@Test
	public void testReplaceStarWithFactory() throws ParseException {
		String query = "select * from my_table where ID=15";
		Map<String,Set<String>> columns = runColumnFinderTest(query);
		assertEquals(1,columns.size());
		assertContinsColumns(columns,"my_table","*","ID");
		log.debug("Statement before: {}",select.toString());
		
		PlainSelect p = (PlainSelect) select.getSelectBody();
		p.setSelectItems(SelectItemsFactory.getSelectItemsList("my_table", "ID","NAME"));
		log.debug("Statement after: {}",select.toString());
		assertEquals("SELECT my_table.ID, my_table.NAME FROM my_table WHERE ID = 15",select.toString());
	}


	@Test
	public void testReplaceAllStarWithReplaecer() throws ParseException {
		String query = "select * from my_table a where ID=15";
		Map<String,List<String>> replaceColumns = new HashMap<String,List<String>>();
		List<String> col = Arrays.asList("id","name","age");
		replaceColumns.put("my_table", col);
		runColumnReplacerTest(query,replaceColumns);
		String query2 = select.toString();
		log.debug("Statement after: {}",query2);

		Map<String,Set<String>> columns = runColumnFinderTest(query);
		assertEquals(1,columns.size());
		assertContinsColumns(columns,"my_table","*","ID");
		log.debug("Statement 1: {}",select.toString());

		Map<String,Set<String>> columns2 = runColumnFinderTest(query2);
		assertEquals(1,columns.size());
		assertContinsColumns(columns2,"my_table","my_table.id","my_table.name","my_table.age","ID");
		log.debug("Statement 2: {}",select.toString());
		
	}

	@Test
	public void testReplaceAllStarWithReplaecer2() throws ParseException {
		String query = "select * from my_table a, table_2 b where a.ID=b.ID";
		Map<String,List<String>> replaceColumns = new HashMap<String,List<String>>();
		List<String> col1 = Arrays.asList("id","name","age");
		List<String> col2 = Arrays.asList("id","grade","period");
		replaceColumns.put("my_table", col1);
		replaceColumns.put("table_2", col2);
		runColumnReplacerTest(query,replaceColumns);
		String query2 = select.toString();
		log.debug("Statement after: {}",query2);

		Map<String,Set<String>> columns = runColumnFinderTest(query);
		assertEquals(2,columns.size());
		assertContinsColumns(columns,"my_table","*","a.ID");
		assertContinsColumns(columns,"table_2","*","b.ID");
		log.debug("Statement 1: {}",select.toString());

		Map<String,Set<String>> columns2 = runColumnFinderTest(query2);
		assertEquals(2,columns.size());
		assertContinsColumns(columns2,"my_table","my_table.id","my_table.name","my_table.age","a.ID");
		assertContinsColumns(columns2,"table_2","table_2.id","table_2.grade","table_2.period","b.ID");
		log.debug("Statement 2: {}",select.toString());
		
	}

	
	@Test
	public void testReplaceStarWithReplaecer() throws ParseException {
		String query = "select a.* from my_table a where ID=15";
		Map<String,List<String>> replaceColumns = new HashMap<String,List<String>>();
		List<String> col = Arrays.asList("id","name","age");
		replaceColumns.put("my_table", col);
		runColumnReplacerTest(query,replaceColumns);
		String query2 = select.toString();
		log.debug("Statement after: {}",query2);

		Map<String,Set<String>> columns = runColumnFinderTest(query);
		assertEquals(1,columns.size());
		assertContinsColumns(columns,"my_table","a.*","ID");
		log.debug("Statement 1: {}",select.toString());

		Map<String,Set<String>> columns2 = runColumnFinderTest(query2);
		assertEquals(1,columns.size());
		assertContinsColumns(columns2,"my_table","my_table.id","my_table.name","my_table.age","ID");
		log.debug("Statement 2: {}",select.toString());
		
	}

	@Test
	public void testReplaceStarWithReplaecer2() throws ParseException {
		String query = "select a.*,b.* from my_table a,table_2 b where a.ID=b.ID";
		Map<String,List<String>> replaceColumns = new HashMap<String,List<String>>();
		List<String> col1 = Arrays.asList("id","name","age");
		List<String> col2 = Arrays.asList("id","grade","period");
		replaceColumns.put("my_table", col1);
		replaceColumns.put("table_2", col2);
		runColumnReplacerTest(query,replaceColumns);
		String query2 = select.toString();
		log.debug("Statement after: {}",query2);

		Map<String,Set<String>> columns = runColumnFinderTest(query);
		assertEquals(2,columns.size());
		assertContinsColumns(columns,"my_table","a.*","a.ID");
		assertContinsColumns(columns,"table_2","b.*","b.ID");
		log.debug("Statement 1: {}",select.toString());

		Map<String,Set<String>> columns2 = runColumnFinderTest(query2);
		assertEquals(2,columns.size());
		assertContinsColumns(columns2,"my_table","my_table.id","my_table.name","my_table.age","a.ID");
		assertContinsColumns(columns2,"table_2","table_2.id","table_2.grade","table_2.period","b.ID");
		log.debug("Statement 2: {}",select.toString());
		
	}

	@Test
	public void testReplaceStarWithReplaecer3() throws ParseException {
		String query = "select a.*,b.grade from my_table a,table_2 b where a.ID=b.ID";
		Map<String,List<String>> replaceColumns = new HashMap<String,List<String>>();
		List<String> col1 = Arrays.asList("id","name","age");
		replaceColumns.put("my_table", col1);
		runColumnReplacerTest(query,replaceColumns);
		String query2 = select.toString();
		log.debug("Statement after: {}",query2);

		Map<String,Set<String>> columns = runColumnFinderTest(query);
		assertEquals(2,columns.size());
		assertContinsColumns(columns,"my_table","a.*","a.ID");
		assertContinsColumns(columns,"table_2","b.ID","b.grade");
		log.debug("Statement 1: {}",select.toString());

		Map<String,Set<String>> columns2 = runColumnFinderTest(query2);
		assertEquals(2,columns.size());
		assertContinsColumns(columns2,"my_table","my_table.id","my_table.name","my_table.age","a.ID");
		assertContinsColumns(columns2,"table_2","b.ID","b.grade");
		log.debug("Statement 2: {}",select.toString());
		
	}

	@Test
	public void testDeleteAddAccessorId() throws ParseException {
		String query = "delete from sometable";
		log.debug("Delete query: "+query);
		Delete delete = (Delete) ParserUtils.parseQuery(query);	
		rac.whereAddAccessor(delete, "test_demo","ACCOUNT",null,"ACCESSOR_ID", "1001",TableQueryType.DELETE);
		log.debug("Delete accessor: "+delete.toString());
	}

	@Test
	public void testDeleteAddAccessorIdToExistingWhere() throws ParseException {
		String query = "delete from sometable where name='mike'";
		log.debug("Delete query 2: "+query);
		Delete delete = (Delete) ParserUtils.parseQuery(query);	
		rac.whereAddAccessor(delete, "test_demo","ACCOUNT",null,"ACCESSOR_ID", "1001",TableQueryType.DELETE);
		log.debug("Delete accessor 2: "+delete.toString());
	}

	@Test
	public void testUpdateAddAccessorId() throws ParseException {
		String query = "update sometable set name = 'mike'";
		log.debug("Update query: "+query);
		Update update = (Update) ParserUtils.parseQuery(query);	
		rac.whereAddAccessor(update, "test_demo","ACCOUNT",null,"ACCESSOR_ID", "1001",TableQueryType.UPDATE);
		log.debug("Update accessor: "+update.toString());
	}

	@Test
	public void testUpdateAddAccessorIdToExistingWhere() throws ParseException {
		String query = "update sometable set name = 'mike' where name='nikita'";
		log.debug("Update query 2: "+query);
		Update update = (Update) ParserUtils.parseQuery(query);	
		rac.whereAddAccessor(update, "test_demo","ACCOUNT",null,"ACCESSOR_ID", "1001",TableQueryType.UPDATE);
		log.debug("Update accessor 2: "+update.toString());
	}

	@Test
	public void testSelectAddAccessorId() throws ParseException {
		String query = "select name from sometable";
		log.debug("Select query: "+query);
		Select select = (Select) ParserUtils.parseQuery(query);	
		rac.whereAddAccessor(select, "test_demo","ACCOUNT",null,"ACCESSOR_ID", "1001",TableQueryType.SELECT);
		log.debug("Select accessor: "+select.toString());
	}

	@Test
	public void testSelectAddAccessorIdToExistingWhere() throws ParseException {
		String query = "select age from sometable where name='nikita'";
		log.debug("Select query 2: "+query);
		Select select = (Select) ParserUtils.parseQuery(query);	
		rac.whereAddAccessor(select, "test_demo","ACCOUNT",null,"ACCESSOR_ID", "1001",TableQueryType.SELECT);
		log.debug("Select accessor 2: "+select.toString());
	}
	
	@Test
	public void testInsertAddAccessorId() throws ParseException {
		String query = "insert into test_demo.ACCOUNT (name,age) values('nikita',14)";
		log.debug("Insert query: "+query);
		Insert insert = (Insert) ParserUtils.parseQuery(query);	
		rac.whereAddAccessor(insert, "test_demo","ACCOUNT",null,"ACCESSOR_ID", "1001",TableQueryType.INSERT);
		log.debug("Insert accessor: "+insert.toString());
	}

	@Test
	public void testInsertAddMultyAccessorId() throws ParseException {
		String query = "insert into test_demo.ACCOUNT (name,age) values('nikita',14),('mike',43)";
		log.debug("Insert query 2: "+query);
		Insert insert = (Insert) ParserUtils.parseQuery(query);	
		rac.whereAddAccessor(insert, "test_demo","ACCOUNT",null,"ACCESSOR_ID", "1001",TableQueryType.INSERT);
		log.debug("Insert accessor 2 : "+insert.toString());
	}

}
