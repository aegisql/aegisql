package com.aegisql.conf;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aegisql.authentication.AuthenticationProvider;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.authorization.Authorizer;

public class Configurator {
	
	public final org.slf4j.Logger log = LoggerFactory.getLogger(Configurator.class);
	
	private final static String DEFAULT_CONFIG_NAME = "aegis.xml";
	
	private final String configPath;
	private final String classPath;
	private final String[] classPathEntries;
	private final String currentDir;
	
	private Document doc = null;
	
	private Map<String,UserAuthentication> authentications = null;
	private Map<String,Authorizer> authorizers = null;
	
	public Configurator() {
		configPath       = System.getProperty("aegis.config.path");
		classPath        = System.getProperty("java.class.path");
		classPathEntries = classPath.split(File.pathSeparator);
		currentDir       = new File(".").getAbsolutePath().replace(".", "");
	}

	public String getConfigPath() {
		return configPath;
	}

	public String getClassPath() {
		return classPath;
	}

	public String[] getClassPathEntries() {
		return classPathEntries;
	}

	public String getCurrentDir() {
		return currentDir;
	}
	
	public File lookupConfigFile() throws Exception {
		File f = null;
		if(configPath != null) {
			log.debug("aegis.config.path found: "+configPath);
			/*
			 * If user specified the aegis.config.path parameter then use it. 
			 * If it is not found - do not attempt to find other configs.
			 * */
			
			f = new File(configPath);
			if( f.isDirectory()) {
				f = new File(configPath+File.separator+DEFAULT_CONFIG_NAME);
			}
			if( ! f.exists()) {
				log.error("Config file not found. Expected {}",configPath+File.separator+DEFAULT_CONFIG_NAME);
				throw new Exception("File "+configPath+" does not exist");
			}
			if( ! f.canRead() ) {
				log.error("Config file {} not readable.",configPath+File.separator+DEFAULT_CONFIG_NAME);
				throw new Exception("File "+configPath+" is not readable");
			}
		} else {
			/*
			 * Try to lookup in current directory and then in all classpath directories
			 * */
			f = new File(currentDir + DEFAULT_CONFIG_NAME);
			if( f.exists() ) {
				if( ! f.canRead() ) {
					throw new Exception("File "+currentDir+DEFAULT_CONFIG_NAME+" is not readable");
				}
				log.debug("Found configuration file in current directory: {}"+currentDir+DEFAULT_CONFIG_NAME);
			} else {
				for( String path : classPathEntries ) {
					if( path.toLowerCase().endsWith("jar") || path.toLowerCase().endsWith("zip") ) {
						continue;
					}
					f = new File(path+File.separator+DEFAULT_CONFIG_NAME);
					if( f.exists() && f.canRead() ) {
						log.debug("Found configuration file in directory: {}"+path+File.separator+DEFAULT_CONFIG_NAME);
						break;
					}
				}
			}
		}
		return f;
	}
	
	public Document getConfigurationDocument() throws Exception {
		if( this.doc == null ) {
			File f                     = this.lookupConfigFile();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db         = dbf.newDocumentBuilder();
			Document d                 = db.parse(f);
			this.doc                   = d;
		}
		return this.doc;
	}
	
	public void resetConfig() {
		this.doc             = null;
		this.authentications = null;
	}
	
	public Authorizer getPreAuthorizedSql(String docName) {
		if( authorizers == null ) {
			authorizers = new HashMap<>();
			try {
				Document doc   = getConfigurationDocument();
				NodeList nodes = doc.getElementsByTagName("authentication_provider");
			} catch( Exception e ) {
				log.error("Failed to build Authorizers: {}",e);
			}
		}
		return authorizers.get(docName);
	}
	
	public UserAuthentication getUserAuthentication( String providerName ) {
		if( authentications == null ) {
			authentications = new HashMap<>();
			try {
				Document doc   = getConfigurationDocument();
				NodeList nodes = doc.getElementsByTagName("authentication_provider");
				
				for(int i = 0; i < nodes.getLength(); i++) {
					Node apn                = nodes.item(i);
					NamedNodeMap attributes = apn.getAttributes();
					Node nameNode           = attributes.getNamedItem("name");
					Node classNode          = attributes.getNamedItem("class");
					String name             = nameNode.getNodeValue();
					String clazz            = classNode.getNodeValue();
					Class<?> pClass         = Class.forName(clazz);
					
					log.debug("Found authentication provider {} class {}",name,clazz);
					
					AuthenticationProvider ap   = (AuthenticationProvider) pClass.newInstance();
					UserAuthentication userAuth = ap.provideUserAuthentication(apn);
					authentications.put(name, userAuth);
				}
			} catch (Exception e) {
				log.error("Failed to build UserAuthentications: {}",e);
			}
		}
		return authentications.get( providerName );
	}
	
}
