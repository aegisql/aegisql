package com.aegisql.testing_tools;

import java.io.FileReader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.access.AccessPattern;
import com.aegisql.access.ColumnAccess;
import com.aegisql.access.Granted;
import com.aegisql.access.GrantedAccess;
import com.aegisql.access.TableAccess;
import com.aegisql.authentication.Group;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.authorization.AuthorizationException;
import com.aegisql.authorization.SqlAuthorizer;
import com.esotericsoftware.yamlbeans.YamlReader;

public class YamlAuthorizationTestProvider {

	public final Logger log = LoggerFactory.getLogger(YamlAuthorizationTestProvider.class);
			
	private final Map<String,Map<String,?>> tests = new HashMap<String, Map<String, ?>>();
	private final String fileName;
	
	public YamlAuthorizationTestProvider( String path ) throws Exception {
		this.fileName = path;
		YamlReader yr = new YamlReader(new FileReader(path));
		while(true) {
			@SuppressWarnings("unchecked")
			Map<String,?> doc = (Map<String, ?>) yr.read();
			if(doc == null) break;
			if( ! doc.containsKey("TEST")) {
				log.warn("Ignore document without TEST name.");
			} else {
				log.debug("Found document with test name {}",doc.get("TEST"));
				tests.put((String) doc.get("TEST"), doc);
			}
		}
	}

	public boolean hasTest( String testName ) {
		return tests.containsKey(testName);
	}

	public String getDescription( String testName ) {
		if( ! hasTest(testName)) {
			throw new RuntimeException("Test '" + testName + "' not found in " + fileName);
		} else {
			return (String) tests.get(testName).get("DESCRIPTION");
		}
	}

	
	public String getSQL( String testName ) {
		if( ! hasTest(testName)) {
			throw new RuntimeException("Test '" + testName + "' not found in " + fileName);
		} else {
			return (String) tests.get(testName).get("SQL");
		}
	}

	public String getExpected( String testName ) {
		if( ! hasTest(testName)) {
			throw new RuntimeException("Test '" + testName + "' not found in " + fileName);
		} else {
			return (String) tests.get(testName).get("EXPECTED");
		}
	}

	public String getUser( String testName ) {
		if( ! hasTest(testName)) {
			throw new RuntimeException("Test '" + testName + "' not found in " + fileName);
		} else {
			return (String) tests.get(testName).get("USER");
		}
	}

	public String getManagedUser( String testName ) {
		if( ! hasTest(testName)) {
			throw new RuntimeException("Test '" + testName + "' not found in " + fileName);
		} else {
			return (String) tests.get(testName).get("MANAGED_USER");
		}
	}
	
	public String getPassword( String testName ) {
		if( ! hasTest(testName)) {
			throw new RuntimeException("Test '" + testName + "' not found in " + fileName);
		} else {
			return (String) tests.get(testName).get("PASSWORD");
		}
	}

	public String getHost( String testName ) {
		if( ! hasTest(testName)) {
			throw new RuntimeException("Test '" + testName + "' not found in " + fileName);
		} else {
			return (String) tests.get(testName).get("HOST");
		}
	}

	public SqlAuthorizer getSqlAuthorizer( String testName ) {
		UserAuthentication ua = getUserAuthentication(testName);
		Granted g             = getGranted(testName);
		String defaultSchema  = (String) tests.get(testName).get("DEFAULT_SCHEMA");
		SqlAuthorizer sa;
		if( isEmpty(defaultSchema) ) {
			sa = new SqlAuthorizer(g, ua);
		} else {
			sa = new SqlAuthorizer(g, ua, defaultSchema);
		}
		return sa;
	}
	
	public SubmittedBy getSubmittedBy( String testName ) {
		SubmittedBy sb = new SubmittedBy();
		String user = this.getUser(testName);
		if(isEmpty(user)) {
			try {
				Statement st = getStatement(testName);
				return st.getSubmittedBy();
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
		sb.setSubmittedBy(user);
		sb.setIdentifiedBy(this.getPassword(testName));
		sb.setManagedUser(this.getManagedUser(testName));
		sb.setHost(this.getHost(testName));
		return sb;
	}
	
	public Statement getStatement(String testName) throws ParseException {
		CCJSqlParser parser  = new CCJSqlParser(new StringReader(getSQL(testName)));
		Statement statement  = null;
		statement = parser.Statement();
		return statement;
	}

	public Statement getExpectedStatement(String testName) throws ParseException {
		String sql = getExpected(testName);
		
		if(isEmpty(sql)) {
			return null;
		}
		
		CCJSqlParser parser  = new CCJSqlParser(new StringReader( sql ));
		Statement statement  = null;
		statement = parser.Statement();
		return statement;
	}

	public UserAuthentication getUserAuthentication( final String testName ) {
		if( ! hasTest(testName)) {
			throw new RuntimeException("Test '" + testName + "' not found in " + fileName);
		}


		UserAuthentication ua = new UserAuthentication() {

			SubmittedBy sb = getSubmittedBy(testName);
			
			String username    = sb != null ? sb.getSubmittedBy(): "";
			String password    = sb != null ? sb.getIdentifiedBy():"";
			Map<String, ?> doc = tests.get(testName);
			
			ArrayList<Map<String,String>> groups = (ArrayList<Map<String, String>>) doc.get("GROUPS");
			
			@Override
			public List<Group> getUserGroups(String userName, String password) throws SQLException {
				
				if( ! this.username.equals(userName) ) {
					throw new AuthorizationException("Username does not match: expected '" + this.username + "' tested '" + userName + "'");
				}
				if( ! this.password.equals(password) ) {
					throw new AuthorizationException("Password does not match: expected '" + this.password + "' tested '" + password + "'");
				}
				
				List<Group> gl = new ArrayList<>();
				long id = 0;
				if(groups != null)
				for(Map<String,String> m : groups ) {
					id++;
					String groupName     = m.get("NAME");
					String useAccessor   = m.get("ACCESSOR");
					String accessotIdStr = m.get("ACCESSOR_ID");
					String isDefaultStr  = m.get("IS_DEFAULT");
					
					if( isEmpty(groupName) ) {
						throw new RuntimeException("Group name is a mandatory parameter for User Authentication. Test name: "+testName );
					}
					
					boolean isDefault = false;
					if( ! isEmpty(isDefaultStr) ) {
						isDefault = Boolean.parseBoolean(isDefaultStr);
					}
					
					Integer accessorId = null;
					if( ! isEmpty(accessotIdStr) ) {
						accessorId = new Integer(accessotIdStr);
					}
					Group g = new Group(id, groupName, isDefault, useAccessor, accessorId);
					
					if(isDefault && (gl.size() > 0) ) {
						Group g0 = gl.get(0);
						gl.set(0, g);
						g = g0;
					}
					
					gl.add(g);
				}
				
				return gl;
			}

			@Override
			public List<Group> getUserGroups(String userName, String password,
					String managedUser) throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
		return ua;
	}

	public Granted getGranted( final String testName ) {
		if( ! hasTest(testName)) {
			throw new RuntimeException("Test '" + testName + "' not found in " + fileName);
		}
		
		Map<String, ?> doc = tests.get(testName);
		final Map<String,Map<String,Map<AccessPattern,GrantedAccess>>> data = new HashMap<String, Map<String, Map<AccessPattern, GrantedAccess>>>();
		
		ArrayList<Map<String,?>> granted = (ArrayList<Map<String, ?>>) doc.get("GRANTED");
		if(granted != null)
		for(Map<String,?> gr:granted) {
			String schema = (String) gr.get("SCHEMA");
			if("".equals(schema)) schema = null;
			if( ! data.containsKey(schema) ) {
				data.put(schema, new HashMap<String, Map<AccessPattern, GrantedAccess>>());
			}
			Map<String, Map<AccessPattern, GrantedAccess>> tblData = data.get(schema);
			String table = (String) gr.get("TABLE");
			if( ! tblData.containsKey(table) ) {
				tblData.put(table, new HashMap<AccessPattern, GrantedAccess>());
			}
			Map<AccessPattern, GrantedAccess> accData = tblData.get(table);
			String groupName  = (String) gr.get("GROUP");
			String userName   = (String) gr.get("USER");
			String hostName   = (String) gr.get("HOST");
			String deviceName = (String) gr.get("DEVICE");
			String accessor   = (String) gr.get("ACCESSOR");

			AccessPattern ap  = new AccessPattern(groupName, userName, hostName, deviceName, accessor, null);
			GrantedAccess ga  = new GrantedAccess(schema, table, accessor, ap);
			
			ArrayList<String> tableAccess = (ArrayList<String>) gr.get("TABLE_ACCESS");
			String[] taArray = tableAccess.toArray(new String[]{});
			TableAccess ta = new TableAccess(table, taArray);
			ga.setTableAccess(ta);
			
			ArrayList<Map<String,?>> colAccess = (ArrayList<Map<String, ?>>) gr.get("COLUMN_ACCESS");
			if(colAccess != null)
			for(Map<String,?> ca:colAccess) {
				String colName = (String) ca.get("COLUMN");
				ArrayList<String> colAccessList = (ArrayList<String>) ca.get("ACCESS");
				String[] caArray = colAccessList.toArray(new String[]{});
				ga.addColumnAccess(new ColumnAccess(colName, caArray));
			}
			
			accData.put(ap, ga);
		}
		
		Granted g = new Granted() {

			Map<String,Map<String,Map<AccessPattern,GrantedAccess>>> accessData = data;
			
			@Override
			public GrantedAccess getGrantedAccess(String schema, String table, AccessPattern accessor) throws SQLException {
				AccessPattern a = new AccessPattern(accessor.getGroupName(),
													accessor.getUserName(),
													accessor.getHostName(),
													accessor.getDeviceName(),
													accessor.getAccessorField(),
													null);
				return accessData.get(schema).get(table).get(a);
			}

			@Override
			public GrantedAccess getGrantedAccess(String table, AccessPattern accessor) throws SQLException {
				AccessPattern a = new AccessPattern(accessor.getGroupName(),
						accessor.getUserName(),
						accessor.getHostName(),
						accessor.getDeviceName(),
						accessor.getAccessorField(),
						null);
				return accessData.get(null).get(table).get(a);
			}
			
		};
		
		return g;
	}
	
	private boolean isEmpty( String s ) {
		return (s==null) || "".equals(s);
	}
	
}
