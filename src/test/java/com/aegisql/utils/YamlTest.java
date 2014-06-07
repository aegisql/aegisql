package com.aegisql.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.access.AccessPattern;
import com.aegisql.access.Granted;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.authorization.SqlAuthorizer;
import com.aegisql.testing_tools.ClassUtils;
import com.aegisql.testing_tools.YamlAuthorizationTestProvider;
import com.esotericsoftware.yamlbeans.YamlReader;

public class YamlTest {

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
	public void yamlProviderTest() throws Exception {
		String path = ClassUtils.getPath(this) + "test.yml";
		YamlAuthorizationTestProvider ytp = new YamlAuthorizationTestProvider(path);
		System.out.println("Description:\n"+ytp.getDescription("YAML_TEST")+"\n");		
		String sql            = ytp.getSQL("YAML_TEST");
		String asql           = ytp.getExpected("YAML_TEST");
		String user           = ytp.getUser("YAML_TEST");
		String password       = ytp.getPassword("YAML_TEST");
		UserAuthentication ua = ytp.getUserAuthentication("YAML_TEST");
		Granted g             = ytp.getGranted("YAML_TEST");
		SubmittedBy sb        = ytp.getSubmittedBy("YAML_TEST");
		SqlAuthorizer sa      = ytp.getSqlAuthorizer("YAML_TEST");

		assertNotNull(sql);
		assertNotNull(asql);
		assertNotNull(user);
		assertNotNull(password);
		assertNotNull(ua);
		assertNotNull(g);
		assertNotNull(sa);
		assertNotNull(sb);

		assertEquals("mike", user);
		assertEquals("12345", password);
		assertTrue(sql.contains("select"));
		assertTrue(asql.contains("where"));
		
		System.out.println("Groups: "+ua.getUserGroups("mike", "12345"));
		AccessPattern a = new AccessPattern("USER",null,null,null,"ACCESSOR_ID",null);
		System.out.println("Granted: "+g.getGrantedAccess("a_table", a));
		
		
		String sql2 = sa.buildAuthorizedQuery(sql, null);
		String sql3 = sa.buildAuthorizedQuery(sql, sb);
		System.out.println("SQL1: "+sql);
		System.out.println("SQL2: "+sql2);
		System.out.println("SQL3: "+sql3);
		System.out.println("SUBMITTER: "+sb);
		
	}

	@Test
	public void yamlMinimalProviderTest() throws Exception {
		String path = ClassUtils.getPath(this) + "test.yml";
		YamlAuthorizationTestProvider ytp = new YamlAuthorizationTestProvider(path);
		System.out.println("Description:\n"+ytp.getDescription("MINIMAL_YAML_TEST")+"\n");
		String sql            = ytp.getSQL("MINIMAL_YAML_TEST");
		String asql           = ytp.getExpected("MINIMAL_YAML_TEST");
		String user           = ytp.getUser("MINIMAL_YAML_TEST");
		String password       = ytp.getPassword("MINIMAL_YAML_TEST");
		UserAuthentication ua = ytp.getUserAuthentication("MINIMAL_YAML_TEST");
		Granted g             = ytp.getGranted("MINIMAL_YAML_TEST");
		SubmittedBy sb        = ytp.getSubmittedBy("MINIMAL_YAML_TEST");
		SqlAuthorizer sa      = ytp.getSqlAuthorizer("MINIMAL_YAML_TEST");

		assertNotNull(sql);
		assertNotNull(asql);
		assertNull(user);
		assertNull(password);
		assertNotNull(ua);
		assertNotNull(g);
		assertNotNull(sa);
		assertNotNull(sb);

		assertTrue(sql.contains("select"));
		assertTrue(asql.contains("where"));
		
		System.out.println("Groups: "+ua.getUserGroups("mike", "12345"));
		AccessPattern a = new AccessPattern("USER",null,null,null,"ACCESSOR_ID",null);
		System.out.println("Granted: "+g.getGrantedAccess("a_table", a));
		
		
		String sql2 = sa.buildAuthorizedQuery(sql, null);
		String sql3 = sa.buildAuthorizedQuery(sql, sb);
		System.out.println("SQL1: "+sql);
		System.out.println("SQL2: "+sql2);
		System.out.println("SQL3: "+sql3);
		System.out.println("SUBMITTER: "+sb);
		
	}

	
	@Test
	public void yamlTest() throws Exception {
		String path = ClassUtils.getPath(this) + "test.yml";
		System.out.println("Yaml file "+path);
		YamlReader yr = new YamlReader(new FileReader(path));
    	while(true) {
    		Map<?, ?> o = (Map<?, ?>) yr.read();
    		if( o == null ) break;
			System.out.println("Object = "+o);
    		assertNotNull(o.get("GROUPS"));
    		assertNotNull(o.get("SQL"));
    	}		
	}

}
