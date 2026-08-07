package ch.rodano.api.configuration.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ch.rodano.core.services.unitofwork.UnitOfWorkService;

/**
 * Opens the unit of work of an HTTP request.
 * It is ordered before the Spring Security filter chain because the very first database access of a request happens
 * there, during token introspection, and because a request rejected by the security filters never reaches the
 * dispatcher servlet and would otherwise leave its objects behind on a pooled container thread.
 */
@Component
@Order(SecurityFilterProperties.DEFAULT_FILTER_ORDER - 1)
public class UnitOfWorkFilter extends OncePerRequestFilter {
	private final UnitOfWorkService unitOfWorkService;

	public UnitOfWorkFilter(final UnitOfWorkService unitOfWorkService) {
		this.unitOfWorkService = unitOfWorkService;
	}

	@Override
	protected void doFilterInternal(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final FilterChain filterChain
	) throws ServletException, IOException {
		//the chain throws two checked exceptions, so the scoped operation can only declare their common supertype
		try {
			unitOfWorkService.<Void, Exception>run(false, () -> {
				filterChain.doFilter(request, response);
				return null;
			});
		}
		catch(final Exception e) {
			if(e instanceof final ServletException servletException) {
				throw servletException;
			}
			if(e instanceof final IOException ioException) {
				throw ioException;
			}
			if(e instanceof final RuntimeException runtimeException) {
				throw runtimeException;
			}
			throw new ServletException(e);
		}
	}

	@Override
	protected boolean shouldNotFilterErrorDispatch() {
		return false;
	}
}
