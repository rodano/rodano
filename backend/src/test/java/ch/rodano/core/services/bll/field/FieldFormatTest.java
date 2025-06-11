package ch.rodano.core.services.bll.field;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Field model format")
@SpringTestConfiguration
@Transactional
public class FieldFormatTest extends DatabaseTest {

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private FieldService fieldService;

	@Autowired
	private TestHelperService testHelperService;

	private Scope center;
	private Scope patient;
	private Dataset patientDocumentation;
	private FieldModel horsepowerFieldModel;

	//TODO create a dedicated field model that is all fresh instead of taking an already existing attribute and adjusting it in each test

	@BeforeEach
	public void initTest() {
		center = testHelperService.createCenter(context);
		patient = testHelperService.createPatient(center, context);
		patientDocumentation = datasetService.get(patient, studyService.getStudy().getDatasetModel("PATIENT_DOCUMENTATION"));
		horsepowerFieldModel = patientDocumentation.getDatasetModel().getFieldModel("HORSEPOWER");
	}

	@Test
	@DisplayName("Test integer field model format")
	public void testIntegerFieldModelFormat() throws InvalidValueException, BadlyFormattedValue {
		final var horsepower = fieldService.get(patientDocumentation, horsepowerFieldModel);
		final var horsePowerFieldModel = horsepower.getFieldModel();

		//remove matcher
		horsePowerFieldModel.setMatcher(null);
		//change formatter to only allow integers
		horsePowerFieldModel.setMaxIntegerDigits(3);
		horsePowerFieldModel.setMaxDecimalDigits(0);

		//regular integer is accepted by integer format
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "125", context, TEST_RATIONALE);
		assertEquals("125", horsepower.getValue());

		//extra digits are allowed
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "1100", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("125", horsepower.getValue(), "Field has not been changed");

		//useless precision is not allowed
		assertThrows(
			BadlyFormattedValue.class,
			() -> fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "00125", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("125", horsepower.getValue(), "Field has not been changed");

		//125.2 should not be a valid number as formatter does not allow decimal numbers
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "125.2", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("125", horsepower.getValue(), "Field has not been changed");

		//125,2 should not be a valid number
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "125,2", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("125", horsepower.getValue(), "Field has not been changed");

		//. should not be a valid number
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, ".", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("125", horsepower.getValue(), "Field has not been changed");

		//empty value is accepted
		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "", context, TEST_RATIONALE);
		assertEquals("", horsepower.getValue());

		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "125", context, TEST_RATIONALE);
		assertEquals("125", horsepower.getValue());

		assertThrows(
			IllegalArgumentException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, null, context, TEST_RATIONALE),
			"Null value is not allowed"
		);

		// a string should not be a valid number
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "test", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("125", horsepower.getValue(), "Field has not been changed");
	}

	@Test
	@DisplayName("Test decimal field model format")
	public void testDecimalFieldModelFormat() throws InvalidValueException, BadlyFormattedValue {
		final var horsepower = fieldService.get(patientDocumentation, horsepowerFieldModel);
		final var horsePowerFieldModel = horsepower.getFieldModel();

		//remove matcher
		horsePowerFieldModel.setMatcher(null);
		//change formatter to allow decimal numbers
		horsePowerFieldModel.setMaxIntegerDigits(3);
		horsePowerFieldModel.setMaxDecimalDigits(1);

		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "125", context, TEST_RATIONALE);
		assertEquals("125", horsepower.getValue());

		fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "125.2", context, TEST_RATIONALE);
		assertEquals("125.2", horsepower.getValue());

		//125,2 should not be a valid number
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "125,2", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("125.2", horsepower.getValue(), "Field has not been changed");

		//125.25 should not be a valid number as formatter does not allow more than one decimal digit
		assertThrows(
			InvalidValueException.class,
			() -> fieldService.updateValue(patient, Optional.empty(), patientDocumentation, horsepower, "125.25", context, TEST_RATIONALE),
			"Value must not have been accepted"
		);
		//field has not been changed
		assertEquals("125.2", horsepower.getValue(), "Field has not been changed");
	}
}
