package ch.rodano.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.core.ParameterizedTypeReference;

import ch.rodano.api.authentication.CredentialsDTO;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

@SpringTestConfiguration
public class AdministrationControllerTest extends ControllerTest {

	private final ParameterizedTypeReference<Map<String, Boolean>> dictionaryType = new ParameterizedTypeReference<Map<String, Boolean>>() {
		//don't care
	};

	@Test
	@DisplayName("Maintenance mode toggle endpoint works")
	void toggleMaintenanceAndMaintenance() {
		authenticate(adminOnStudyEmail);
		client.post().uri("/administration/maintenance").body(Map.of("state", true)).exchange().expectStatus().isAccepted();

		Map<String, Boolean> inMaintenance = get("/administration/maintenance", dictionaryType);
		assertTrue(inMaintenance.get("state"));

		client.post().uri("/administration/maintenance").body(Map.of("state", false)).exchange().expectStatus().isAccepted();
		inMaintenance = get("/administration/maintenance", dictionaryType);
		assertFalse(inMaintenance.get("state"));
	}

	@Test
	@DisplayName("Only administrators can log in during maintenance mode")
	void loginDuringMaintenance() {
		authenticate(adminOnStudyEmail);
		client.post().uri("/administration/maintenance").body(Map.of("state", true)).exchange().expectStatus().isAccepted();
		token = Optional.empty();

		final var invalidAdminCredentials = new CredentialsDTO();
		invalidAdminCredentials.setEmail(adminOnStudyEmail);
		invalidAdminCredentials.setPassword("invalid-password");
		client.post().uri("/sessions").body(invalidAdminCredentials).exchange().expectStatus().isUnauthorized();

		final var investigatorCredentials = new CredentialsDTO();
		investigatorCredentials.setEmail(investigatorOnStudyEmail);
		investigatorCredentials.setPassword(TEST_USER_PASSWORD);
		client.post().uri("/sessions").body(investigatorCredentials).exchange().expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

		authenticate(adminOnStudyEmail);
		client.post().uri("/administration/maintenance").body(Map.of("state", false)).exchange().expectStatus().isAccepted();
	}

	@Test
	@DisplayName("Debug mode toggle endpoint works")
	void toggleDebugAndGetDebug() {
		authenticate(adminOnStudyEmail);
		client.post().uri("/administration/debug").body(Map.of("state", true)).exchange().expectStatus().isAccepted();
		Map<String, Boolean> inDebug = get("/administration/debug", dictionaryType);
		assertTrue(inDebug.get("state"));

		client.post().uri("/administration/debug").body(Map.of("state", false)).exchange().expectStatus().isAccepted();
		inDebug = get("/administration/debug", dictionaryType);
		assertFalse(inDebug.get("state"));
	}

	@Test
	@DisplayName("Configuration reload endpoint works")
	void reloadConfiguration() {
		authenticate(adminOnStudyEmail);
		client.post().uri("/administration/reload").exchange().expectStatus().isAccepted();
	}
}
