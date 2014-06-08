package com.aegisql.authentication;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DatabaseAuthenticationProvider implements AuthenticationProvider {

	public final org.slf4j.Logger log = LoggerFactory.getLogger(DatabaseAuthenticationProvider.class);
	
	public DatabaseAuthenticationProvider() {
		
	}
	
	@Override
	public UserAuthentication provideUserAuthentication(Node authenticationProviderNode) throws Exception {
		UserAuthenticationDB ua = new UserAuthenticationDB();
		NodeList nodes          = authenticationProviderNode.getChildNodes();
		for(int i = 0; i<nodes.getLength(); i++ ) {
			Node node = nodes.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if( "data_source".equals(node.getNodeName())) {
				setDataSource(ua, node);
			}
			if("select_users_groups_sql".equals(node.getNodeName())) {
				setSql(ua, node);
			}
		}
		return ua;
	}

	private void setDataSource(UserAuthenticationDB ua, Node dataSourceNode ) throws Exception {
		log.debug("Set Authentication DataSource");
		NamedNodeMap attributes  = dataSourceNode.getAttributes();
		Node classNode           = attributes.getNamedItem("class");
		String className         = classNode.getNodeValue();
		log.debug("DataSource class: {}",className);
		Class<?> datasourceClass = Class.forName(className);
		Method[] methods         = datasourceClass.getMethods();
		DataSource dataSource    = (DataSource) datasourceClass.newInstance();
		NodeList propertyNodes   = dataSourceNode.getChildNodes();
		for(int i = 0; i < propertyNodes.getLength(); i++) {
			Node propertyNode      = propertyNodes.item(i);
			NamedNodeMap propAttr  = propertyNode.getAttributes();
			if(propAttr == null) continue;
			if(propAttr.getLength() < 2 ) continue;
			String setter = null;
			String value  = null;
			for(int ii=0; ii < propAttr.getLength(); ii++) {
				Node paNode        = propAttr.item(ii);
				String paNodeName  = paNode.getNodeName();
				String paNodeValue = paNode.getNodeValue();
				if(paNodeName.equals("name")) {
					setter = getSetter(paNodeValue);
				}
				if(paNodeName.equals("value")) {
					value = paNodeValue;
				}
			}
			if(setter == null) {
				continue;
			}
			for(Method m:methods) {
				if(m.getName().equalsIgnoreCase(setter) && (m.getParameterCount()==1)) {
					Parameter p = m.getParameters()[0];
					log.debug("Matching DataSource Setter found: {}({}). Trying to invoke with value '{}'",m.getName(),p,value);
					Class<?> type = p.getType();
					if(type.equals(String.class)) {
						m.invoke(dataSource, value);
					} else if(Boolean.TYPE.equals(type)) {
						m.invoke(dataSource, Boolean.parseBoolean(value));
					} else if(Integer.TYPE.equals(type)) {
						m.invoke(dataSource, Integer.parseInt(value));
					} else if(Long.TYPE.equals(type)) {
						m.invoke(dataSource, Long.parseLong(value));
					} else if(Double.TYPE.equals(type)) {
						m.invoke(dataSource, Double.parseDouble(value));
					} else if(Float.TYPE.equals(type)) {
						m.invoke(dataSource, Float.parseFloat(value));
					} else {
						throw new Exception("Unsupported type "+type+" in node "+propertyNode+"; provided value "+value);
					}
					break;
				}
			}

		}
		ua.setConnection(dataSource.getConnection(), "");
	}

	private void setSql(UserAuthenticationDB ua, Node sqlNode ) {
		log.debug("Set Authentication SQL");
		NodeList list = sqlNode.getChildNodes();
		
		for(int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			if(Node.CDATA_SECTION_NODE == n.getNodeType() ) {
				log.debug("Found SQL: "+n.getNodeValue());
				ua.setSelectUsersGroupsSql(n.getNodeValue());
				break;
			}
		}
	}

	private String getSetter( String s ) {
		if(s==null || "".equals(s) ) {
			return s;
		} else {
			return "set"+s.substring(0,1).toUpperCase()+s.substring(1);
		}
	}
}
