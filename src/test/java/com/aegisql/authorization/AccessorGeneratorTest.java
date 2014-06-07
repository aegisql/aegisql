package com.aegisql.authorization;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

import com.aegisql.access.AccessPattern;
import com.aegisql.access.AccessPatternsGenerator;
import com.aegisql.authentication.Group;

public class AccessorGeneratorTest {
	
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


	Group userGroup    = new Group(1, "user", true, "ACCESSOR_ID", 1001);

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
	public void testAccessorGenerator1() {
		List<Group> groups = new ArrayList<>();
		groups.add(userGroup);
		Set<AccessPattern> accessors = AccessPatternsGenerator.buildAllAccessors("mike", "PC", "127.0.0.1", groups);
		assertNotNull(accessors);
		logAccessorList(accessors);
	}

	private void logAccessorList(Set<AccessPattern> ac) {
		log.debug("Found accessors: {}",ac.size());
		for( AccessPattern a: ac) {
			log.debug(a.toString());
		}
	}
	
}
