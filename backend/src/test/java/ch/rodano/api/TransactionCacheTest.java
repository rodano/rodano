package ch.rodano.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.rodano.api.actor.UserDTO;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

@SpringTestConfiguration
public class TransactionCacheTest extends ControllerTest {

	@BeforeEach
	public void freeMainThreadCache() {
		transactionCacheDAOService.emptyCache();
	}

	@Test
	@DisplayName("Cache is emptied after a successful request")
	public void cacheIsEmptiedAfterSuccess() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		// Send a request that writes something to the database
		client.put().uri("/users/1/remove").exchange().expectStatus().isOk().expectBody(UserDTO.class);
		client.put().uri("/users/1/restore").exchange().expectStatus().isOk().expectBody(UserDTO.class);

		// Check that no thread has a cache.
		assertEquals(0, transactionCacheDAOService.getCache().getJavaObjectCacheSize());
		assertEquals(0, transactionCacheDAOService.getCache().getRecordCacheSize());
	}

	@Test
	@DisplayName("Cache is emptied after an error")
	public void cacheIsEmptiedAfterError() {
		// Log in as the DM user
		authenticate(dataManagerOnStudyEmail);

		// Make whichever request that writes to the database
		// DM does not have sufficient rights and is thrown out of the system
		client.put().uri("/users/1/remove").exchange().expectStatus().isUnauthorized();

		// Check that all transaction caches have been emptied after a request that produces an error
		assertEquals(0, transactionCacheDAOService.getCache().getJavaObjectCacheSize());
		assertEquals(0, transactionCacheDAOService.getCache().getRecordCacheSize());
	}
}
