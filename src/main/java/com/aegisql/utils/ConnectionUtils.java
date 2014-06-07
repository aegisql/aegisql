package com.aegisql.utils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ConnectionUtils {

	public static final Map<String, String> knownSchemaDelim = new HashMap<>();
	static {
		knownSchemaDelim.put("mysql", "&");
		knownSchemaDelim.put("postgresql", "&");
		knownSchemaDelim.put("jtds:sqlserver", ";");
		knownSchemaDelim.put("jtds:sybase", ";");
		knownSchemaDelim.put("jtds:sqlserver", ";");
		knownSchemaDelim.put("microsoft:sqlserver", ";");

		knownSchemaDelim.put("", "&");
		knownSchemaDelim.put(null, "&");

	}

	public static Map<String, String> getQueryProperties(String dbType, String query) {
		Map<String, String> prop = new HashMap<String, String>();
		String delim;
		if (knownSchemaDelim.containsKey(dbType)) {
			delim = knownSchemaDelim.get(dbType);
		} else {
			delim = "&";
		}

		if (query != null && ! "".equals(query) ) {
			String[] pairs = query.split(delim);
			for (String pair : pairs) {
				if (pair != null && !"".equals(pair)) {
					String[] keyVal = pair.split("=", 2);
					if (keyVal.length == 2) {
						prop.put(keyVal[0], keyVal[1]);
					} else if (keyVal.length == 1) {
						prop.put(keyVal[0], null);
					} else {
						throw new RuntimeException("Unexpected condition for query: "+query+" dbType: "+dbType);
					}
				}
			}
		}
		return prop;
	}
	
	public static Map<String,String> parseConnectionUrl( String urlString ) {
		String cleanURI = urlString.substring(5); //remove jdbc:
		URI uri = URI.create(cleanURI);
		String scheme = uri.getScheme();
		String query = uri.getQuery();
		Map<String,String> uriProperties = getQueryProperties(scheme, query);
		uriProperties.put("_SCHEME_", scheme);
		return uriProperties;
	}
	
}
