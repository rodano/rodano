package ch.rodano.api.configuration.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ch.rodano.api.configuration.security.BearerTokenAuthentication;
import ch.rodano.api.configuration.security.BearerTokenAuthenticationProvider;
import ch.rodano.core.services.bll.session.SessionService;

/**
 * This filter retrieves the bearer token from an incoming request, if present, and tries to authenticate the user
 * through the provided token. If the token is not present, or it is invalid, continue the filter chain as if nothing
 * happened.
 */
@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_BEARER_PREFIX = "Bearer";

	private final BearerTokenAuthenticationProvider bearerTokenAuthenticationProvider;

	public BearerTokenAuthenticationFilter(
		final BearerTokenAuthenticationProvider bearerTokenAuthenticationProvider
	) {
		this.bearerTokenAuthenticationProvider = bearerTokenAuthenticationProvider;
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
		// Get the HTTP Authorization header from the request
		final var authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		// Check that it has the 'Bearer' prefix
		if(StringUtils.startsWith(authorizationHeader, AUTHORIZATION_BEARER_PREFIX)) {
			// Extract the token from the HTTP Authorization header
			final var token = StringUtils.substringAfter(authorizationHeader, AUTHORIZATION_BEARER_PREFIX).trim();

			// Check that the token conforms to length and allowed characters
			if(StringUtils.isNotBlank(token) && StringUtils.isAsciiPrintable(token) && token.length() == SessionService.SESSION_TOKEN_STRING_LENGTH) {
				// Create a bearer token authentication that is provided to the auth provider
				final var tokenAuthentication = new BearerTokenAuthentication(token);

				// Authenticate the user through the provided token and provide appropriate authorization
				final var authentication = bearerTokenAuthenticationProvider.authenticate(tokenAuthentication);

				// If the token was invalid, do nothing. Otherwise...
				if(authentication != null) {
					// Create new context instead of getting the existing one to avoid the race conditions
					final var context = SecurityContextHolder.createEmptyContext();

					// Set the created authentication to the security context holder
					context.setAuthentication(authentication);
					SecurityContextHolder.setContext(context);
				}
			}
		}

		// Continue the filter chain
		filterChain.doFilter(request, response);
	}
}
