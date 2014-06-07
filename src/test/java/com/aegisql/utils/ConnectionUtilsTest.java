package com.aegisql.utils;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectionUtilsTest {

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
	public void testEmptyQuery() {
		Map<String, String> prop = ConnectionUtils.getQueryProperties("mysql", "");
		assertNotNull(prop);
		assertEquals(0,prop.size());
	}

	@Test
	public void testSimpleMysqlQuery() {
		Map<String, String> prop = ConnectionUtils.getQueryProperties("mysql", "user=mike&password=12345");
		System.out.println(prop);
		assertNotNull(prop);
		assertEquals(2,prop.size());
		assertTrue(prop.containsKey("user"));
		assertTrue(prop.containsKey("password"));
		assertEquals("mike",prop.get("user"));
		assertEquals("12345",prop.get("password"));
	}

	@Test
	public void testSimplePostgresQuery() {
		Map<String, String> prop = ConnectionUtils.getQueryProperties("postgres", "user=mike&password=12345");
		System.out.println(prop);
		assertNotNull(prop);
		assertEquals(2,prop.size());
		assertTrue(prop.containsKey("user"));
		assertTrue(prop.containsKey("password"));
		assertEquals("mike",prop.get("user"));
		assertEquals("12345",prop.get("password"));
	}

	@Test
	public void testSimpleSybaseQuery() {
		Map<String, String> prop = ConnectionUtils.getQueryProperties("jtds:sybase", "user=mike;password=12345");
		System.out.println(prop);
		assertNotNull(prop);
		assertEquals(2,prop.size());
		assertTrue(prop.containsKey("user"));
		assertTrue(prop.containsKey("password"));
		assertEquals("mike",prop.get("user"));
		assertEquals("12345",prop.get("password"));
	}
	
	@Test
	public void testStrangeQuery() {
		Map<String, String> prop = ConnectionUtils.getQueryProperties("mysql", "&&user=mike=&&password=12345&q=&w");
		System.out.println(prop);
		assertNotNull(prop);
		assertEquals(4,prop.size());
		assertTrue(prop.containsKey("user"));
		assertTrue(prop.containsKey("password"));
		assertTrue(prop.containsKey("q"));
		assertTrue(prop.containsKey("w"));
		assertEquals("mike=",prop.get("user"));
		assertEquals("12345",prop.get("password"));
		assertEquals("",prop.get("q"));
		assertNull(prop.get("w"));
	}

	
	
}
