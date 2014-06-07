package com.aegisql.conf;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfiguratorTest {
	
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("aegis.config.path", "./src/test/java/");
	}

	@Test
	public void testPaths() {
		Configurator c = new Configurator();
		assertNotNull(c.getClassPath());
		assertNotNull(c.getClassPathEntries());
		assertTrue(c.getClassPathEntries().length > 0);
		assertNotNull(c.getCurrentDir());
		assertNotNull(c.getConfigPath());
		System.out.println("pathSeparator="+File.separator);
		System.out.println("ClassPath="+c.getClassPath());
		System.out.println("CurrentDir="+c.getCurrentDir());
	}

	@Test
	public void testConfigFile() throws Exception {
		Configurator c = new Configurator();
		File cf = c.lookupConfigFile();
		assertNotNull(cf);
		String path = cf.getAbsolutePath();
		System.out.println("Path="+path);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(cf);
		Node root = doc.getDocumentElement();
		root.normalize();
		System.out.println("Root: "+root.getNodeName());
		
		NodeList nodes = doc.getElementsByTagName("authentication_provider");
		System.out.println("Nodes: "+nodes.getLength());
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				System.out.println("Node: "+node);
				NamedNodeMap attributes = node.getAttributes();
				System.out.println("Attributes: "+attributes.getNamedItem("id")+" "+attributes.getNamedItem("class"));
			}
		}
	}
	
	
}
