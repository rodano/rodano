package ch.rodano.api.configuration.security;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Component;

import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.session.PendingSessionUpdates;
import ch.rodano.core.services.bll.session.SessionService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.utils.RightsService;

@Component
public class SessionOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final UserDAOService userDAOService;
	private final SessionService sessionService;
	private final RightsService rightsService;
	private final RoleService roleService;
	private final PendingSessionUpdates pendingSessionUpdates;

	public SessionOpaqueTokenIntrospector(
		final UserDAOService userDAOService,
		final SessionService sessionService,
		final RightsService rightsService,
		final RoleService roleService,
		final PendingSessionUpdates pendingSessionUpdates
	) {
		this.userDAOService = userDAOService;
		this.sessionService = sessionService;
		this.rightsService = rightsService;
		this.roleService = roleService;
		this.pendingSessionUpdates = pendingSessionUpdates;
	}

	//not transactional on purpose: this runs in the security filter chain, before the controller's own transaction begins
	@Override
	public OAuth2AuthenticatedPrincipal introspect(final String token) {
		//validate token format before hitting the database
		if(!StringUtils.isAsciiPrintable(token) || token.length() != SessionService.SESSION_TOKEN_STRING_LENGTH) {
			throw new OAuth2IntrospectionException("Invalid token");
		}

		final var session = sessionService.getSessionByToken(token);
		if(session == null) {
			throw new OAuth2IntrospectionException("Unknown session");
		}

		//flag session for last-access-time update
		pendingSessionUpdates.add(session.getPk(), ZonedDateTime.now());

		final var user = userDAOService.getUserByPk(session.getUserFk());
		final var roles = roleService.getActiveRoles(user);

		final List<GrantedAuthority> authorities = new ArrayList<>();
		if(!roles.isEmpty()) {
			if(!user.isShouldChangePassword()) {
				authorities.add(new SimpleGrantedAuthority(Authority.USER));
				if(rightsService.hasRightAdmin(roles)) {
					authorities.add(new SimpleGrantedAuthority(Authority.ADMIN));
				}
			}
		}

		logger.trace("User {} is validating their session with authorities {}", user.getName(), authorities);

		final Map<String, Object> attributes = Map.of("user", user, "session", session);
		return new OAuth2IntrospectionAuthenticatedPrincipal(user.getName(), attributes, authorities);
	}
}
