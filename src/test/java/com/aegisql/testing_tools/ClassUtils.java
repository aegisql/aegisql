package com.aegisql.testing_tools;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ClassUtils {

public static String getAbsolutePath() throws ClassNotFoundException {
	StackTraceElement[] st = Thread.currentThread().getStackTrace();
	int i=st.length-1;
	for(;i>=0;i--) {
		StackTraceElement e = st[i];
		if("com.aegisql.testing_tools.ClassUtils".equals(e.getClassName())) {
			i++;
			break;
		}
	}	
	StackTraceElement ste = st[i];
	Class<?> thiz = Class.forName(ste.getClassName());
	String url = thiz.getResource("").getPath();
	return url;
}

public static String getAbsolutePath(String file) throws ClassNotFoundException {
	return getAbsolutePath()+file;
}

public static String getTopPackage() throws ClassNotFoundException {
	String[] pathElements = getAbsolutePath().split("[/\\\\]");
	return pathElements[pathElements.length-1];
}

public static String getPath(Object o) {
    String path = o.getClass().getResource(".").getPath();
    String decoded;
    try {
        decoded = URLDecoder.decode(path,"UTF-8");
    } catch (UnsupportedEncodingException e) {
        decoded = path;
    }
    return decoded;
}

public static String getPath(Class<?> o) {
    String path = o.getResource(".").getPath();
    String decoded;
    try {
        decoded = URLDecoder.decode(path,"UTF-8");
    } catch (UnsupportedEncodingException e) {
        decoded = path;
    }
    return decoded;
}

}
