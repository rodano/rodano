package ch.rodano.configuration.services.study;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.configuration.builder.StudyBuilder;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.test.SpringTestConfiguration;
import tools.jackson.databind.json.JsonMapper;

@SpringTestConfiguration
public class StudySerializationTest {
	@Autowired
	private JsonMapper mapper;

	@Test
	@DisplayName("Study is serializable")
	public void studyIsSerializable() {
		final var study = StudyBuilder.buildStudy().getStudy();

		final var studySerialized = mapper.writeValueAsString(study);
		assertTrue(StringUtils.isNotBlank(studySerialized));
	}

	@Test
	@DisplayName("Study is deserializable")
	public void studyIsDeserializable() {
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
