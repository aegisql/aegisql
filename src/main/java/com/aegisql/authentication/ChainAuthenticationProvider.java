package com.aegisql.authentication;

import org.w3c.dom.Node;

public class ChainAuthenticationProvider implements AuthenticationProvider {

	@Override
	public UserAuthentication provideUserAuthentication( Node authenticationProviderNode ) throws Exception {
		UserAuthenticationChain chain = new UserAuthenticationChain();
		return chain;
	}

}
