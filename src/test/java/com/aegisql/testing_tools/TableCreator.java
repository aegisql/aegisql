package com.aegisql.testing_tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TableCreator {
	
	SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	StringBuilder sb = new StringBuilder();
	
	boolean started = false;
	boolean ended   = false;
	String mark = "`";
	
	String dbMode = "MYSQL";
	
	String DATETIME = "DATETIME"; //mySQL
	String AUTO_INCREMENT = "AUTO_INCREMENT"; //mySQL
	
	String schemaName;
	String tableName;
	String fullName;
	
	public static TableCreator mysqlTableCreator( String schemaName, String tableName ) {
		TableCreator tc = new TableCreator( schemaName, tableName, "`" );
		return tc;
	}

	public static TableCreator pgTableCreator( String schemaName, String tableName ) {
		TableCreator tc = new TableCreator( schemaName, tableName, "" );
		tc.DATETIME       = "TIMESTAMP";
		tc.AUTO_INCREMENT = "SERIAL";
		tc.dbMode = "PG";
		return tc;
	}

	private TableCreator( String schemaName, String tableName ) {
		this.schemaName = mark+schemaName.replace(mark, "")+mark;
		this.tableName = mark+tableName.replace(mark, "")+mark;
		this.fullName = this.schemaName + "." + this.tableName;
	}

	private TableCreator( String schemaName, String tableName, String mark ) {
		this.schemaName = mark+schemaName.replace(mark, "")+mark;
		this.tableName = mark+tableName.replace(mark, "")+mark;
		this.fullName = this.schemaName + "." + this.tableName;
		this.mark = mark;
	}

	
	public TableCreator begin() {
		if( ! started ) {
			started = true;
			sb.append("CREATE TABLE IF NOT EXISTS ").append(fullName).append(" (\n");
		}
		return this;
	}

	public TableCreator end() {
		if( started && ! ended ) {
			ended = true;
			sb.append(")\n");
		}
		return this;
	}

	public TableCreator ID() {
		if( started && ! ended ) {
			
			switch(dbMode) {
				case "MYSQL":
					sb.append(mark+"ID"+mark+" INT NOT NULL ").append(AUTO_INCREMENT).append(",\n");
					break;
				case "PG":
					sb.append(mark+"ID"+mark+" SERIAL NOT NULL ").append(",\n");
				default:
			}
			
		}
		return this;
	}

	public TableCreator ID_NO_AUTO_INCR() {
		if( started && ! ended ) {
			sb.append(mark+"ID"+mark+" INT NOT NULL,\n");
		}
		return this;
	}
	
	public TableCreator INT( String colName, boolean notNull, Integer defaultValue) {
		if( started && ! ended ) {
			sb.append(mark).append(colName.replace(mark, "")).append(mark);
			sb.append(" INT");
			if(notNull) sb.append(" NOT NULL");
			if(defaultValue != null) sb.append(" DEFAULT ").append(defaultValue);
			sb.append(",\n");
		}
		return this;
	}

	public TableCreator VARCHAR( String colName, int length, boolean notNull, String defaultValue) {
		if( started && ! ended ) {
			sb.append(mark).append(colName.replace(mark, "")).append(mark);
			sb.append(" VARCHAR(").append(length).append(")");
			if(notNull) sb.append(" NOT NULL");
			if(defaultValue != null) sb.append(" DEFAULT '").append(defaultValue).append("'");
			sb.append(",\n");
		}
		return this;
	}

	public TableCreator DATETIME( String colName, boolean notNull, String defaultValue, String onUpdate) {
		if( started && ! ended ) {
			sb.append(mark).append(colName.replace(mark, "")).append(mark);
			sb.append(" ").append(DATETIME).append(" ");
			if(notNull) sb.append(" NOT NULL");
			if(defaultValue != null) {
				try {
					datetimeFormat.parse(defaultValue);
					defaultValue = "'"+defaultValue+"'";
				} catch (ParseException e) {
				}
				sb.append(" DEFAULT ").append(defaultValue).append("");
			}
			if(onUpdate != null) {
				try {
					datetimeFormat.parse(onUpdate);
					onUpdate = "'"+onUpdate+"'";
				} catch (ParseException e) {
				}

				sb.append(" ON UPDATE ").append(onUpdate).append("");
			}
			sb.append(",\n");
		}
		return this;
	}

	public TableCreator TIMESTAMP( String colName, boolean notNull, String defaultValue, String onUpdate) {
		if( started && ! ended ) {
			sb.append(mark).append(colName.replace(mark, "")).append(mark);
			sb.append(" TIMESTAMP ");
			if(notNull) sb.append(" NOT NULL");
			if(defaultValue != null) {
				try {
					datetimeFormat.parse(defaultValue);
					defaultValue = "'"+defaultValue+"'";
				} catch (ParseException e) {
				}
				sb.append(" DEFAULT ").append(defaultValue).append("");
			}
			if(onUpdate != null) {
				try {
					datetimeFormat.parse(onUpdate);
					onUpdate = "'"+onUpdate+"'";
				} catch (ParseException e) {
				}

				sb.append(" ON UPDATE ").append(onUpdate).append("");
			}
			sb.append(",\n");
		}
		return this;
	}
	
	public TableCreator primaryKey( String key ) {
		if( started && ! ended ) {
			sb.append("PRIMARY KEY ("+mark + key.replace(mark, "")+mark+")\n");
		}
		return this;
	}	
	
	/*
	 * CREATE TABLE `TEST_DEMO`.`ACCOUNT` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `USER_NAME` VARCHAR(45) NOT NULL,
  `ADDRESS` VARCHAR(45) NOT NULL,
  `ACCESSOR_ID` INT NOT NULL,
  PRIMARY KEY (`ID`));

	 * */
	
	public String getFullTableName(){
		return fullName;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}

	
	
}
