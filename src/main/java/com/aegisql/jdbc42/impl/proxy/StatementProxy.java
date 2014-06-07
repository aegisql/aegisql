package com.aegisql.jdbc42.impl.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.jdbc42.SubmittedStatement;
import com.aegisql.jdbc42.impl.AegisConnection;

public class StatementProxy implements InvocationHandler, SubmittedStatement {

	public final org.slf4j.Logger log = LoggerFactory.getLogger(StatementProxy.class);
	
	private Statement inner;
	private final AegisConnection connection;
	
	private SubmittedBy submitter = null;

	private StatementProxy(AegisConnection connection, Statement impl) {
		this.inner      = impl;
		this.connection = connection;
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
				return proxy.getClass().getName() + "@"	+ Integer.toHexString(System.identityHashCode(proxy)) + ", with StatementProxy " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}
		
		if(statementMethods.contains( method.getName() )) {
			args[0] = connection.getAuthorizer().buildAuthorizedQuery( (String) args[0], submitter == null ? connection.getConnectionSubmitter() : submitter );
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
	
	public static Statement wrap(AegisConnection connection, Statement impl) {
		Statement as = (Statement) Proxy.newProxyInstance(
				impl.getClass().getClassLoader(),
				new Class[] {Statement.class,SubmittedStatement.class},
				new StatementProxy(connection,impl)
				);
		return as;
	}

}