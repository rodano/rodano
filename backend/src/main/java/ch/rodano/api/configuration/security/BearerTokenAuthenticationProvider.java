package ch.rodano.api.configuration.security;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.session.SessionService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.utils.RightsService;

@Component
public class BearerTokenAuthenticationProvider implements AuthenticationProvider {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final UserDAOService userDAOService;
	private final SessionService sessionService;
	private final RightsService rightsService;
	private final RoleService roleService;

	public BearerTokenAuthenticationProvider(
		final UserDAOService userDAOService,
		final SessionService sessionService,
		final RightsService rightsService,
		final RoleService roleService
	) {
		this.userDAOService = userDAOService;
		this.sessionService = sessionService;
		this.rightsService = rightsService;
		this.roleService = roleService;
	}

	/**
	 * Performs authentication with the same contract as
	 * {@link AuthenticationManager#authenticate(Authentication)}
	 * .
	 *
	 * @param authentication the authentication request object.
	 * @return a fully authenticated object including credentials. May return
	 * <code>null</code> if the <code>AuthenticationProvider</code> is unable to support
	 * authentication of the passed <code>Authentication</code> object. In such a case,
	 * the next <code>AuthenticationProvider</code> that supports the presented
	 * <code>Authentication</code> class will be tried.
	 * @throws AuthenticationException if authentication fails.
	 */
	@Override
	public Authentication authenticate(final Authentication authentication) {
		// Get the token
		final var token = authentication.getCredentials().toString();

		// Get the session associated with the token
		final var session = sessionService.getSessionByToken(token);

		if(session != null) {
			// Update the user's last access date in the database if required
			// Update session only once every 5 seconds to improve performance
			if(session.getLastAccessTime().plusSeconds(5).isBefore(ZonedDateTime.now())) {
				sessionService.refreshSession(session);
			}

			// Get the user and their roles
			final var user = userDAOService.getUserByPk(session.getUserFk());
			final var roles = roleService.getActiveRoles(user);

			// Retrieve authorities
			final List<GrantedAuthority> authorities = new ArrayList<>();
			if(!roles.isEmpty()) {
				if(!user.isShouldChangePassword()) {
					authorities.add(new SimpleGrantedAuthority(Authority.ROLE_USER.name()));
					if(rightsService.hasRightAdmin(roles)) {
						authorities.add(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.name()));
					}
				}
			}

			logger.trace("User {} is validating their session with authorities {}", user.getName(), authorities);

			// Return authentication object if the user has been successfully authenticated
			return new UsernamePasswordAuthenticationToken(user, token, authorities);
		}

		// Otherwise return null
		return null;
	}

	/**
	 * Returns <code>true</code> if this <Code>AuthenticationProvider</code> supports the
	 * indicated <Code>Authentication</code> object.
	 * <p>
	 * Returning <code>true</code> does not guarantee an
	 * <code>AuthenticationProvider</code> will be able to authenticate the presented
	 * instance of the <code>Authentication</code> class. It simply indicates it can
	 * support closer evaluation of it. An <code>AuthenticationProvider</code> can still
	 * return <code>null</code> from the {@link #authenticate(Authentication)} method to
	 * indicate another <code>AuthenticationProvider</code> should be tried.
	 * </p>
	 * <p>
	 * Selection of an <code>AuthenticationProvider</code> capable of performing
	 * authentication is conducted at runtime the <code>ProviderManager</code>.
	 * </p>
	 *
	 * @return <code>true</code> if the implementation can more closely evaluate the
	 * <code>Authentication</code> class presented
	 */
	@Override
	public boolean supports(final Class<?> authentication) {
		return authentication.equals(BearerTokenAuthentication.class);
	}
}
