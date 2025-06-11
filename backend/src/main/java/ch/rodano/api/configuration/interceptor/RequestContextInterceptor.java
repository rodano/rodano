package ch.rodano.api.configuration.interceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.core.model.actor.Actor;

/**
 * Extracts the request info from the HTTP request and passes that info to the request context service.
 */
@Component
public class RequestContextInterceptor implements HandlerInterceptor {

	private final static List<HttpMethod> AUDITED_METHODS = Arrays.asList(
		HttpMethod.PUT,
		HttpMethod.POST,
		HttpMethod.DELETE
	);

	private final RequestContextService requestContextService;

	public RequestContextInterceptor(final RequestContextService requestContextService) {
		this.requestContextService = requestContextService;
	}

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
		if(AUDITED_METHODS.contains(HttpMethod.valueOf(request.getMethod()))) {

			final var rationale = request.getMethod() + " : " + request.getRequestURL().toString();

			// Sometimes there is no actor on the action, (e.g. when the user is logging in)
			final var authentication = SecurityContextHolder.getContext().getAuthentication();

			final Optional<Actor> actor;
			if(authentication instanceof AnonymousAuthenticationToken || authentication == null) {
				actor = Actor.SYSTEM;
			}
			else {
				actor = Optional.of((Actor) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			}

			// Store the contextual info in a request-scope service
			requestContextService.setAuditedRequest(true);
			requestContextService.setActor(actor);
			requestContextService.setRationale(rationale);
		}

		return true;
	}
}
