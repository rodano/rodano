package ch.rodano.api.configuration.security;

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
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.dao.robot.RobotDAOService;
import ch.rodano.core.utils.RightsService;

@Component
public class RobotBasicAuthenticationProvider implements AuthenticationProvider {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final RobotDAOService robotDAOService;
	private final RightsService rightsService;
	private final RoleService roleService;

	public RobotBasicAuthenticationProvider(
		final RobotDAOService robotDAOService,
		final RightsService rightsService,
		final RoleService roleService
	) {
		this.robotDAOService = robotDAOService;
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
	@Transactional
	public Authentication authenticate(final Authentication authentication) {
		final var name = authentication.getName();
		final var key = authentication.getCredentials().toString();

		final var robot = robotDAOService.getRobotByNameAndKey(name, key);
		if(robot == null) {
			throw new RobotDoesNotExistException(name);
		}

		final var roles = roleService.getActiveRoles(robot);
		if(roles.isEmpty()) {
			throw new RobotNotActivatedException(name);
		}

		//retrieve authorities
		final List<GrantedAuthority> authorities = new ArrayList<>();
		if(!roles.isEmpty()) {
			authorities.add(new SimpleGrantedAuthority(Authority.ROLE_USER.name()));
			if(rightsService.hasRightAdmin(roles)) {
				authorities.add(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.name()));
			}
		}

		logger.debug("Robot {} is logging with authorities {}", name, authorities);

		return new UsernamePasswordAuthenticationToken(robot, key, authorities);
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
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
