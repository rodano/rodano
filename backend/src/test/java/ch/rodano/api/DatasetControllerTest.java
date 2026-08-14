package ch.rodano.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.api.dataset.DatasetDTO;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

@SpringTestConfiguration
public class DatasetControllerTest extends ControllerTest {

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Test
	@DisplayName("A candidate dataset is built and rolled back within the request's own transaction")
	public void candidateDatasetIsNotPersisted() {
		authenticate(adminOnStudyEmail);

		final var patient = scopeDAOService.getScopeByCode("FR-01-01");

		final var candidate = client.get()
			.uri("/scopes/" + patient.getPk() + "/candidate-dataset?datasetModelId=PATIENT_DOCUMENTATION")
			.exchange()
			.expectStatus().isOk()
			.expectBody(DatasetDTO.class)
			.returnResult()
			.getResponseBody();

		assertEquals("PATIENT_DOCUMENTATION", candidate.getModelId());

		//the skeleton is created by the regular creation code then rolled back to a savepoint, so it carries no key
		assertNull(candidate.getPk());
		assertFalse(candidate.getFields().isEmpty());
		assertTrue(candidate.getFields().stream().allMatch(f -> f.getPk() == null));
	}
}
