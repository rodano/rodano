package ch.rodano.core.services.bll.field;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Field service")
@SpringTestConfiguration
@Transactional
public class FieldServiceTest extends DatabaseTest {

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private EventService eventService;

	@Autowired
	private FieldService fieldService;

	@Autowired
	private FieldDAOService fieldDAOService;

	@Autowired
	private WorkflowStatusService workflowStatusService;

	@Autowired
	private TestHelperService testHelperService;

	private DatasetModel addressDatasetModel;
	private DatasetModel telephoneDatasetModel;
	private DatasetModel patientDatasetModel;
	private DatasetModel visitDatasetModel;
	private FieldModel addressFieldModel;
	private FieldModel dateOfPhoneCallFieldModel;
	private FieldModel callDurationFieldModel;
	private FieldModel genderFieldModel;
	private FieldModel dateOfFirstStudyDrugFieldModel;
	private FieldModel educationFieldModel;
	private FieldModel withdrawalFieldModel;

	private Scope center;
	private Scope patient;

	@BeforeEach
	public void initTest() throws IOException {
		studyService.reload();

		addressDatasetModel = studyService.getStudy().getDatasetModel("ADDRESS");
		telephoneDatasetModel = studyService.getStudy().getDatasetModel("TELEPHONE_DOCUMENTATION");
		patientDatasetModel = studyService.getStudy().getDatasetModel("PATIENT_DOCUMENTATION");
		visitDatasetModel = studyService.getStudy().getDatasetModel("VISIT_DOCUMENTATION");
		addressFieldModel = addressDatasetModel.getFieldModel("ADDRESS_1");
		dateOfPhoneCallFieldModel = telephoneDatasetModel.getFieldModel("DATE_OF_PHONE_CALL");
		callDurationFieldModel = telephoneDatasetModel.getFieldModel("CALL_DURATION");
		genderFieldModel = patientDatasetModel.getFieldModel("GENDER");
		dateOfFirstStudyDrugFieldModel = patientDatasetModel.getFieldModel("DATE_OF_FIRST_STUDY_DRUG");
		educationFieldModel = patientDatasetModel.getFieldModel("EDUCATION");
		withdrawalFieldModel = visitDatasetModel.getFieldModel("WITHDRAWAL");

		center = testHelperService.createCenter(context);
		patient = testHelperService.createPatient(center, context);
	}

	@Test
	@DisplayName("Test simple string value")
	public void testStringValue() throws InvalidValueException, BadlyFormattedValue {
		final var dataset = datasetService.get(center, addressDatasetModel);
		final var field = fieldService.get(dataset, addressFieldModel);

		fieldService.updateValue(center, Optional.empty(), dataset, field, "4, chemin de la tour de champel", context, TEST_RATIONALE);
		assertEquals("4, chemin de la tour de champel", field.getValue());

		//symbols are valid strings
		fieldService.updateValue(center, Optional.empty(), dataset, field, "$%^+-*/?!", context, TEST_RATIONALE);
		assertEquals("$%^+-*/?!", field.getValue(), "Symbols are valid strings");
	}

	@Test
	@DisplayName("Test that the max string length defined in the field model is respected")
	public void fieldModelMaxLengthTest() throws InvalidValueException, BadlyFormattedValue {
		final var dataset = datasetService.get(center, addressDatasetModel);
		final var field = fieldService.get(dataset, addressFieldModel);

		//reset valid strings
		fieldService.updateValue(center, Optional.empty(), dataset, field, "champel", context, TEST_RATIONALE);

		//change attribute max length
		field.getFieldModel().setMaxLength(10);

		//long value should not be allowed
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(center, Optional.empty(), dataset, field, "4, chemin de la tour de champel", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("champel", field.getValue(), "Field has not been changed");

	}

	@DisplayName("Invalid date values are rejected")
	@ParameterizedTest
	@ValueSource(strings = { "01.13.2012", "01.02.1842", "01.02.2042" })
	public void testInvalidValues(final String value) throws InvalidValueException, BadlyFormattedValue {
		final var visit = createTelephoneVisit(patient);
		final var dataset = datasetService.get(visit, telephoneDatasetModel);
		final var field = fieldService.get(dataset, dateOfPhoneCallFieldModel);

		dateOfPhoneCallFieldModel.getValidatorIds().remove("AFTER_LAST_VISIT");

		fieldService.updateValue(patient, Optional.of(visit), dataset, field, "01.02.2012", context, TEST_RATIONALE);

		// value is recognized as invalid
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.of(visit), dataset, field, value, context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("01.02.2012", field.getValue(), "Field has not been changed");
	}

	@DisplayName("Empty value is allowed")
	@Test
	public void allowEmptyValue() throws InvalidValueException, BadlyFormattedValue {
		final var visit = createTelephoneVisit(patient);
		final var dataset = datasetService.get(visit, telephoneDatasetModel);
		final var field = fieldService.get(dataset, dateOfPhoneCallFieldModel);

		fieldService.updateValue(patient, Optional.of(visit), dataset, field, "", context, TEST_RATIONALE);
		assertEquals("", field.getValue());
	}

	@DisplayName("Null value is rejected")
	@Test
	public void nullValueIsForbidden() {
		final var visit = createTelephoneVisit(patient);
		final var dataset = datasetService.get(visit, telephoneDatasetModel);
		final var field = fieldService.get(dataset, dateOfPhoneCallFieldModel);

		assertThrows(
			IllegalArgumentException.class,
			() -> fieldService.updateValue(patient, Optional.of(visit), dataset, field, null, context, TEST_RATIONALE),
			"Null value is not allowed"
		);
	}

	@DisplayName("Test future date validation")
	@Test
	public void testFutureDates() throws InvalidValueException, BadlyFormattedValue {
		final var visit = createTelephoneVisit(patient);
		final var dataset = datasetService.get(visit, telephoneDatasetModel);
		final var field = fieldService.get(dataset, dateOfPhoneCallFieldModel);

		final var study = studyService.getStudy();

		//disallow date in future
		dateOfPhoneCallFieldModel.setAllowDateInFuture(false);
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.of(visit), dataset, field, "01.02.2030", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);

		//allow date in future
		study.getDatasetModel("TELEPHONE_DOCUMENTATION").getFieldModel("DATE_OF_PHONE_CALL").setAllowDateInFuture(true);
		//01.02.2030 is now allowed
		fieldService.updateValue(patient, Optional.of(visit), dataset, field, "01.02.2030", context, TEST_RATIONALE);
		assertEquals("01.02.2030", field.getValue());
	}

	@Test
	@DisplayName("Test time field model value")
	public void testTimeFieldModelValue() throws InvalidValueException, BadlyFormattedValue {
		final var dataset = datasetService.get(createTelephoneVisit(patient), telephoneDatasetModel);
		final var field = fieldService.get(dataset, callDurationFieldModel);

		fieldService.updateValue(patient, Optional.empty(), dataset, field, "12:34", context, TEST_RATIONALE);

		//25:35 is not a valid date
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "25:35", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("12:34", field.getValue(), "Field has not been changed");

		//14:35 is a valid hour
		fieldService.updateValue(patient, Optional.empty(), dataset, field, "14:35", context, TEST_RATIONALE);
		assertEquals("14:35", field.getValue());

		//15:36:42 does not have the good format
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "15:36:42", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("14:35", field.getValue(), "Field has not been changed");
	}

	@Test
	@DisplayName("Test select field model value")
	public void testSelectFieldModelValue() throws InvalidValueException, BadlyFormattedValue {
		final var dataset = datasetService.get(patient, patientDatasetModel);
		final var field = fieldService.get(dataset, genderFieldModel);

		fieldService.updateValue(patient, Optional.empty(), dataset, field, "MALE", context, TEST_RATIONALE);

		//toto is not a possible value
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "toto", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("MALE", field.getValue(), "Field has not been changed");

		//empty value is allowed
		fieldService.updateValue(patient, Optional.empty(), dataset, field, "", context, TEST_RATIONALE);
		assertEquals("", field.getValue());

		assertThrows(
			IllegalArgumentException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, null, context, TEST_RATIONALE),
			"Null value is not allowed"
		);
	}

	@Test
	@DisplayName("Test date select field model value")
	public void testDateSelectFieldModelValue() throws InvalidValueException, BadlyFormattedValue {
		final var dataset = datasetService.get(patient, patientDatasetModel);
		final var field = fieldService.get(dataset, dateOfFirstStudyDrugFieldModel);

		fieldService.updateValue(patient, Optional.empty(), dataset, field, "01.01.2001", context, TEST_RATIONALE);

		//01..2001 should not be a valid date
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "01..2001", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("01.01.2001", field.getValue(), "Field has not been changed");

		//..2010 should not be a valid date
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "..2010", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("01.01.2001", field.getValue(), "Field has not been changed");

		//14.. should not be a valid date
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "14..", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("01.01.2001", field.getValue(), "Field has not been changed");

		//... should not be a valid date
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "...", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("01.01.2001", field.getValue(), "Field has not been changed");

		//empty value is allowed
		fieldService.updateValue(patient, Optional.empty(), dataset, field, "", context, TEST_RATIONALE);
		assertEquals("", field.getValue());

		//Unknown.02.2008 should be a valid date
		fieldService.updateValue(patient, Optional.empty(), dataset, field, "Unknown.02.2008", context, TEST_RATIONALE);
		assertEquals("Unknown.02.2008", field.getValue());

		//Unknown.Unknown.Unknown should be a valid date
		fieldService.updateValue(patient, Optional.empty(), dataset, field, "Unknown.Unknown.Unknown", context, TEST_RATIONALE);
		assertEquals("Unknown.Unknown.Unknown", field.getValue());

		//14.Unknown.Unknown should not be a valid date
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "14.Unknown.Unknown", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("Unknown.Unknown.Unknown", field.getValue(), "Field has not been changed");

		fieldService.updateValue(patient, Optional.empty(), dataset, field, "12.06.2010", context, TEST_RATIONALE);
		assertEquals("12.06.2010", field.getValue());

		//12.13.2010 should not be a valid date
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "12.13.2010", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("12.06.2010", field.getValue(), "Field has not been changed");

		//31.04.2010 should not be a valid date
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), dataset, field, "31.04.2010", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("12.06.2010", field.getValue(), "Field has not been changed");
	}

	@Test
	@DisplayName("Field audit trail is recorded correctly")
	public void testAuditTrail() throws InvalidValueException, BadlyFormattedValue {
		final var dataset = datasetService.get(patient, patientDatasetModel);
		final var field = fieldService.get(dataset, educationFieldModel);

		// save a couple of values
		fieldService.updateValue(patient, Optional.empty(), dataset, field, "COLLEGE", context, "2");
		fieldService.updateValue(patient, Optional.empty(), dataset, field, "UNIVERSITY", context, "3");

		// check that the history of the field is recorded correctly
		final var auditTrails = fieldDAOService.getAuditTrails(field, Optional.empty(), Optional.empty());
		assertEquals(3, auditTrails.size());

		final var firstTrail = auditTrails.pollFirst();
		assertNull(firstTrail.getValue());
		final var secondTrail = auditTrails.pollFirst();
		assertEquals("COLLEGE", secondTrail.getValue());
		assertEquals("2", secondTrail.getAuditContext());
		final var thirdTrail = auditTrails.pollFirst();
		assertEquals("UNIVERSITY", thirdTrail.getValue());
		assertEquals(Actor.SYSTEM_USERNAME, thirdTrail.getAuditActor());
		assertEquals("3", thirdTrail.getAuditContext());
	}

	@Test
	@DisplayName("Past field value function works")
	public void testPastFieldValueFunction() throws InvalidValueException, BadlyFormattedValue, InterruptedException {
		final var dataset = datasetService.get(patient, patientDatasetModel);
		final var field = fieldService.get(dataset, educationFieldModel);

		// set a field value
		fieldService.updateValue(patient, Optional.empty(), dataset, field, "COLLEGE", createDatabaseActionContext(), TEST_RATIONALE);
		final var timestamp = ZonedDateTime.now();
		final var oldPastValue = field.getValue();

		// wait a bit, otherwise the save just goes too fast
		Thread.sleep(100);
		// set another field value
		fieldService.updateValue(patient, Optional.empty(), dataset, field, "UNIVERSITY", createDatabaseActionContext(), TEST_RATIONALE);

		// get the past value
		final var newPastValue = fieldService.getLatestValue(field, Optional.of(timestamp));
		assertEquals(oldPastValue, newPastValue);
	}

	@Test
	@DisplayName("Field value change triggers rule execution")
	public void testFieldRuleExecution() throws InvalidValueException, BadlyFormattedValue {
		//activate visit 6
		final var patientStatusWorkflow = studyService.getStudy().getWorkflow("PATIENT_STATUS");
		final var event = patient.getScopeModel().getEventModel("VISIT_6");
		final var visit = eventService.get(patient, event, 0);

		eventService.updateDate(patient, visit, ZonedDateTime.now(), context, TEST_RATIONALE);
		final var dataset = datasetService.get(visit, visitDatasetModel);
		final var field = fieldService.get(dataset, withdrawalFieldModel);

		final var patientStatus = workflowStatusService.getAll(patient, patientStatusWorkflow).get(0);

		assertEquals("REGISTERED", patientStatus.getStateId());
		fieldService.updateValue(patient, Optional.of(visit), dataset, field, "Y", context, TEST_RATIONALE);
		assertEquals("WITHDRAWN", patientStatus.getStateId());
		fieldService.updateValue(patient, Optional.of(visit), dataset, field, "N", context, TEST_RATIONALE);
		assertEquals("ONGOING", patientStatus.getStateId());
	}

	private Event createTelephoneVisit(final Scope patient) {
		final var event = patient.getScopeModel().getEventModel("TELEPHONE_VISIT");
		return eventService.create(patient, event, context, "Test");
	}
}
