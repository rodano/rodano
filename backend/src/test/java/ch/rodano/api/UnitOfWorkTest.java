package ch.rodano.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

import ch.rodano.api.actor.UserDTO;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.RequestThreadCacheProbeFilter;
import ch.rodano.test.SpringTestConfiguration;

@SpringTestConfiguration
public class UnitOfWorkTest extends ControllerTest {

	private String header(final RestTestClient.ResponseSpec response, final String name) {
		return response.returnResult(Void.class).getResponseHeaders().getFirst(name);
	}

	/**
	 * The request-thread cache size observed at the very start of a request, before it does any work of its own. It
	 * reflects only what the security filter chain cached during token introspection, and never anything left behind by
	 * a previous request: the cache is bound to a request's unit of work and released when it closes, so a request
	 * reusing a pooled server thread cannot see the objects of the request that ran on it before.
	 */
	private String startJavaObjects(final RestTestClient.ResponseSpec response) {
		return header(response, RequestThreadCacheProbeFilter.START_JAVA_OBJECTS_HEADER);
	}

	private String startRecords(final RestTestClient.ResponseSpec response) {
		return header(response, RequestThreadCacheProbeFilter.START_RECORDS_HEADER);
	}

	@Test
	@DisplayName("Two successive requests each run in their own unit of work and do not accumulate a cache")
	public void cacheIsEmptiedAfterSuccess() {
		authenticate(adminOnStudyEmail);

		// Two sequential requests, possibly served by the same pooled server thread. Each starts its own unit of work
		// with the same baseline cache (only what introspection caches for the authenticated caller), which proves the
		// first request released everything it loaded rather than leaving it for the second to reuse.
		final var first = client.put().uri("/users/1/remove").exchange();
		first.expectStatus().isOk().expectBody(UserDTO.class);

		final var second = client.put().uri("/users/1/restore").exchange();
		second.expectStatus().isOk().expectBody(UserDTO.class);

		assertEquals(startJavaObjects(first), startJavaObjects(second));
		assertEquals(startRecords(first), startRecords(second));
	}

	@Test
	@DisplayName("A request rejected by the security filters still runs in its own unit of work")
	public void cacheIsEmptiedAfterError() {
		authenticate(dataManagerOnStudyEmail);

		// The DM lacks the rights and is rejected before the controller. The unit of work is opened before the security
		// filters, so a rejected request still gets its own cache that is released with it; a second rejected request
		// therefore starts from the same baseline as the first rather than inheriting its objects.
		final var first = client.put().uri("/users/1/remove").exchange();
		first.expectStatus().isUnauthorized();

		final var second = client.put().uri("/users/1/remove").exchange();
		second.expectStatus().isUnauthorized();

		assertEquals(startJavaObjects(first), startJavaObjects(second));
		assertEquals(startRecords(first), startRecords(second));
	}

	@Test
	@DisplayName("Cache survives from the security filter chain to the controller within a request")
	public void cacheSurvivesAcrossAuthenticationAndControllerTransactions() {
		authenticate(adminOnStudyEmail);

		// Logout is authenticated: token introspection runs in the security filter chain, before the controller's
		// transaction begins, and caches the current user's record that the controller then needs to update. The
		// request can only succeed if the single instance cached during introspection is the very one the controller
		// updates, which is exactly the single-in-memory-copy guarantee spanning the filter chain and the controller.
		client.delete().uri("/sessions").exchange().expectStatus().isNoContent();
	}

	@Test
	@DisplayName("Test-thread operations run in the unit of work opened for the whole test run")
	public void testThreadRunsInTheTestRunUnitOfWork() {
		// The JUnit launcher interceptor opens a unit of work around the whole test run, so operations performed directly
		// on the test thread (rather than through an HTTP request) always have a current unit of work to bind to.
		assertEquals(true, unitOfWorkService.isActive());
	}
}
