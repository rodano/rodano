package ch.rodano.core.services.bll;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class DatasetServiceTest extends DatabaseTest {

	@Autowired
	private DatasetDAOService datasetDAOService;

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private EventService eventService;

	@Autowired
	private TestHelperService testHelperService;

	private Scope center;
	private EventModel visit6Model;
	private DatasetModel patientDocumentationModel;
	private DatasetModel dmtGridModel;
	private DatasetModel visitDocumentationModel;
	private DatasetModel relapseGridModel;

	@BeforeEach
	public void initTest() {
		center = testHelperService.createCenter(context);
		visit6Model = studyService.getStudy().getScopeModel("PATIENT").getEventModel("VISIT_6");
		patientDocumentationModel = studyService.getStudy().getDatasetModel("PATIENT_DOCUMENTATION");
		dmtGridModel = studyService.getStudy().getDatasetModel("DMT_GRID");
		visitDocumentationModel = studyService.getStudy().getDatasetModel("VISIT_DOCUMENTATION");
		relapseGridModel = studyService.getStudy().getDatasetModel("RELAPSES_GRID");
	}

	@Test
	@DisplayName("Dataset creation works")
	public void testDatasetCreation() {
		final var patient = testHelperService.createPatient(center, context);
		final var dmtDataset = datasetService.create(patient, dmtGridModel, context, TEST_RATIONALE);
		assertEquals(dmtGridModel.getId(), dmtDataset.getDatasetModelId());

		final var retrievedDatasets = datasetService.getAll(patient, Collections.singleton(dmtGridModel));
		assertEquals(1, retrievedDatasets.size());
		final var retrievedDataset = retrievedDatasets.getFirst();
		assertFalse(retrievedDataset.getDeleted());
		assertEquals(patient.getPk(), retrievedDataset.getScopeFk());

		assertEquals(1, datasetDAOService.getAuditTrails(dmtDataset, Optional.empty(), Optional.empty()).size());
	}

	@Test
	@DisplayName("Dataset deletion works")
	public void testDatasetDeletion() {
		final var patient = testHelperService.createPatient(center, context);
		final var dmtDataset = datasetService.create(patient, dmtGridModel, context, TEST_RATIONALE);

		//delete dataset
		final var deletionRationale = "Deletion";
		datasetService.delete(patient, Optional.empty(), dmtDataset, context, deletionRationale);
		assertTrue(dmtDataset.getDeleted());

		final var retrievedDatasets = datasetService.getAllIncludingRemoved(patient, Collections.singleton(dmtGridModel));
		assertEquals(1, retrievedDatasets.size());
		final var retrievedDataset = retrievedDatasets.getFirst();
		assertTrue(retrievedDataset.getDeleted());

		//check the deleted flag and audit trail
		final var trails = datasetDAOService.getAuditTrails(dmtDataset, Optional.empty(), Optional.empty());
		assertEquals(2, trails.size());
		final var trail = trails.last();
		assertEquals(Actor.SYSTEM_USERNAME, trail.getAuditActor());
		assertEquals(String.format("Dataset removed: %s", deletionRationale), trail.getAuditContext());
		assertTrue(trail.getDeleted());
	}

	@Test
	@DisplayName("Dataset restoration works")
	public void testDatasetRestoration() {
		final var patient = testHelperService.createPatient(center, context);
		final var dmtDataset = datasetService.create(patient, dmtGridModel, context, TEST_RATIONALE);

		//delete dataset
		datasetService.delete(patient, Optional.empty(), dmtDataset, context, TEST_RATIONALE);

		//restore dataset
		final var restorationRationale = "Restoration";
		datasetService.restore(patient, Optional.empty(), dmtDataset, context, restorationRationale);
		assertFalse(dmtDataset.getDeleted());

		final var retrievedDatasets = datasetService.getAll(patient, Collections.singleton(dmtGridModel));
		assertEquals(1, retrievedDatasets.size());
		final var retrievedDataset = retrievedDatasets.getFirst();
		assertFalse(retrievedDataset.getDeleted());

		//check the deleted flag and audit trail
		final var trails = datasetDAOService.getAuditTrails(dmtDataset, Optional.empty(), Optional.empty());
		assertEquals(3, trails.size());
		final var trail = trails.last();
		assertEquals(Actor.SYSTEM_USERNAME, trail.getAuditActor());
		assertEquals(String.format("Dataset restored: %s", restorationRationale), trail.getAuditContext());
		assertFalse(trail.getDeleted());
	}

	@Test
	@DisplayName("Scope dataset retrieval works")
	public void testRetrieveDatasets() {
		final var patient = testHelperService.createPatient(center, context);
		var datasets = datasetService.getAll(patient);

		assertFalse(datasets.isEmpty());
		final var datasetNumber = datasets.size();

		final var dataset = datasetService.get(patient, patientDocumentationModel);
		assertNotNull(dataset);
		assertEquals(patientDocumentationModel.getId(), dataset.getDatasetModelId());

		//add dataset
		final var dmtDataset = datasetService.create(patient, dmtGridModel, context, TEST_RATIONALE);

		assertThrows(InvalidParameterException.class, () -> datasetService.get(patient, dmtGridModel));

		datasets = datasetService.getAll(patient);
		assertEquals(datasetNumber + 1, datasets.size());

		datasets = datasetService.getAll(patient, Collections.singleton(dmtGridModel));
		assertEquals(1, datasets.size());
		assertEquals(dmtGridModel.getId(), datasets.getFirst().getDatasetModelId());

		//delete dataset
		datasetDAOService.deleteDataset(dmtDataset, context, TEST_RATIONALE);

		datasets = datasetService.getAll(patient);
		assertEquals(datasetNumber, datasets.size());

		datasets = datasetService.getAllIncludingRemoved(patient);
		assertEquals(datasetNumber + 1, datasets.size());

		datasets = datasetService.getAll(patient, Collections.singleton(dmtGridModel));
		assertEquals(0, datasets.size());

		datasets = datasetService.getAllIncludingRemoved(patient, Collections.singleton(dmtGridModel));
		assertEquals(1, datasets.size());
		assertEquals(dmtGridModel.getId(), datasets.getFirst().getDatasetModelId());
	}

	@Test
	@DisplayName("Event dataset retrieval works")
	public void testGetAllForEvent() {
		final var patient = testHelperService.createPatient(center, context);
		final var visit6Event = eventService.get(patient, visit6Model, 0);
		var datasets = datasetService.getAll(visit6Event);

		assertFalse(datasets.isEmpty());
		final var datasetNumber = datasets.size();

		final var dataset = datasetService.get(visit6Event, visitDocumentationModel);
		assertNotNull(dataset);
		assertEquals(visitDocumentationModel.getId(), dataset.getDatasetModelId());

		//add dataset
		final var dmtDataset = datasetService.create(patient, visit6Event, relapseGridModel, context, TEST_RATIONALE);

		datasets = datasetService.getAll(visit6Event);
		assertEquals(datasetNumber + 1, datasets.size());

		datasets = datasetService.getAll(visit6Event, Collections.singleton(relapseGridModel));
		assertEquals(1, datasets.size());
		assertEquals(relapseGridModel.getId(), datasets.getFirst().getDatasetModelId());

		//delete dataset
		datasetDAOService.deleteDataset(dmtDataset, context, TEST_RATIONALE);

		datasets = datasetService.getAll(visit6Event);
		assertEquals(datasetNumber, datasets.size());

		datasets = datasetService.getAllIncludingRemoved(visit6Event);
		assertEquals(datasetNumber + 1, datasets.size());

		datasets = datasetService.getAll(visit6Event, Collections.singleton(relapseGridModel));
		assertEquals(0, datasets.size());

		datasets = datasetService.getAllIncludingRemoved(visit6Event, Collections.singleton(relapseGridModel));
		assertEquals(1, datasets.size());
		assertEquals(relapseGridModel.getId(), datasets.getFirst().getDatasetModelId());
	}

}
