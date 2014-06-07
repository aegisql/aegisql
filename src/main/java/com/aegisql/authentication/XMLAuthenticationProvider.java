package com.aegisql.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLAuthenticationProvider implements AuthenticationProvider {

	class GroupStub {
		
		public final int id;
		public final String name;
		public final String accessor;
		
		public GroupStub(int id, String name, String accessor) {
			this.id       = id;
			this.name     = name;
			this.accessor = accessor;
		}
	}

	class UserStub {
		
		public final String name;
		public final String password;
		
		public UserStub(String name, String password) {
			this.name     = name;
			this.password = password;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result
					+ ((password == null) ? 0 : password.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserStub other = (UserStub) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (password == null) {
				if (other.password != null)
					return false;
			} else if (!password.equals(other.password))
				return false;
			return true;
		}

		private XMLAuthenticationProvider getOuterType() {
			return XMLAuthenticationProvider.this;
		}
	}

	
	class UserGroupStub {
		
		public final Integer accessor_id;
		public final String name_ref;
		public final boolean def;
		
		public UserGroupStub(String name_ref, Integer accessor_id, boolean def) {
			this.name_ref    = name_ref;
			this.accessor_id = accessor_id;
			this.def         = def;
		}
	}

	private Map<String,GroupStub> groupStubs = new HashMap<>();
	private Map<UserStub,List<UserGroupStub>> userStubs = new HashMap<>();
	
	public final org.slf4j.Logger log = LoggerFactory.getLogger(XMLAuthenticationProvider.class);
	
	public XMLAuthenticationProvider() {
		
	}
	
	@Override
	public UserAuthentication provideUserAuthentication(Node authenticationProviderNode) {
		UserAuthenticationXML ua = new UserAuthenticationXML();
		
		NodeList nodes          = authenticationProviderNode.getChildNodes();
		for(int i = 0; i<nodes.getLength(); i++ ) {
			Node node = nodes.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if( "group".equals(node.getNodeName())) {
				buildGroup(node);
			}
			if( "user".equals(node.getNodeName())) {
				buildUser(node);
			}
		}
		
		assembleGroups(ua);
		
		return ua;
	}

	private void assembleGroups(UserAuthenticationXML ua) {
		log.debug("Assembling groups");

		for(UserStub user:userStubs.keySet()) {
			log.debug("Assembling user {}",user.name);
			List<Group> groups = new ArrayList<>();
			List<UserGroupStub> ugStubs = userStubs.get(user);
			for( UserGroupStub ug : ugStubs ) {
				log.debug("Assembling user group {}",ug.name_ref);
				GroupStub gs = groupStubs.get(ug.name_ref);
				if(gs == null) {
					continue;
				}
				
				Group g = new Group(gs.id, gs.name, ug.def, gs.accessor, ug.accessor_id);
				groups.add(g);
			}
			ua.addGroup(user.name, user.password, groups);
		}
		
	}

	/*
	 * 		<user name="nikita" password="12345">
				<user_group ref_name="USER" default="true" accessor_id="1002"/>
		    </user>
	 * */
	private void buildUser(Node node) {
		log.debug("Building user");
		NamedNodeMap attributes  = node.getAttributes();
		Node nameNode            = attributes.getNamedItem("name");
		String name              = nameNode.getNodeValue();
		
		Node passwordNode        = attributes.getNamedItem("password");
		String password          = passwordNode.getNodeValue();

		log.debug("user NAME={} PASSWORD={}",name,password);

		UserStub user = new UserStub(name, password);
		
		userStubs.put(user, new ArrayList<UserGroupStub>());
		
		NodeList ugNodes = node.getChildNodes();
		
		for(int i = 0; i<ugNodes.getLength();i++) {
			Node next = ugNodes.item(i);
			if( ! "user_group".equals(next.getNodeName())) {
				continue;
			}
			NamedNodeMap ugAttr      = next.getAttributes();
			Node refNode             = ugAttr.getNamedItem("ref_name");
			String refName           = refNode.getNodeValue();

			Node defaultNode         = ugAttr.getNamedItem("default");
			String def               = "false";
			if(defaultNode != null ) {
				def = defaultNode.getNodeValue();
			}
			
			Node accessorNode        = ugAttr.getNamedItem("accessor_id");
			String accessorId        = null;
			if(accessorNode != null) {
				accessorId = accessorNode.getNodeValue();
			}
			log.debug("user grup REF={} DEFAULT={} ACCESSOR_ID={}",refName,def,accessorId);
			Integer id = null;
			if(accessorId != null && ! "".equals(accessorId)) {
				id = Integer.parseInt(accessorId);
			}
			boolean d = Boolean.parseBoolean(def);
			List<UserGroupStub> userGroups = userStubs.get(user);
			userGroups.add(new UserGroupStub(refName, id, d));
		}
	}

	private void buildGroup(Node node) {
		NamedNodeMap attributes  = node.getAttributes();
		Node idNode              = attributes.getNamedItem("id");
		String idCode            = idNode.getNodeValue();

		Node nameNode            = attributes.getNamedItem("name");
		String name              = nameNode.getNodeValue();
		
		Node accessorNode        = attributes.getNamedItem("accessor");
		String accessor          = null;
		if(accessorNode != null) {
			accessor = accessorNode.getNodeValue();
		}
		
		int id = Integer.parseInt(idCode);

		groupStubs.put(name, new GroupStub(id, name, accessor));
		
		log.debug("group ID={} NAME={} ACCESSOR={}",idCode,name,accessor);
		
	}

}
