package com.aegisql.access;

import java.util.HashSet;
import java.util.Set;

public class ColumnAccess {

	private final String colName;
	private final Set<ColumnQueryType> accessSet = new HashSet<ColumnQueryType>();
	
	public ColumnAccess( String colName, ColumnQueryType... access ) {
		this.colName = colName;
		for(ColumnQueryType e:access) {
			accessSet.add(e);
		}
	}

	public ColumnAccess( String colName, String... access ) {
		this.colName = colName;
		for(String e:access) {
			accessSet.add( ColumnQueryType.valueOf(e));
		}
	}

	public String getColName() {
		return colName;
	}

	public String getAccessSet() {
		StringBuffer sb = new StringBuffer();
		for(ColumnQueryType e:accessSet) {
			sb.append(e).append(",");
		}
		if(sb.length()>0) sb.deleteCharAt(sb.lastIndexOf(","));
		return sb.toString();
	}

	public boolean isGranted(ColumnQueryType type) {
		if(ColumnQueryType.BLOCKED.equals(type)) {
			return false;
		} else {
			return accessSet.contains(type);
		}
	}

	@Override
	public String toString() {
		return colName + ": " + accessSet;
	}
		
}
