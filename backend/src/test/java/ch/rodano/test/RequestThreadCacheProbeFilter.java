package ch.rodano.test;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ch.rodano.core.services.unitofwork.UnitOfWorkService;

/**
 * Reports, in response headers, the state of the unit-of-work cache of the container thread that actually handled the
 * request. Under {@code RANDOM_PORT} the requests run on server threads, so a test running on the JUnit thread cannot
 * observe those caches directly; this filter is the only way to assert what a request leaves behind on its own thread.
 * <p>
 * It runs inside {@link ch.rodano.api.configuration.filter.UnitOfWorkFilter}, so a unit of work is always bound. The
 * {@code start} header is the cache size when this filter is entered: it is the baseline a request begins with, made up
 * only of what the security filter chain caches during token introspection and never of anything a previous request
 * left behind, since each request opens its own unit of work whose cache is released when it closes. Comparing that
 * baseline across two requests served by the same pooled thread is therefore what proves the cache does not leak from
 * one request to the next. The {@code end} header is the cache size after the request has run but before its unit of
 * work is closed, so it reflects the objects the request loaded and is reported for information only.
 */
@Profile("test")
@Component
//run inside UnitOfWorkFilter (ordered lower) so a unit of work is bound; the security chain shares this order and may
//run first, so the baseline reported here already includes what token introspection caches
@Order(SecurityFilterProperties.DEFAULT_FILTER_ORDER)
public class RequestThreadCacheProbeFilter extends OncePerRequestFilter {
	public static final String START_JAVA_OBJECTS_HEADER = "X-UoW-Start-JavaObjects";
	public static final String START_RECORDS_HEADER = "X-UoW-Start-Records";
	public static final String END_JAVA_OBJECTS_HEADER = "X-UoW-End-JavaObjects";
	public static final String END_RECORDS_HEADER = "X-UoW-End-Records";

	private final UnitOfWorkService unitOfWorkService;

	public RequestThreadCacheProbeFilter(final UnitOfWorkService unitOfWorkService) {
		this.unitOfWorkService = unitOfWorkService;
	}

	@Override
	protected void doFilterInternal(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final FilterChain filterChain
	) throws ServletException, IOException {
		final var startCache = unitOfWorkService.current().getCache();
		response.setHeader(START_JAVA_OBJECTS_HEADER, Integer.toString(startCache.getJavaObjectCacheSize()));
		response.setHeader(START_RECORDS_HEADER, Integer.toString(startCache.getRecordCacheSize()));

		filterChain.doFilter(request, response);

		final var endCache = unitOfWorkService.current().getCache();
		response.setHeader(END_JAVA_OBJECTS_HEADER, Integer.toString(endCache.getJavaObjectCacheSize()));
		response.setHeader(END_RECORDS_HEADER, Integer.toString(endCache.getRecordCacheSize()));
	}

	@Override
	protected boolean shouldNotFilterErrorDispatch() {
		return false;
	}
}
