package ch.rodano.api;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;

import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
public class AdministrationControllerTest extends ControllerTest {

	@Test
	@DisplayName("Maintenance mode toggle endpoint works")
	void toggleMaintenanceAndMaintenance() {
		authenticate(adminOnStudyEmail);
		executePostAndReturnBody("/administration/maintenance", Collections.singletonMap("state", true), Void.class);
		Map<String, Boolean> inMaintenance = executeGetAndReturnBody("/administration/maintenance", new ParameterizedTypeReference<Map<String, Boolean>>() {});
		assertTrue(inMaintenance.get("state"));

		executePostAndReturnBody("/administration/maintenance", Collections.singletonMap("state", false), Void.class);
		inMaintenance = executeGetAndReturnBody("/administration/maintenance", new ParameterizedTypeReference<Map<String, Boolean>>() {});
		assertFalse(inMaintenance.get("state"));
	}

	@Test
	@DisplayName("Debug mode toggle endpoint works")
	void toggleDebugAndGetDebug() {
		authenticate(adminOnStudyEmail);
		executePostAndReturnBody("/administration/debug", Collections.singletonMap("state", true), Void.class);
		Map<String, Boolean> inDebug = executeGetAndReturnBody("/administration/debug", new ParameterizedTypeReference<Map<String, Boolean>>() {});
		assertTrue(inDebug.get("state"));

		executePostAndReturnBody("/administration/debug", Collections.singletonMap("state", false), Void.class);
		inDebug = executeGetAndReturnBody("/administration/debug", new ParameterizedTypeReference<Map<String, Boolean>>() {});
		assertFalse(inDebug.get("state"));
	}

	@Test
	@DisplayName("Configuration reload endpoint works")
	void reloadConfiguration() {
		authenticate(adminOnStudyEmail);
		final var response = executePost("/administration/reload", HttpEntity.EMPTY, Void.class);
		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
	}
}
