package ch.rodano.core.services.bll.field;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.core.services.plugin.validator.exception.ValidatorException;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Field validation service")
@SpringTestConfiguration
@Transactional
public class FieldValidationServiceTest extends DatabaseTest {

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private ValidationService validationService;

	@Autowired
	private FieldService fieldService;

	@Autowired
	private WorkflowStatusService workflowStatusService;

	@Autowired
	private WorkflowStatusDAOService workflowStatusDAOService;

	@Autowired
	private TestHelperService testHelperService;

	private Scope center;
	private Scope patient;
	private Dataset patientDocumentation;

	private FieldModel birthDateFieldModel;
	private FieldModel dateFirstDrugFieldModel;
	private FieldModel genderFieldModel;
	private FieldModel employmentFieldModel;

	private Workflow queryWorkflow;
	private WorkflowState openQueryState;
	private Workflow protocolDeviationWorkflow;

	@BeforeEach
	public void initTest() throws IOException {
		studyService.reload();

		center = testHelperService.createCenter(context);
		patient = testHelperService.createPatient(center, context);
		patientDocumentation = datasetService.get(patient, studyService.getStudy().getDatasetModel("PATIENT_DOCUMENTATION"));

		birthDateFieldModel = patientDocumentation.getDatasetModel().getFieldModel("BIRTH_DATE");
		dateFirstDrugFieldModel = patientDocumentation.getDatasetModel().getFieldModel("DATE_OF_FIRST_STUDY_DRUG");
		genderFieldModel = patientDocumentation.getDatasetModel().getFieldModel("GENDER");
		employmentFieldModel = patientDocumentation.getDatasetModel().getFieldModel("EMPLOYMENT");

		queryWorkflow = studyService.getStudy().getWorkflow("QUERY");
		openQueryState = queryWorkflow.getState("OPEN");
		protocolDeviationWorkflow = studyService.getStudy().getWorkflow("PROTOCOL_DEVIATION");
	}

	@Test
	@DisplayName("Field blocking validation works")
	public void testBlockingValidation() throws InvalidValueException, BadlyFormattedValue {
		final var dateOfBirth = fieldService.get(patientDocumentation, birthDateFieldModel);
		final var dateOfFirstStudyDrug = fieldService.get(patientDocumentation, dateFirstDrugFieldModel);

		// set a date of birth for the patient
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, dateOfBirth, "1979", context, TEST_RATIONALE);

		//date of first study drug must be greater or equals to date of birth
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, dateOfFirstStudyDrug, "Unknown.Unknown.Unknown", context, TEST_RATIONALE);

		// blocking validator should apply
		assertThrows(
			ValidatorException.class,
			() -> validationService.applyBlockingValidators(patient, Optional.empty(), patientDocumentation, dateOfFirstStudyDrug),
			"Blocking validators fail to apply"
		);
	}

	@Test
	@DisplayName("Field validation works")
	public void testFieldValidation() throws InvalidValueException, BadlyFormattedValue {
		//set empty value for date of first study drug
		final var dateOfFirstStudyDrugField = fieldService.get(patientDocumentation, dateFirstDrugFieldModel);
		closeValueWorkflows(dateOfFirstStudyDrugField);
		var context = createDatabaseActionContext();
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, dateOfFirstStudyDrugField, "", context, TEST_RATIONALE);
		validationService.validateField(patient, Optional.empty(), patientDocumentation, dateOfFirstStudyDrugField, context, TEST_RATIONALE);

		assertEquals(1, workflowStatusService.getAll(dateOfFirstStudyDrugField, openQueryState).size());
		closeValueWorkflows(dateOfFirstStudyDrugField);

		//set empty value on gender
		final var genderField = fieldService.get(patientDocumentation, genderFieldModel);
		closeValueWorkflows(genderField);
		context = createDatabaseActionContext();
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, genderField, "", context, TEST_RATIONALE);
		validationService.validateField(patient, Optional.empty(), patientDocumentation, genderField, context, TEST_RATIONALE);
		assertEquals("", genderField.getValue());

		assertEquals(1, workflowStatusService.getAll(genderField, openQueryState).size());
		closeValueWorkflows(genderField);

		//reset gender value
		context = createDatabaseActionContext();
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, genderField, "FEMALE", context, TEST_RATIONALE);

		//set empty value on employment
		final var employmentField = fieldService.get(patientDocumentation, employmentFieldModel);
		closeValueWorkflows(employmentField);
		context = createDatabaseActionContext();
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, employmentField, "", context, TEST_RATIONALE);
		validationService.validateField(patient, Optional.empty(), patientDocumentation, employmentField, context, TEST_RATIONALE);
		assertEquals("", employmentField.getValue());

		//retrieve newly created workflow status
		assertEquals(1, workflowStatusService.getAll(employmentField, openQueryState).size());

		final var employmentFirstQuery = workflowStatusService.getMostRecent(employmentField, openQueryState).orElseThrow();
		assertAll(
			() -> assertEquals("OPEN", employmentFirstQuery.getStateId()),
			() -> assertEquals("REQUIRED_WITH_QUERY", employmentFirstQuery.getValidatorId())
		);

		final var firstQueryTrails = workflowStatusDAOService.getAuditTrails(employmentFirstQuery, Optional.empty(), Optional.empty());
		assertAll(
			"Retrieve only newly created workflow status",
			() -> assertEquals(1, firstQueryTrails.size()),
			() -> assertEquals("OPEN", firstQueryTrails.last().getStateId()),
			() -> assertEquals("Field is required.", firstQueryTrails.last().getAuditContext())
		);

		//try to set the same wrong value, nothing should happen as value has not been changed
		context = createDatabaseActionContext();
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, employmentField, "", context, TEST_RATIONALE);
		assertEquals(1, workflowStatusService.getAll(employmentField, queryWorkflow).size());
		assertEquals(1, workflowStatusService.getAll(employmentField, openQueryState).size());

		//re-validating the field closes the currently open query and create a new one
		context = createDatabaseActionContext();
		validationService.validateField(patient, Optional.empty(), patientDocumentation, employmentField, context, TEST_RATIONALE);
		assertEquals(2, workflowStatusService.getAll(employmentField, queryWorkflow).size());
		assertEquals(1, workflowStatusService.getAll(employmentField, openQueryState).size());

		//set a different wrong value
		context = createDatabaseActionContext();
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, employmentField, "STUDENT", context, TEST_RATIONALE);

		//check updated workflow status
		context = createDatabaseActionContext();
		validationService.validateField(patient, Optional.empty(), patientDocumentation, employmentField, context, TEST_RATIONALE);
		assertEquals(3, workflowStatusService.getAll(employmentField, queryWorkflow).size());
		assertEquals(1, workflowStatusService.getAll(employmentField, openQueryState).size());

		final var employmentSecondQuery = workflowStatusService.getMostRecent(employmentField, openQueryState).orElseThrow();
		assertAll(
			() -> assertEquals("OPEN", employmentSecondQuery.getStateId()),
			() -> assertEquals("FEMALE_EMPLOYED", employmentSecondQuery.getValidatorId())
		);

		final var secondQueryTrails = workflowStatusDAOService.getAuditTrails(employmentSecondQuery, Optional.empty(), Optional.empty());
		assertAll(
			"Check updated workflow status",
			() -> assertEquals(1, secondQueryTrails.size()),
			() -> assertEquals("OPEN", secondQueryTrails.last().getStateId()),
			() -> assertEquals("Females must be employed.", secondQueryTrails.last().getAuditContext())
		);

		//check previous workflow status trail
		final var workflowStatusAuditTrails = workflowStatusDAOService.getAuditTrails(employmentFirstQuery, Optional.empty(), Optional.empty());

		assertEquals(2, workflowStatusAuditTrails.size());
		final var iwst = workflowStatusAuditTrails.iterator();
		var wst = iwst.next();
		assertEquals("OPEN", wst.getStateId());
		assertEquals("Field is required.", wst.getAuditContext());
		wst = iwst.next();
		assertEquals("CLOSED", wst.getStateId());
		assertEquals("Re-assessing due to value change", wst.getAuditContext());
	}

	@Test
	@DisplayName("Workflows triggered by field validation are modified correctly")
	public void testWorkflowsTriggeredByFieldValidation() throws InvalidValueException, BadlyFormattedValue {
		final var field = fieldService.get(patientDocumentation, birthDateFieldModel);

		assertEquals(0, workflowStatusService.getAll(field).size());

		//empty value is not allowed
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, field, "", context, TEST_RATIONALE);
		validationService.validateField(patient, Optional.empty(), patientDocumentation, field, context, TEST_RATIONALE);
		assertEquals("", field.getValue());

		//one workflow status has been created for workflow QUERY and status OPEN
		assertEquals(1, workflowStatusService.getAll(field).size());
		assertEquals(1, workflowStatusService.getAll(field, queryWorkflow).size());

		final var query = workflowStatusService.getAll(field, queryWorkflow).get(0);
		final var queryTrails = workflowStatusDAOService.getAuditTrails(query, Optional.empty(), Optional.empty());
		assertAll(
			"One workflow status has been created for workflow QUERY and status OPEN",
			() -> assertEquals("OPEN", query.getStateId()),
			() -> assertEquals("REQUIRED_WITH_QUERY", query.getValidatorId()),
			() -> assertEquals(1, queryTrails.size()),
			() -> assertEquals("OPEN", queryTrails.last().getStateId()),
			() -> assertEquals("Field is required.", queryTrails.last().getAuditContext())
		);

		//set an other wrong value which should trigger an other workflow (PROTOCOL_DEVIATION)
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, field, String.valueOf(ZonedDateTime.now().getYear()), context, TEST_RATIONALE);
		validationService.validateField(patient, Optional.empty(), patientDocumentation, field, context, TEST_RATIONALE);

		//an other workflow status has been created
		assertEquals(2, workflowStatusService.getAll(field).size());

		//the old validation workflow has been closed
		assertEquals("CLOSED", query.getStateId());

		//new workflow status has been created for workflow PROTOCOL_DEVIATION and status TO_REVIEW
		final var protocolDeviation = workflowStatusService.getAll(field, protocolDeviationWorkflow).get(0);
		final var protocolDeviationTrails = workflowStatusDAOService.getAuditTrails(protocolDeviation, Optional.empty(), Optional.empty());
		assertAll(
			"New workflow status has been created for workflow PROTOCOL_DEVIATION and status TO_REVIEW",
			() -> assertEquals("TO_REVIEW", protocolDeviation.getStateId()),
			() -> assertEquals("OLDER_THAN_18", protocolDeviation.getValidatorId()),
			() -> assertEquals(1, protocolDeviationTrails.size()),
			() -> assertEquals("TO_REVIEW", protocolDeviationTrails.last().getStateId()),
			() -> assertEquals("Field must be greater than 18 years.", protocolDeviationTrails.last().getAuditContext())
		);
	}

	private void closeValueWorkflows(final Field field) {
		final var context = createDatabaseActionContext();
		for(final var ws : workflowStatusService.getAll(field, queryWorkflow)) {
			final var family = workflowStatusService.createDataFamily(ws);
			workflowStatusService.updateState(family, ws, queryWorkflow.getState("CLOSED"), Collections.emptyMap(), context, "Close for tests");
		}
	}
}
