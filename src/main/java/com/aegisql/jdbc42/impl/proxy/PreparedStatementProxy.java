package com.aegisql.jdbc42.impl.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.jdbc42.SubmittedStatement;
import com.aegisql.jdbc42.impl.AegisConnection;

public class PreparedStatementProxy implements InvocationHandler, SubmittedStatement {

	public final org.slf4j.Logger log = LoggerFactory.getLogger(PreparedStatementProxy.class);
	
	private PreparedStatement inner;
	private final AegisConnection connection;
	private final String sql;
	
	private SubmittedBy submitter = null;

	private PreparedStatementProxy(AegisConnection connection, PreparedStatement impl, String sql) {
		this.inner      = impl;
		this.connection = connection;
		this.sql        = sql;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		if (Object.class == method.getDeclaringClass()) {
			String name = method.getName();
			if ("equals".equals(name)) {
				return proxy == args[0];
			} else if ("hashCode".equals(name)) {
				return System.identityHashCode(proxy);
			} else if ("toString".equals(name)) {
				return proxy.getClass().getName() + "@"	+ Integer.toHexString(System.identityHashCode(proxy)) + ", with PreparedStatementProxy " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}
		
		if(statementMethods.contains( method.getName() )) {
			
			if( args != null ) { //Statement interface
				args[0] = connection.getAuthorizer().buildAuthorizedQuery( (String) args[0], submitter == null ? connection.getConnectionSubmitter() : submitter );
			} else { //PreparedStatement interface. Need to be covered with test
				PreparedStatement ps = inner;
				Connection c = ps.getConnection();
				String sql = ""+inner;
				sql = sql.substring(sql.indexOf(" "));
				ps = c.prepareStatement(sql);
				inner = ps;
				return method.invoke(ps, args);
			}
		} else if(method.getName().equals( "setSubmitter" ) ) {
			this.setSubmitter((SubmittedBy) args[0]);
			return null;
		} else if(method.getName().equals( "getSubmitter" ) ) {
			return this.getSubmitter();
		}
		
		return method.invoke(inner, args);
	}

	@Override
	public void setSubmitter( SubmittedBy submitter ) {
		this.submitter = submitter;
	}

	@Override
	public SubmittedBy getSubmitter() {
		return submitter;
	}

	public String getInnerSql() {
		return sql;
	}
	
	private static final Set<String> statementMethods = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("execute");
			add("executeQuery");
			add("executeUpdate");
			add("executeLargeUpdate");
			add("addBatch");
		}
	};
	
	public static PreparedStatement wrap(AegisConnection connection, PreparedStatement impl, String sql) {
		PreparedStatement ps = (PreparedStatement) Proxy.newProxyInstance(
				PreparedStatement.class.getClassLoader(),
				new Class[] {PreparedStatement.class},
				new PreparedStatementProxy(connection,impl,sql)
				);
		return ps;
	}

}