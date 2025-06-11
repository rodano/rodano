package ch.rodano.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import ch.rodano.api.actor.UserDTO;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		restTemplate.exchange(
			"/users/24/remove",
			HttpMethod.PUT,
			null,
			UserDTO.class
		);

		restTemplate.exchange(
			"/users/24/restore",
			HttpMethod.PUT,
			null,
			UserDTO.class
		);

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
		final var response = restTemplate.exchange(
			"/users/1/remove",
			HttpMethod.PUT,
			null,
			UserDTO.class
		);

		// DM does not have sufficient rights and is thrown out of the system
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

		// Check that all transaction caches have been emptied after a request that produces an error
		assertEquals(0, transactionCacheDAOService.getCache().getJavaObjectCacheSize());
		assertEquals(0, transactionCacheDAOService.getCache().getRecordCacheSize());
	}
}
