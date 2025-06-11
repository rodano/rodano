package ch.rodano.core.model.rules;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.rules.formula.FormulaParserService;
import ch.rodano.core.model.rules.formula.exception.UnableToCalculateFormulaException;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringTestConfiguration
public class FormulaParserServiceTest {
	@Autowired
	private StudyService studyService;

	@Autowired
	private FormulaParserService formulaParserService;

	//for dates, we cut off the nanoseconds in order to avoid the inclusion of the execution time in the date comparison
	public void assertDateEquals(final PartialDate expected, final PartialDate actual) {
		final ZonedDateTime expectedZdt = expected.toZonedDateTime().get();
		final ZonedDateTime actualZdt = actual.toZonedDateTime().get();
		assertEquals(expectedZdt.truncatedTo(ChronoUnit.SECONDS), actualZdt.truncatedTo(ChronoUnit.SECONDS), String.format("There is more than one minute between %s and %s", expected, actual));
	}

	@Test
	@DisplayName("Test formula parser without evaluations")
	public void test_without_evaluations() {
		assertAll(
			"Check simplest formula",
			() -> assertEquals(3d, formulaParserService.parse("=3")),
			() -> assertEquals("TOTO", formulaParserService.parse("=\"TOTO\""))
		);

		assertAll(
			"Check simple function parsing when formula is well formated",
			() -> assertDateEquals(PartialDate.now(), formulaParserService.parse("=TODAY()")),
			() -> assertEquals("TOTO", formulaParserService.parse("=UPPERCASE(\"toto\")")),
			() -> assertEquals(5d, formulaParserService.parse("=ABS(-5)")),
			() -> assertEquals(7.5d, formulaParserService.parse("=AVERAGE(5, 10)")),
			() -> assertEquals(0d, formulaParserService.parse("=AVERAGE(10, 5, -15)"))
		);

		assertAll(
			"Check advanced function parsing when formula is well formated",
			() -> assertDateEquals(PartialDate.now().plusYears(2), formulaParserService.parse("=ADD_YEARS(TODAY(), 2)")),
			() -> assertDateEquals(PartialDate.now().minusYears(61), formulaParserService.parse("=ADD_YEARS(TODAY(), -61)")),
			() -> assertDateEquals(PartialDate.now().plusYears(2).minusDays(10), formulaParserService.parse("=ADD_DURATION(TODAY(), 2, 0, -10, 0, 0, 0)")),
			() -> assertDateEquals(PartialDate.now().plusMonths(2).minusMinutes(10).plusHours(3), formulaParserService.parse("=ADD_DURATION(TODAY(), 0, 2, 0, 3, -10, 0)"))
		);

		assertAll(
			"Check function parsing when formula is not well formated",
			() -> assertThrows(UnableToCalculateFormulaException.class, () -> formulaParserService.parse("=TODAY(")),
			() -> assertThrows(UnableToCalculateFormulaException.class, () -> formulaParserService.parse("=ADD_YEARS(TODAY(), 5"))
		);

		assertAll(
			"Check function parsing when formula contains spaces",
			() -> assertEquals("TOTO", formulaParserService.parse("=   UPPERCASE(\"toto\")")),
			() -> assertEquals(10d, formulaParserService.parse("=MULTIPLY(2,      5)")),
			() -> assertEquals("  TOTO", formulaParserService.parse("=     UPPERCASE(\"  toto\")"))
		);

		assertAll(
			"Check conditional functions",
			//equals
			() -> assertEquals(true, formulaParserService.parse("=IS_EQUAL_TO(\"TUTU\", \"TUTU\")")),
			() -> assertEquals(true, formulaParserService.parse("=IS_EQUAL_TO(3, 3)")),
			() -> assertEquals(false, formulaParserService.parse("=IS_EQUAL_TO(\"TOTO\", \"TUTU\")")),
			() -> assertEquals(false, formulaParserService.parse("=IS_EQUAL_TO(3, 4)")),
			() -> assertEquals(false, formulaParserService.parse("=IS_EQUAL_TO(3, \"TUTU\")")),
			//if
			() -> assertEquals("TUTU", formulaParserService.parse("=IF(IS_BLANK(\"TOTO\"), \"TITI\", \"TUTU\")")),
			() -> assertEquals("TITI", formulaParserService.parse("=IF(IS_BLANK(\"\"), \"TITI\", \"TUTU\")")),
			() -> assertEquals(2d, formulaParserService.parse("=IF(IS_BLANK(\"\"), 2, 4)"))
		);

		assertAll(
			"Check string to number",
			() -> assertThrows(UnableToCalculateFormulaException.class, () -> formulaParserService.parse("=MULTIPLY(\"2\", 5)")),
			() -> assertEquals(10d, formulaParserService.parse("=MULTIPLY(STRING_TO_NUMBER(\"2\"), 5)"))
		);

		assertAll(
			"Check number to string",
			() -> assertEquals("510", formulaParserService.parse("=CONCAT(NUMBER_TO_STRING(5),NUMBER_TO_STRING(10))")),
			() -> assertEquals("5.510", formulaParserService.parse("=CONCAT(NUMBER_TO_STRING(5.50),NUMBER_TO_STRING(10))"))
		);

		assertAll(
			"Check combined formulas",
			() -> assertEquals(20d, formulaParserService.parse("=DIFFERENCE_IN_DAYS(TODAY(), ADD_DAYS(TODAY(), 20))")),
			() -> assertEquals(19d, formulaParserService.parse("=DIFFERENCE_IN_DAYS(CREATE_DATE(2020, 1, 1, 0, 0, 0), CREATE_DATE(2020, 1, 20, 0, 0, 0))")),
			() -> assertEquals(30d, formulaParserService.parse("=DIFFERENCE_IN_SECONDS(CREATE_DATE(2020, 1, 1, 0, 1, 20), CREATE_DATE(2020, 1, 1, 0, 0, 50))"))
		);

		//TODO this should be moved in the formula test
		assertAll(
			"Check number functions",
			//round
			() -> assertEquals(5d, formulaParserService.parse("=ROUND(5.2, 0)")),
			() -> assertEquals(-8.52d, formulaParserService.parse("=ROUND(-8.518, 2)")),
			() -> assertEquals(-1d, formulaParserService.parse("=ROUND(-1.001, 2)")),
			() -> assertEquals(-1.5d, formulaParserService.parse("=ROUND(-1.500, 1)")),
			//sum
			() -> assertEquals(5d, formulaParserService.parse("=SUM(5)")),
			() -> assertEquals(15d, formulaParserService.parse("=SUM(5, 10)")),
			() -> assertEquals(1.1d, formulaParserService.parse("=SUM(1.5, -2.2, 1.8)")),
			//multiply
			() -> assertEquals(-5d, formulaParserService.parse("=MULTIPLY(-5)")),
			() -> assertEquals(-1.5d, formulaParserService.parse("=MULTIPLY(1.5, -1)")),
			() -> assertEquals(0d, formulaParserService.parse("=MULTIPLY(1.5, -1, 0)")),
			//divide
			() -> assertEquals(2d, formulaParserService.parse("=DIVIDE(4, 2)")),
			() -> assertEquals(1.1d, formulaParserService.parse("=DIVIDE(4.4, 2, 2)")),
			() -> assertThrows(UnableToCalculateFormulaException.class, () -> formulaParserService.parse("=DIVIDE(2)")),
			//inverse
			() -> assertEquals(-2d, formulaParserService.parse("=INVERSE(-0.5d)")),
			() -> assertEquals(0.5d, formulaParserService.parse("=INVERSE(2)")),
			//module
			() -> assertEquals(2d, formulaParserService.parse("=MODULO(10, 4)")),
			() -> assertEquals(0d, formulaParserService.parse("=MODULO(100, 1)"))
		);

		//number operations are not supported, use functions instead
		/*assertAll("Check if the number operations are correct",
			() -> assertEquals(3.0, formulaParserService.parse("=3")),
			() -> assertEquals(5.0, formulaParserService.parse("=3+2")),
			() -> assertEquals(102.0, formulaParserService.parse("=10 0+2")),
			() -> assertEquals(12.0, formulaParserService.parse("=  7  +  5  ")),
			() -> assertEquals(2.0, formulaParserService.parse("=12/(4+2)")),
			() -> assertEquals(5.0, formulaParserService.parse("=500/((100/10)*(20/2))")));

		//string operations are not supported (and should never be)
		assertAll("Check if the string operations is correct",
			() -> assertEquals("102", formulaParserService.parse("=10+2")),
			() -> assertEquals("75", formulaParserService.parse("=  7  +  5  ")),
			() -> assertEquals("toto", formulaParserService.parse("=  to+  to")),
			() -> assertEquals("1toto2", formulaParserService.parse("=1+to+  to  + 2")));

		assertThrows(UnsupportedOperationException.class, () -> formulaParserService.parse("=tu-tu"));
		assertThrows(FormulaBadSyntaxException.class, () -> formulaParserService.parse("=3+-2"));*/
	}

	@Test
	@DisplayName("Test formula parser with evaluations")
	public void test_with_evaluations() throws Exception {
		//create dataset model
		final var datasetModel = new DatasetModel();
		datasetModel.setId("FORMULA_DOCUMENT");
		studyService.getStudy().getDatasetModels().add(datasetModel);

		//create dataset
		final var dataset = new Dataset();
		dataset.setDatasetModel(datasetModel);

		final var anyNumber = new FieldModel();
		anyNumber.setId("ANY_NUMBER");
		anyNumber.setType(FieldModelType.NUMBER);
		anyNumber.setDataType(OperandType.NUMBER);
		anyNumber.setMaxIntegerDigits(1);
		datasetModel.getFieldModels().add(anyNumber);

		//create field
		var field = new Field();
		field.setDatasetModel(datasetModel);
		field.setFieldModel(anyNumber);
		field.setDatasetFk(dataset.getPk());
		field.setValue("3");

		var datastate = new DataState(
			Collections.emptySet(), Collections.emptySet(), Collections.singleton(dataset), Collections.singleton(field), Collections.emptySet(), Collections.emptySet(), RulableEntity.FIELD
		);
		var evaluations = Collections.singletonMap("AN", datastate);

		assertEquals(3.0d, formulaParserService.parse("=AN:VALUE_NUMBER", evaluations));
		assertEquals(6.0d, formulaParserService.parse("=SUM(AN:VALUE_NUMBER, 3)", evaluations));

		//create attribute
		final var anyDate = new FieldModel();
		anyDate.setId("ANY_DATE");
		anyDate.setType(FieldModelType.DATE);
		anyDate.setDataType(OperandType.DATE);
		anyDate.setWithYears(true);
		anyDate.setWithMonths(true);
		anyDate.setWithDays(true);
		datasetModel.getFieldModels().add(anyDate);

		final var referenceDate = PartialDate.of(2010, 2, 5, 0, 0, 0);

		//create field
		field = new Field();
		field.setDatasetModel(datasetModel);
		field.setFieldModel(anyDate);
		field.setDatasetFk(dataset.getPk());
		field.setValue(anyDate.objectToString(referenceDate));

		datastate = new DataState(
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.singleton(dataset),
			Collections.singleton(field),
			Collections.emptySet(),
			Collections.emptySet(),
			RulableEntity.FIELD
		);
		evaluations = Collections.singletonMap("AD", datastate);

		final var finalEvaluations = evaluations;
		assertAll(
			"Check parsed dates",
			() -> assertEquals(referenceDate, formulaParserService.parse("=AD:VALUE_DATE", finalEvaluations)),
			() -> assertDateEquals(referenceDate.plusDays(120), formulaParserService.parse("=ADD_DAYS(AD:VALUE_DATE, 120)", finalEvaluations)),
			() -> assertDateEquals(referenceDate.plusYears(2), formulaParserService.parse("=ADD_YEARS(AD:VALUE_DATE, 2)", finalEvaluations)),
			() -> assertDateEquals(referenceDate.plusMonths(2).plusMinutes(2), formulaParserService.parse("=ADD_MONTHS(ADD_MINUTES(AD:VALUE_DATE, 2), 2)", finalEvaluations)),
			() -> assertDateEquals(referenceDate.minusMonths(2).plusMinutes(2), formulaParserService.parse("=ADD_MONTHS(ADD_MINUTES(AD:VALUE_DATE, 2), -2)", finalEvaluations)),
			() -> assertDateEquals(referenceDate.withDayOfMonth(1), formulaParserService.parse("=MONTH_OF_DATE(AD:VALUE_DATE)", finalEvaluations)),
			() -> assertDateEquals(referenceDate.withDayOfMonth(1).plusYears(2), formulaParserService.parse("=ADD_YEARS(MONTH_OF_DATE(AD:VALUE_DATE), 2)", finalEvaluations))
		);

		//create attributes
		final var weightFieldModel = new FieldModel();
		weightFieldModel.setId("WEIGHT");
		weightFieldModel.setType(FieldModelType.NUMBER);
		weightFieldModel.setDataType(OperandType.NUMBER);
		weightFieldModel.setMaxIntegerDigits(1);
		datasetModel.getFieldModels().add(weightFieldModel);

		final var heightFieldModel = new FieldModel();
		heightFieldModel.setId("HEIGHT");
		heightFieldModel.setType(FieldModelType.NUMBER);
		heightFieldModel.setDataType(OperandType.NUMBER);
		heightFieldModel.setMaxIntegerDigits(1);
		datasetModel.getFieldModels().add(heightFieldModel);

		final var weight = new Field();
		weight.setDatasetFk(dataset.getPk());
		weight.setDatasetModel(datasetModel);
		weight.setFieldModel(weightFieldModel);
		weight.setValue("20");

		final var height = new Field();
		height.setDatasetFk(dataset.getPk());
		height.setDatasetModel(datasetModel);
		height.setFieldModel(heightFieldModel);
		height.setValue("100");

		final var weightState = new DataState(
			Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.singleton(weight), Collections.emptySet(), Collections.emptySet(), RulableEntity.FIELD
		);
		final var heightState = new DataState(
			Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.singleton(height), Collections.emptySet(), Collections.emptySet(), RulableEntity.FIELD
		);
		evaluations = Map.of("WEIGHT", weightState, "HEIGHT", heightState);

		assertEquals(20.0d, formulaParserService.parse("=BMI(WEIGHT:VALUE_NUMBER, HEIGHT:VALUE_NUMBER)", evaluations));
	}
}
