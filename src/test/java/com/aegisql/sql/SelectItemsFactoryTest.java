package com.aegisql.sql;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.statement.select.SelectItem;

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

public class SelectItemsFactoryTest {
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

	@Test
	public void testSetectItems() {
		List<SelectItem> items = SelectItemsFactory.getSelectItemsList("my_table", "id","name");
		assertEquals(2,items.size());
		log.debug("Select Items {}",items);
	}

	@Test
	public void testSetectListItems() {
		ArrayList<String> list = new ArrayList<String>(){
			private static final long serialVersionUID = 1L;
		{
			add("id");
			add("name");
		}};
		List<SelectItem> items = SelectItemsFactory.getSelectItemsList("my_table", list);
		assertEquals(2,items.size());
		log.debug("Select Items {}",items);
	}

	@Test(expected=RuntimeException.class)
	public void testSetectItemsException() {
		SelectItemsFactory.getSelectItemsList("my_table", "id","name","*");
	}

}
