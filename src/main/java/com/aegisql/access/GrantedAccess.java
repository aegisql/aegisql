package com.aegisql.access;

import java.util.ArrayList;
import java.util.List;

public class GrantedAccess {

	private final String             schema;
	private final String             tableName;
	private final String             accessorName;
	private final AccessPattern      accessPattern;
	private TableAccess              tableAccess;
	private final List<ColumnAccess> columns = new ArrayList<ColumnAccess>();
	
	public GrantedAccess(String schema, String tableName, String accessorName, AccessPattern ap) {	
		this.schema        = schema;
		this.tableName     = tableName;
		this.accessorName  = accessorName;
		this.accessPattern = ap;
	}
	
	public TableAccess getTableAccess() {
		return tableAccess;
	}

	public void setTableAccess(TableAccess table) {
		this.tableAccess = table;
	}

	public String getSchema() {
		return schema;
	}

	public String getTableName() {
		return tableName;
	}

	public List<ColumnAccess> getColumns() {
		return columns;
	}

	public String getAccessorField() {
		return accessorName;
	}
	
	public void addColumnAccess(ColumnAccess col) {
		columns.add(col);
	}

	@Override
	public String toString() {
		return "GrantedAccess [" + schema + "." + tableName
				+ " tableAccess=" + tableAccess
				+ " columns=" + columns
				+ " accessPattern=" + accessPattern + "]";
	}
	
	public boolean isTableAccessGranted( TableQueryType type ) {
		return tableAccess.isGranted(type);
	}

	public List<ColumnAccess> getGrantedColumns( ColumnQueryType type ) {
		List<ColumnAccess> granted = new ArrayList<ColumnAccess>();
		for( ColumnAccess col : columns ) {
			if(col.isGranted(type)) granted.add(col);
		}
		return granted;
	}

	public AccessPattern getAccessPattern() {
		return accessPattern;
	}
	
}
