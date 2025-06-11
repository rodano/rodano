package ch.rodano.configuration.services.study;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.rodano.configuration.builder.StudyBuilder;
import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.study.Study;

public class StudySerializationTest {
	private static ObjectMapper mapper;

	@BeforeAll
	public static void initialize() {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
	}

	@Test
	@DisplayName("Study is serializable")
	public void studyIsSerializable() throws JsonProcessingException {
		final var study = StudyBuilder.buildStudy().getStudy();

		final var studySerialized = mapper.writeValueAsString(study);
		assertTrue(StringUtils.isNotBlank(studySerialized));
	}

	@Test
	@DisplayName("Study is deserializable")
	public void studyIsDeserializable() throws IOException, NoNodeException {
		final var originalStudy = StudyBuilder.buildStudy().getStudy();
		final var studySerialized = mapper.writeValueAsString(originalStudy);

		final var study = mapper.readValue(studySerialized, Study.class);

		assertNotNull(study);
		assertEquals(originalStudy.getId(), study.getId());

		//dataset model
		assertEquals(originalStudy.getDatasetModels().size(), study.getDatasetModels().size());

		final var document = originalStudy.getDatasetModels().first();
		final var newDocument = study.getDatasetModel(document.getId());

		assertEquals(newDocument.getFieldModels().size(), document.getFieldModels().size());

		//form model
		assertEquals(originalStudy.getFormModels().size(), study.getFormModels().size());

		final var formModel = originalStudy.getFormModels().first();
		final var newFormModel = study.getFormModel(formModel.getId());

		assertEquals(newFormModel.getLayouts().size(), formModel.getLayouts().size());
	}
}
