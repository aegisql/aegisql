package com.aegisql.authentication;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aegisql.conf.Configurator;

public class XmlAuthenticationProviderTest {
	
	Configurator c = new Configurator();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("aegis.config.path", "./src/test/java/com/aegisql/authentication/");
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
	public void test() throws Exception {
		File f = c.lookupConfigFile();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(f);
		Node root = doc.getDocumentElement();
		root.normalize();
		
		NodeList nodes          = doc.getElementsByTagName("authentication_provider");
		Node apn                = nodes.item(1);
		NamedNodeMap attributes = apn.getAttributes();
		Node nameNode           = attributes.getNamedItem("name");
		Node classNode          = attributes.getNamedItem("class");
		String name             = nameNode.getNodeValue();
		String clazz            = classNode.getNodeValue();
		System.out.println("attributes: "+name+ " " +clazz);
		assertEquals("xml_authentication",name);
		assertEquals("com.aegisql.authentication.XMLAuthenticationProvider",clazz);
		Class<?> pClass = Class.forName(clazz);
		
		AuthenticationProvider ap = (AuthenticationProvider) pClass.newInstance();
		UserAuthentication ua = ap.provideUserAuthentication(apn);
		assertNotNull(ua);
		assertNotNull(ua);
		List<Group> g = ua.getUserGroups("mike", "12345");
		assertNotNull(g);
		System.out.println(g);
		assertEquals(2,g.size());

		List<Group> g2 = ua.getUserGroups("nikita", "12345");
		assertNotNull(g2);
		System.out.println(g2);
		assertEquals(1,g2.size());

	}

}
