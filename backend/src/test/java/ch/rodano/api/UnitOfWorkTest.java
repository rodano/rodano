package ch.rodano.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.rodano.api.actor.UserDTO;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

@SpringTestConfiguration
public class UnitOfWorkTest extends ControllerTest {

	@Test
	@DisplayName("Cache is emptied after a successful request")
	public void cacheIsEmptiedAfterSuccess() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		// The requests below run on their own container thread and thus in their own unit of work; discard whatever the
		// test set-up left in this thread's unit of work so the assertion only reflects what the requests leave behind
		unitOfWorkService.clear();

		// Send a request that writes something to the database
		client.put().uri("/users/1/remove").exchange().expectStatus().isOk().expectBody(UserDTO.class);
		client.put().uri("/users/1/restore").exchange().expectStatus().isOk().expectBody(UserDTO.class);

		// Check that no thread has a cache.
		assertEquals(0, unitOfWorkService.current().getCache().getJavaObjectCacheSize());
		assertEquals(0, unitOfWorkService.current().getCache().getRecordCacheSize());
	}

	@Test
	@DisplayName("Cache is emptied after an error")
	public void cacheIsEmptiedAfterError() {
		// Log in as the DM user
		authenticate(dataManagerOnStudyEmail);

		// The request below runs on its own container thread and thus in its own unit of work; discard whatever the
		// test set-up left in this thread's unit of work so the assertion only reflects what the request leaves behind
		unitOfWorkService.clear();

		// Make whichever request that writes to the database
		// DM does not have sufficient rights and is thrown out of the system
		client.put().uri("/users/1/remove").exchange().expectStatus().isUnauthorized();

		// Check that all caches have been emptied after a request that produces an error
		assertEquals(0, unitOfWorkService.current().getCache().getJavaObjectCacheSize());
		assertEquals(0, unitOfWorkService.current().getCache().getRecordCacheSize());
	}

	@Test
	@DisplayName("Cache survives from the security filter chain to the controller")
	public void cacheSurvivesAcrossAuthenticationAndControllerTransactions() {
		authenticate(adminOnStudyEmail);

		// The request below runs on its own container thread and thus in its own unit of work; discard whatever the
		// test set-up left in this thread's unit of work so the assertion only reflects what the request leaves behind
		unitOfWorkService.clear();

		// Logout is authenticated: token introspection runs in the security filter chain, before the controller's
		// transaction begins, and caches the current user's record that the controller then needs to update
		client.delete().uri("/sessions").exchange().expectStatus().isNoContent();

		// Check that the cache has been emptied once the whole request completed
		assertEquals(0, unitOfWorkService.current().getCache().getJavaObjectCacheSize());
		assertEquals(0, unitOfWorkService.current().getCache().getRecordCacheSize());
	}
}
