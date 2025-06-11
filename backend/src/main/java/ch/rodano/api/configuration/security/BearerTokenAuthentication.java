package ch.rodano.api.configuration.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class BearerTokenAuthentication extends AbstractAuthenticationToken {
	private static final long serialVersionUID = 2193341416571338550L;

	private final String credentials;

	/**
	 * Creates a token with the supplied array of authorities.
	 *
	 * @param token The bearer token
	 */
	public BearerTokenAuthentication(final String token) {
		super(null);
		credentials = token;
	}

	@Override
	public String getName() {
		return credentials;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

	@Override
	public Object getPrincipal() {
		//TODO throwing an exception here make some requests fail unexpectedly
		//the problem is this method should never been called as this "principal" is only used to store a token
		//throw new NotImplementedException();
		//return null instead
		return null;
	}
}
