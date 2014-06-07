package com.aegisql.access;

import java.util.HashSet;
import java.util.Set;

public class TableAccess {

	private final String tableName;
	private final Set<TableQueryType> accessSet = new HashSet<TableQueryType>();
	
	public TableAccess( String tableName, TableQueryType... access ) {
		this.tableName = tableName;
		for(TableQueryType e:access) {
			accessSet.add(e);
		}
	}

	public TableAccess( String tableName, String... access ) {
		this.tableName = tableName;
		for(String e:access) {
			accessSet.add(TableQueryType.valueOf(e));
		}
	}

	public String getTableName() {
		return tableName;
	}

	public String getAccessSet() {
		StringBuffer sb = new StringBuffer();
		for(TableQueryType e:accessSet) {
			sb.append(e).append(",");
		}
		if(sb.length()>0) sb.deleteCharAt(sb.lastIndexOf(","));
		return sb.toString();
	}
	
	public boolean isGranted(TableQueryType type) {
		if(TableQueryType.BLOCKED.equals(type)) {
			return false;
		}
		else {
			return accessSet.contains(type);
		}
	}
	
	@Override
	public String toString() {
		return tableName + ": " + accessSet;
	}

}
