package ch.rodano.api.configuration.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;

import ch.rodano.core.services.dao.commons.cache.transaction.TransactionCacheDAOService;

@Component
public class TransactionCacheHandlerInterceptor implements HandlerInterceptor {
	private final TransactionCacheDAOService transactionCacheDAOService;

	public TransactionCacheHandlerInterceptor(final TransactionCacheDAOService transactionCacheDAOService) {
		this.transactionCacheDAOService = transactionCacheDAOService;
	}

	/**
	 * Callback after completion of request processing, that is, after rendering
	 * the view. Will be called on any outcome of handler execution, thus allows
	 * for proper resource cleanup.
	 * <p>Note: Will only be called if this interceptor's {@code preHandle}
	 * method has successfully completed and returned {@code true}!
	 * <p>As with the {@code postHandle} method, the method will be invoked on each
	 * interceptor in the chain in reverse order, so the first interceptor will be
	 * the last to be invoked.
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link AsyncHandlerInterceptor}.
	 * <p>The default implementation is empty.
	 *
	 * @param request  current HTTP request
	 * @param response current HTTP response
	 * @param handler  handler (or {@link HandlerMethod}) that started asynchronous
	 *                 execution, for type and/or instance examination
	 * @param ex       exception thrown on handler execution, if any
	 */
	@Override
	public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex) {
		transactionCacheDAOService.emptyCache();
	}
}
