package ch.rodano.core.services.bll.export;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVReader;

import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.export.extract.ExtractService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class ExtractServiceTest extends DatabaseTest {

	@Autowired
	private ExtractService extractService;

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private FieldService fieldService;

	@Autowired
	private TestHelperService testHelperService;

	private void updateValue(final Scope patient, final String fieldModelId, final String value) throws Exception {
		final var dataset = datasetService.get(patient, studyService.getStudy().getDatasetModel("PATIENT_DOCUMENTATION"));
		final var field = fieldService.get(dataset, dataset.getDatasetModel().getFieldModel(fieldModelId));
		fieldService.updateValue(patient, Optional.empty(), dataset, field, value, context, TEST_RATIONALE);
	}

	@Test
	@DisplayName("Extract contains one line per dataset with each field value in its own column")
	public void testExtractPivotsFields() throws Exception {
		testHelperService.createPatient(testHelperService.createCenter(context), context);
		final var center = testHelperService.createCenter(context);
		final var firstPatient = testHelperService.createPatient(center, context);
		final var secondPatient = testHelperService.createPatient(center, context);

		updateValue(firstPatient, "EYES_COLOR", "Blue");
		updateValue(secondPatient, "HORSEPOWER", "125");

		final var datasetModel = studyService.getStudy().getDatasetModel("PATIENT_DOCUMENTATION");
		final var out = new ByteArrayOutputStream();
		extractService.getDataExtract(out, datasetModel, "en", List.of(center), true);

		final List<String[]> lines;
		try(var reader = new CSVReader(new StringReader(out.toString()))) {
			lines = reader.readAll();
		}

		//the patient of the other center must not be exported
		assertEquals(3, lines.size());

		final var header = Arrays.asList(lines.getFirst());
		final var codeIndex = header.indexOf(firstPatient.getScopeModel().getId().toUpperCase());
		final var eyesColorIndex = header.indexOf(datasetModel.getFieldModel("EYES_COLOR").getExportColumnLabel().toUpperCase());
		final var horsepowerIndex = header.indexOf(datasetModel.getFieldModel("HORSEPOWER").getExportColumnLabel().toUpperCase());

		final var firstLine = lines.stream().filter(l -> l[codeIndex].equals(firstPatient.getCode())).findFirst().orElseThrow();
		assertEquals("Blue", firstLine[eyesColorIndex]);
		assertFalse(firstLine[eyesColorIndex + 1].isEmpty(), "The modification date must follow the value");
		assertTrue(firstLine[horsepowerIndex].isEmpty());

		final var secondLine = lines.stream().filter(l -> l[codeIndex].equals(secondPatient.getCode())).findFirst().orElseThrow();
		assertTrue(secondLine[eyesColorIndex].isEmpty());
		assertEquals("125", secondLine[horsepowerIndex]);
	}
}
