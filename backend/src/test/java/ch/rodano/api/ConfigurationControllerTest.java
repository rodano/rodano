package ch.rodano.api;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import ch.rodano.api.config.EventModelDTO;
import ch.rodano.api.config.FieldModelDTO;
import ch.rodano.api.config.MenuDTO;
import ch.rodano.api.config.PublicStudyDTO;
import ch.rodano.api.config.StudyDTO;
import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringTestConfiguration
public class ConfigurationControllerTest extends ControllerTest {
	@Test
	@DisplayName("Public study endpoint works")
	void getPublicStudy() {
		final var publicStudy = executeGetAndReturnBody("/config/public-study", PublicStudyDTO.class);
		assertNotNull(publicStudy);
	}

	@Test
	@DisplayName("Study endpoint works")
	void getStudy() {
		authenticate(investigatorOnStudyEmail);
		final var study = executeGetAndReturnBody("/config/study", StudyDTO.class);
		assertNotNull(study);
	}

	@Test
	@DisplayName("Menu configuration endpoint works")
	void getMenus() {
		authenticate(investigatorOnStudyEmail);
		final var result = executeGetAndReturnBody("/config/menu", new ParameterizedTypeReference<List<MenuDTO>>() {});
		assertNotNull(result);
	}

	@Test
	@DisplayName("Searchable field models endpoint works")
	void getSearchableFieldModels() {
		authenticate(investigatorOnStudyEmail);
		final var result = executeGetAndReturnBody("/config/searchable-field-models", new ParameterizedTypeReference<List<FieldModelDTO>>() {});
		assertNotNull(result);
	}

	@Test
	@DisplayName("Event model configuration endpoint works")
	void getEventModels() {
		authenticate(adminOnStudyEmail);
		final var result = executeGetAndReturnBody("/config/event-models", new ParameterizedTypeReference<List<EventModelDTO>>() {});
		assertNotNull(result);
	}

	@Test
	@DisplayName("Field model configuration endpoint works")
	void getFieldModels() {
		authenticate(adminOnStudyEmail);
		final var result = executeGetAndReturnBody("/config/field-models", new ParameterizedTypeReference<List<FieldModelDTO>>() {});
		assertNotNull(result);
	}

	@Test
	@DisplayName("Workflow configuration works")
	void getWorkflows() {
		authenticate(adminOnStudyEmail);
		final var result = executeGetAndReturnBody("/config/workflows", new ParameterizedTypeReference<List<WorkflowDTO>>() {});
		assertNotNull(result);
	}
}
