package ch.rodano.core.model;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.configuration.services.node.NodeCopyService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@SpringTestConfiguration
public class NodeCopyServiceTest {
	@Autowired
	private StudyService studyService;

	@Test
	@DisplayName("Copy nodes")
	public void copyNodes() throws NoSuchFieldException, IllegalAccessException, IOException {
		final var study = studyService.getStudy();

		//one level node
		final var feature = study.getFeature("ADMIN");
		final var featureCopy = NodeCopyService.copy(feature, "ADMIN_2");

		assertEquals("ADMIN", feature.getId());
		assertEquals("ADMIN_2", featureCopy.getId());
		assertNotSame(feature, featureCopy);

		//two levels node
		final var datasetModel = study.getDatasetModel("ADDRESS");
		final var fieldModels = datasetModel.getFieldModels();

		final var datasetModelCopy = NodeCopyService.copy(datasetModel, "ADDRESS_2");
		final var fieldModelsCopy = datasetModelCopy.getFieldModels();

		assertEquals("ADDRESS", datasetModel.getId());
		assertEquals("ADDRESS_2", datasetModelCopy.getId());
		assertNotSame(datasetModel, datasetModelCopy);
		assertEquals(fieldModelsCopy.size(), fieldModels.size());

		//two levels node
		final var widget = study.getWorkflowWidget("VISITS_TO_CLOSE");
		final var columns = widget.getColumns();

		final var widgetCopy = NodeCopyService.copy(widget, "VISITS_TO_CLOSE_2");
		final var columnsCopy = widgetCopy.getColumns();

		assertEquals("VISITS_TO_CLOSE", widget.getId());
		assertEquals("VISITS_TO_CLOSE_2", widgetCopy.getId());
		assertNotSame(widget, widgetCopy);
		assertEquals(columnsCopy.size(), columns.size());
		for(var i = 0; i < columns.size(); i++) {
			assertEquals(columnsCopy.get(i).getId(), columns.get(i).getId());
			assertNotSame(columns.get(i), columnsCopy.get(i));
		}
	}
}
