package com.aegisql.sqltest.mysql.test_schema_1;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.sql.SQLException;

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

import com.aegisql.testing_tools.ClassUtils;
import com.aegisql.testing_tools.MySQLLocalClient;

public class TestSchema1Test {

	private static String URL;
	private static MySQLLocalClient mysql;
	
	public final Logger log = LoggerFactory.getLogger("UnitTestLogger");
	@Rule
	public TestName testName = new TestName();
	@Rule
	public TestRule watchman = new TestWatcher() {
		public void starting(Description description) {
			super.starting(description);
			String method = description.getMethodName();
			log.debug("Starting {} ================================", method);
			try {
				mysql.executeFile(ClassUtils.getAbsolutePath(description.getMethodName()+".sql"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public void finished(Description description) {
			super.starting(description);
			String method = description.getMethodName();
			log.debug("Finished {} ================================", method);
			try {
				mysql.executeFile(ClassUtils.getAbsolutePath("_"+description.getMethodName()+".sql"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		System.out.println("Class="+ClassUtils.getAbsolutePath());
		System.out.println("File="+ClassUtils.getAbsolutePath("before_test.sql"));
		System.out.println("Package="+ClassUtils.getTopPackage());

		URL = ClassUtils.getAbsolutePath();
		System.out.println("URL "+URL);
		
		mysql = new MySQLLocalClient("jdbc:mysql://localhost/"+ClassUtils.getTopPackage()+"?user=root&password=");
		mysql.resetDatabase(ClassUtils.getTopPackage());
		mysql.executeFile(ClassUtils.getAbsolutePath("before_test.sql"));
		mysql.executeFile(ClassUtils.getAbsolutePath("before_rest.sql"));
		
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
	public void test() {
	}

}
