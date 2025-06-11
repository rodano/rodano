package ch.rodano.core.services.bll;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class DatasetServiceTest extends DatabaseTest {

	@Autowired
	private DatasetDAOService datasetDAOService;

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private TestHelperService testHelperService;

	private Scope center;
	private Scope patient;
	private DatasetModel dmtGridDocument;

	@BeforeEach
	public void initTest() {
		center = testHelperService.createCenter(context);
		patient = testHelperService.createPatient(center, context);
		dmtGridDocument = studyService.getStudy().getDatasetModel("DMT_GRID");
	}

	@Test
	@DisplayName("Dataset creation works")
	public void testDatasetCreation() {
		final var dmtDataset = datasetService.create(patient, dmtGridDocument, context, TEST_RATIONALE);

		final var patientDatasets = datasetService.getAll(patient, Collections.singleton(dmtGridDocument));
		assertEquals(1, patientDatasets.size());
		assertEquals(dmtGridDocument.getId(), dmtDataset.getDatasetModelId());
		assertEquals(1, datasetDAOService.getAuditTrails(dmtDataset, Optional.empty(), Optional.empty()).size());
	}

	@Test
	@DisplayName("Dataset deletion works")
	public void testDeleteDataset() {
		// Create a dataset
		final var dmtDataset = datasetService.create(patient, dmtGridDocument, context, TEST_RATIONALE);

		// Delete dataset
		final var deletionRationale = "Deletion";
		datasetDAOService.deleteDataset(dmtDataset, context, deletionRationale);
		assertTrue(dmtDataset.getDeleted());

		// check the deleted flag and audit trail
		final var trails = datasetDAOService.getAuditTrails(dmtDataset, Optional.empty(), Optional.empty());
		final var trail = trails.last();
		assertEquals(Actor.SYSTEM_USERNAME, trail.getAuditActor());
		assertEquals(deletionRationale, trail.getAuditContext());
		assertTrue(trail.getDeleted());
	}

	@Test
	@DisplayName("Dataset restoration works")
	public void testRestoreDataset() {
		final var dmtDataset = datasetService.create(patient, dmtGridDocument, context, TEST_RATIONALE);

		// Delete dataset
		datasetDAOService.deleteDataset(dmtDataset, context, TEST_RATIONALE);

		// Restore dataset
		final var restorationRationale = "Restoration des Bourbons";
		datasetDAOService.restoreDataset(dmtDataset, context, restorationRationale);
		assertFalse(dmtDataset.getDeleted());
		final var trails = datasetDAOService.getAuditTrails(dmtDataset, Optional.empty(), Optional.empty());
		final var trail = trails.last();
		assertEquals(Actor.SYSTEM_USERNAME, trail.getAuditActor());
		assertEquals(restorationRationale, trail.getAuditContext());
	}
}
