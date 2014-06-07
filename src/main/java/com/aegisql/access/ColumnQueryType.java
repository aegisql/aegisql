package com.aegisql.access;

public enum ColumnQueryType {
	INSERT,SELECT,UPDATE,REPLACE,BLOCKED,VALIDATE;

	public static ColumnQueryType valueOf( TableQueryType statement ) {
		switch( statement ) {
		case INSERT:
			return INSERT;
		case SELECT:
			return SELECT;
		case UPDATE:
			return UPDATE;
		case REPLACE:
			return REPLACE;
		default:
			return BLOCKED;
		}
		
	}

}
