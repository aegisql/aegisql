package com.aegisql.access;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public enum TableQueryType {
	INSERT,SELECT,UPDATE,DELETE,REPLACE,DROP,TRUNCATE,CREATE,LOAD,BLOCKED,VALIDATE;
	public static TableQueryType valueOf( Statement statement ) {
		if( statement != null ) {
			if( statement instanceof Insert )      return INSERT;
			if( statement instanceof Select )      return SELECT;
			if( statement instanceof Update )      return UPDATE;
			if( statement instanceof Delete )      return DELETE;
			if( statement instanceof Drop )        return DROP;
			if( statement instanceof Truncate )    return TRUNCATE;
			if( statement instanceof CreateTable ) return CREATE;
			if( statement instanceof Replace )     return REPLACE;
			//and there is nothing for LOAD yet
		}
		return BLOCKED;
		
	}
}
