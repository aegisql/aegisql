package com.aegisql.authentication;

import org.w3c.dom.Node;

public interface AuthenticationProvider {
	public UserAuthentication provideUserAuthentication(Node authenticationProviderNode) throws Exception;
}
