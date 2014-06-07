	package com.aegisql.sql;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class SelectItemsFactory {

	public static List<SelectItem> getSelectItemsList(String tableName, List<String> columns ) {
		return getSelectItemsList(tableName,columns.toArray(new String[]{}));
	}
	
	public static List<SelectItem> getSelectItemsList(String tableName, String ... columns) {
		List<SelectItem> items = new ArrayList<SelectItem>();
		Table table = new Table();
		table.setName(tableName);

		for( String columnName : columns ) {
			if(columnName.contains("*")) throw new RuntimeException("SelectItemsFactory can only process real column names. * is not allowed!");
			SelectExpressionItem item = new SelectExpressionItem();
			Column column = new Column();
			column.setColumnName(columnName);
			column.setTable(table);
			item.setExpression(column);
			items.add(item);
		}
		
		return items;
	}

}
