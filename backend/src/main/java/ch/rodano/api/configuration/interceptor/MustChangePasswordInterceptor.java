package ch.rodano.api.configuration.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ch.rodano.core.model.exception.security.MustChangePasswordException;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.user.UserSecurityService;

/**
 * Checks if the connected user's password is still valid. If not, it throws a MustChangePasswordException with a 403 error.
 */
@Component
public class MustChangePasswordInterceptor implements HandlerInterceptor {

	private final UserSecurityService userSecurityService;

	public MustChangePasswordInterceptor(
		@Lazy final UserSecurityService userSecurityService
	) {
		this.userSecurityService = userSecurityService;
	}

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
		// Get the authentication
		final var authentication = SecurityContextHolder.getContext().getAuthentication();

		// Ignore if authentication is empty, if the request is anonymous or if the connected entity is a robot
		if(authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.getPrincipal() instanceof final User user) {
			if(userSecurityService.mustChangePassword(user)) {
				throw new MustChangePasswordException();
			}
		}

		return true;
	}
}
