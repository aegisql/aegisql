package com.aegisql.authorization;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.authorize.Authorize;
import net.sf.jsqlparser.statement.select.Select;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.access.AccessPattern;
import com.aegisql.access.AccessPatternsGenerator;
import com.aegisql.access.ColumnAccess;
import com.aegisql.access.ColumnQueryType;
import com.aegisql.access.Granted;
import com.aegisql.access.GrantedAccess;
import com.aegisql.access.TableAccesorID;
import com.aegisql.access.TableAccess;
import com.aegisql.access.TableQueryType;
import com.aegisql.authentication.Group;
import com.aegisql.authentication.UserAuthentication;
import com.aegisql.sql.QueryAnalizer;
import com.aegisql.sql.QueryEditor;

public final class SqlAuthorizer implements Authorizer {

	public final static Logger log = LoggerFactory.getLogger(SqlAuthorizer.class);

	private final Granted granted;
	private final UserAuthentication ua;
	private final String defaultSchema;
	private Set<String> allowedGroups = null;

	public SqlAuthorizer(Granted granted, UserAuthentication ua) {
		this(granted,ua,null);
	}
	
	public SqlAuthorizer(Granted granted, UserAuthentication ua, String defaultSchema) {
		this.granted        = granted;
		this.ua             = ua;
		this.defaultSchema  = defaultSchema;
	}

	
	/*
	 * Refactoring ideas:
	 * 1) Column and table names are found in the beginning - use this to build granted access objects
	 * before they used. This will allow to test them separately.
	 * */
	
	@Override
	public String buildAuthorizedQuery(String query, SubmittedBy statementSubmitter) throws SQLException {
		
		if( query == null ) {
			throw new SQLException("SQL query is null",new NullPointerException("Expected SQL String"));
		}
		if( "".equals( query ) ) {
			throw new SQLException("SQL query is empty");
		}

		log.debug("Original query: {}", query);
		String modifiedQuery = null;
		CCJSqlParser parser  = new CCJSqlParser(new StringReader(query));
		Statement statement  = null;
				
		try {
			statement = parser.Statement();
		} catch (ParseException e) {
			throw new AuthorizationException("SQL query rejected: Parsing error", e);
		}
		
		boolean authorize = false;
		
		if(statement instanceof Authorize) {
			statement = ((Authorize) statement).getInnerStatement();
			authorize = true;
		}

		QueryAnalizer queryAnalizer   = new QueryAnalizer( statement );
		
		SubmittedBy submittedBy = statement.getSubmittedBy();

		if (submittedBy == null) {
			if( statementSubmitter != null ) {
				submittedBy = statementSubmitter;
			} else if( statement instanceof Select ) {
				List<String> tables = queryAnalizer.getTableList();
				
				log.debug("Tables {}",tables);
				if( tables.size()==0 ) {
					if(authorize) {
						return "AUTHORIZED";						
					} else {
						return query;						
					}
				} else {
					throw new AuthorizationException("SQL query rejected: Missing submitter in query " + query );					
				}
			}
		}

		String userName        = submittedBy.getSubmittedBy();
		String password        = submittedBy.getIdentifiedBy();
		String device          = null;
		String hostName        = submittedBy.getHost();
		String managedUserName = submittedBy.getManagedUser();

		log.debug("User: '{}' pass: '{}' host: '{}' managed user '{}'", userName, password, hostName, managedUserName);
		List<Group> groups;
		try {
			groups = ua.getUserGroups(userName, password);
			if ((groups == null) || (groups.size() == 0))
				throw new SQLException("User " + submittedBy + " is not authorized. Must be a member of at least one group.");
			
			log.debug("User {}'s groups: {}", userName, groups);
			
			if( (allowedGroups != null ) && (allowedGroups.size()) > 0 ) {
				log.debug("Checking connection restrictions.");
				boolean pass = false;
				for( Group g: groups ) {
					if( allowedGroups.contains(g.getGroupName()) || "ADMIN".equalsIgnoreCase(g.getGroupName()) ) {
						pass = true;
						break;
					}
				}
				if( ! pass ) {
					throw new AuthorizationException("Connection requires membership in one of those groups: " + allowedGroups);
				}
			} else {
				log.debug("No connection restrictions on groups found");
			}
		} catch (SQLException e) {
			throw new AuthorizationException(e);
		}

		Set<AccessPattern> accessPatternsSet = AccessPatternsGenerator.buildAllAccessors(userName, device, hostName, groups);
		
		log.debug("Generated {} access patterns: {}", accessPatternsSet.size(),accessPatternsSet);
		log.debug("Found Tables: {}",queryAnalizer.getAllTables());
		log.debug("Reversed Aliases: {}",queryAnalizer.getReversedTableAliase());
		log.debug("Found Columns: " + queryAnalizer.getAllColumns());
		
		modifiedQuery = modifyQuery(query, queryAnalizer, accessPatternsSet);

		log.debug("Final query: {}", modifiedQuery);

		if(authorize) {
			return "AUTHORIZED";
		} else {
			return modifiedQuery;			
		}
		
	}

	public String modifyQuery(String queryOrig, QueryAnalizer queryAnalizer, Set<AccessPattern> accessPatterns) throws AuthorizationException {

		String query = queryOrig;
		TableQueryType mainQueryType = TableQueryType.valueOf(queryAnalizer.getStatement());

		TreeMap<Integer, String> sortedMap = new TreeMap<Integer, String>();
		if (queryAnalizer.hasStar()) {
			for (AccessPattern accessPattern : accessPatterns) {
				try {
					log.debug("Try greedy pattern {}",accessPattern);
					CCJSqlParser parser  = new CCJSqlParser(new StringReader(query));
					Statement st = parser.Statement();
					Map<String, List<String>> granted = buildTableColumnAccess(st, queryAnalizer, accessPattern, mainQueryType);
					query = st.toAegisString();
					log.debug("Granted Tables: " + granted);
					Integer replaced = forceReplaceStar(st, granted);
					sortedMap.put(replaced, st.toString());
				} catch (AuthorizationException e) {
					log.debug("Pattern rejected: {}: {}", accessPattern, e.getMessage());
				} catch (ParseException e) {
					throw new AuthorizationException("Unexpected exception: ",e);
				}
			}
		} else {
			boolean oneGranted = false;
			for (AccessPattern accessPattern : accessPatterns) {
				try {
					log.debug("Try pattern {}",accessPattern);
					Map<String, List<String>> granted = buildTableColumnAccess(queryAnalizer.getStatement(), queryAnalizer, accessPattern, mainQueryType);
					log.debug("Granted Tables: " + granted);
					oneGranted = true;
					break; // first found already works
				} catch (AuthorizationException e) {
					log.debug("rejected: {}: {}", accessPattern, e.getMessage());
				}
			}
			if( oneGranted) {
				sortedMap.put(0, queryAnalizer.getStatement().toString());
			}
		}
		if(sortedMap.size() == 0 ) {
			throw new AuthorizationException("Statement not granted: " + queryAnalizer.getStatement().toAegisString() );
		}
		String resultingStatement = sortedMap.lastEntry().getValue();
		return resultingStatement;
	}

	private int forceReplaceStar(Statement statement, Map<String, List<String>> granted) {
		if (statement instanceof Select) {
			QueryEditor rc = new QueryEditor( TableQueryType.SELECT );
			return rc.selectReplaceStar((Select) statement, granted);
		} else {
			throw new RuntimeException("Unimplemented * conversion feature for statement " + statement.toString());
		}
	}

	public Map<String, List<String>> buildTableColumnAccess(Statement statement, QueryAnalizer queryAnalizer, AccessPattern accessPattern, TableQueryType mainQueryType) throws AuthorizationException {
		Map<String, List<String>> granted = new HashMap<String, List<String>>();
		
		Map<String, Set<String>> columns = queryAnalizer.getAllColumns();
		List<String> tables              = queryAnalizer.getAllTables();
		Map<String,String> aliases       = queryAnalizer.getReversedTableAliase();

		//Keep working on supporting really complex queries
		Set<TableAccesorID> ids          = new HashSet<>();

		for (String table : tables) {
			Set<String> col = columns.get(table);
			String alias = aliases.get(table);
			try {
				TableQueryType queryType         = queryAnalizer.getQueryType(table);
				log.debug("Query type for table {} is {} ", table, queryType);
				granted.put(table, buildColumnAccess(statement, queryType, mainQueryType, table, alias, col, accessPattern, ids));
			}catch(Exception e){
				throw e;
			}
		}
		log.debug("Built accessors: {}", ids);
		return granted;
	}

	public List<String> buildColumnAccess(Statement statement, TableQueryType queryType, TableQueryType mainQueryType, String table, String alias,
			Set<String> col, AccessPattern accessPattern, Set<TableAccesorID> ids) throws AuthorizationException {
		log.debug("testing table {}",table);
		GrantedAccess ga;
		List<String> result = new ArrayList<String>();
		try {
			ga = granted.getGrantedAccess(table, accessPattern);
			if(ga == null) {
				throw new AuthorizationException("Table Access not granted");
			}
			TableAccess ta = ga.getTableAccess();
			if (!ta.isGranted(queryType)) {
				String err = "access type " + queryType + " for '" + table + "' for pattern " + accessPattern;
				throw new AuthorizationException( err );
			}
			if(TableQueryType.DELETE == queryType) {
				//Table is already granted and there are no DELETE command for columns (cannot delete column, only rows)
				if(accessPattern.getAccessorId() != null) {
					QueryEditor ed = new QueryEditor( mainQueryType );
					TableAccesorID id = new TableAccesorID(defaultSchema, table, alias, accessPattern.getAccessorField(), accessPattern.getAccessorId());
					ids.add(id);
					ed.whereAddAccessor(statement, id, queryType);
					log.debug("=== in Delete Fix it when you ready: Accessor:{}={} statement: {}",accessPattern.getAccessorField(), accessPattern.getAccessorId(),statement);
				} else {
					log.error("This is not implemented and just wrong in DELETE: " +accessPattern);
				}

				List<String> deleteGranted = new ArrayList<String>();
				return deleteGranted;
			}
			List<ColumnAccess> grantedColumns = ga.getGrantedColumns(ColumnQueryType.valueOf(queryType));
			log.debug("GRANTED {} {} COLUMNS {}" ,queryType,ga, grantedColumns);
			Set<String> upColumns = new HashSet<String>();
			for (String c : col) {
				if (!c.contains("*")) {
					String[] colFields = c.split("\\.");
					String colName = colFields[ colFields.length - 1 ];
					upColumns.add(colName.toUpperCase());
				}
			}
			for (ColumnAccess c : grantedColumns) {
				String columnName = c.getColName();
				if( columnName.equalsIgnoreCase(accessPattern.getAccessorField() )) {
					log.debug("===== Accessor field found");
					continue;
				}
				upColumns.remove(columnName);
				result.add(columnName);
			}
			if (upColumns.size() > 0) {
				throw new AuthorizationException("UnAuthtorized access to columns: " + upColumns);
			}

		} catch (SQLException e) {
			String err = "Not granted " + e.getMessage();
			throw new AuthorizationException( err );
		}
		if(accessPattern.getAccessorId() != null) {
			QueryEditor ed = new QueryEditor( mainQueryType );
			TableAccesorID id = new TableAccesorID(defaultSchema, table, alias, accessPattern.getAccessorField(), accessPattern.getAccessorId());
			ids.add(id);
			ed.whereAddAccessor(statement, id, queryType);
			log.debug("=== Fix it when you ready: Accessor:{}={} statement: {}",accessPattern.getAccessorField(), accessPattern.getAccessorId(),statement);
		} else {
			log.error("This is not implemented and just wrong: " +accessPattern);
		}
		return result;
	}

	/*
	 * This can be set only once to prevent any manipulation.
	 * and usually as a DataSource property.
	 * */
	@Override
	public void setAllowedGroups(Set<String> allowedGroups) {
		if( this.allowedGroups == null ){
			this.allowedGroups = allowedGroups;
		}
	}

}
