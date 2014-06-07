package com.aegisql.testing_tools;

import com.aegisql.access.ColumnAccess;

public class AccessSchema {

	public static String[] INSERT_SELECT_UPDATE_DELETE = {"INSERT","SELECT","UPDATE","DELETE"};
	public static String[] INSERT_SELECT_UPDATE = {"INSERT","SELECT","UPDATE"};
	public static String[] INSERT = {"INSERT"};
	public static String[] INSERT_SELECT = {"INSERT","SELECT"};
	public static String[] INSERT_UPDATE = {"INSERT","UPDATE"};
	public static String[] SELECT = {"SELECT"};
	public static String[] SELECT_UPDATE = {"SELECT","UPDATE"};
	public static String[] UPDATE = {"UPDATE"};
	
	public static ColumnAccess getInsertSelectUpdateColumn( String columnName ) {
		return new ColumnAccess(columnName, INSERT_SELECT_UPDATE);
	}

	public static ColumnAccess getInsertSelectColumn( String columnName ) {
		return new ColumnAccess(columnName, INSERT_SELECT);
	}

	public static ColumnAccess getInsertUpdateColumn( String columnName ) {
		return new ColumnAccess(columnName, INSERT_UPDATE);
	}

	public static ColumnAccess getInsertColumn( String columnName ) {
		return new ColumnAccess(columnName, INSERT);
	}

	public static ColumnAccess getSelectColumn( String columnName ) {
		return new ColumnAccess(columnName, SELECT);
	}

	public static ColumnAccess getSelectUpdateColumn( String columnName ) {
		return new ColumnAccess(columnName, SELECT_UPDATE);
	}

	public static ColumnAccess getUpdateColumn( String columnName ) {
		return new ColumnAccess(columnName, UPDATE);
	}
	
}
