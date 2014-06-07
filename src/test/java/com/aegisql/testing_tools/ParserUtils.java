package com.aegisql.testing_tools;

import java.io.Reader;
import java.io.StringReader;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

public class ParserUtils {

	public ParserUtils() {
		// TODO Auto-generated constructor stub
	}

	public static Statement parseQuery(String query) throws ParseException {
		Reader reader = new StringReader(query);
		CCJSqlParser parser = new CCJSqlParser(reader);
		return parser.Statement();
	}
	
}
