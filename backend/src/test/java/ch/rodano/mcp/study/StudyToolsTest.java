package ch.rodano.mcp.study;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.ACL;
import ch.rodano.core.utils.RightsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StudyToolsTest {
	@AfterEach
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void rejectsMissingAuthentication() {
		final var studyTools = new StudyTools(null, null);
		assertThrows(AccessDeniedException.class, studyTools::describeStudy);
	}

	@Test
	public void rejectsNonRobotPrincipal() {
		final var studyTools = new StudyTools(null, null);
		final var authentication = new UsernamePasswordAuthenticationToken("user", null);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		assertThrows(AccessDeniedException.class, studyTools::describeStudy);
	}

	@Test
	public void returnsOnlyReadableModels() {
		final var robot = new Robot();
		final var authentication = new UsernamePasswordAuthenticationToken(robot, null);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		final var readableScopeModel = scopeModel("SITE");
		final var hiddenScopeModel = scopeModel("SUBJECT");
		final var readableDatasetModel = datasetModel("DEMOGRAPHICS");
		final var hiddenDatasetModel = datasetModel("TREATMENT");
		final var scopeModels = sortedScopeModels(readableScopeModel, hiddenScopeModel);
		final var datasetModels = sortedDatasetModels(readableDatasetModel, hiddenDatasetModel);

		final var study = mock(Study.class);
		when(study.getId()).thenReturn("TEST");
		when(study.getDefaultLocalizedShortname()).thenReturn("Test study");
		when(study.getDefaultLanguageId()).thenReturn("en");
		when(study.getScopeModels()).thenReturn(scopeModels);
		when(study.getDatasetModels()).thenReturn(datasetModels);

		final var acl = mock(ACL.class);
		when(acl.hasRight(readableScopeModel, Rights.READ)).thenReturn(true);
		when(acl.hasRight(readableDatasetModel, Rights.READ)).thenReturn(true);

		final var studyService = mock(StudyService.class);
		when(studyService.getStudy()).thenReturn(study);
		final var rightsService = mock(RightsService.class);
		when(rightsService.getACL(robot)).thenReturn(acl);

		final var summary = new StudyTools(studyService, rightsService).describeStudy();

		assertEquals("TEST", summary.id());
		assertEquals("Test study", summary.name());
		assertEquals("en", summary.defaultLanguageId());
		assertEquals(java.util.List.of("SITE"), summary.scopeModelIds());
		assertEquals(java.util.List.of("DEMOGRAPHICS"), summary.datasetModelIds());
	}

	private ScopeModel scopeModel(final String id) {
		final var scopeModel = mock(ScopeModel.class);
		when(scopeModel.getId()).thenReturn(id);
		return scopeModel;
	}

	private DatasetModel datasetModel(final String id) {
		final var datasetModel = mock(DatasetModel.class);
		when(datasetModel.getId()).thenReturn(id);
		return datasetModel;
	}

	private SortedSet<ScopeModel> sortedScopeModels(final ScopeModel... scopeModels) {
		final SortedSet<ScopeModel> models = new TreeSet<>(Comparator.comparing(ScopeModel::getId));
		models.addAll(java.util.List.of(scopeModels));
		return models;
	}

	private SortedSet<DatasetModel> sortedDatasetModels(final DatasetModel... datasetModels) {
		final SortedSet<DatasetModel> models = new TreeSet<>(Comparator.comparing(DatasetModel::getId));
		models.addAll(java.util.List.of(datasetModels));
		return models;
	}
}
