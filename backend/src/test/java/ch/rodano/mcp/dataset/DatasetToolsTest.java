package ch.rodano.mcp.dataset;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.export.extract.DataExtract;
import ch.rodano.core.services.bll.export.extract.DataExtractRow;
import ch.rodano.core.services.bll.export.extract.ExtractService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.unitofwork.UnitOfWorkService;
import ch.rodano.core.utils.ACL;
import ch.rodano.core.utils.RightsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DatasetToolsTest {
	@AfterEach
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void describesOnlyExportableFields() {
		final var fixture = fixture();
		final var exportableField = fieldModel("AGE", true);
		final var hiddenField = fieldModel("INTERNAL", false);
		when(fixture.datasetModel().getFieldModelsExportables()).thenReturn(List.of(exportableField));
		when(fixture.datasetModel().getFieldModels()).thenReturn(List.of(exportableField, hiddenField));

		final var description = fixture.tools().describeDataset("DEMOGRAPHICS");

		assertEquals("DEMOGRAPHICS", description.id());
		assertEquals(List.of("AGE"), description.fields().stream().map(DatasetFieldDescription::id).toList());
	}

	@Test
	public void rejectsQueryAboveMaximum() {
		final var tools = new DatasetTools(null, null, null, null, null);
		assertThrows(IllegalArgumentException.class, () -> tools.queryDataset("DEMOGRAPHICS", 101));
	}

	@Test
	public void queriesOnlyScopesWhoseProfileGrantsBothRights() {
		final var fixture = fixture();
		final var allowedProfile = mock(Profile.class);
		final var datasetDeniedProfile = mock(Profile.class);
		final var allowedScope = mock(Scope.class);
		final var deniedScope = mock(Scope.class);
		when(allowedProfile.hasRight(FeatureStatic.EXPORT)).thenReturn(true);
		when(allowedProfile.hasRight(fixture.datasetModel(), Rights.READ)).thenReturn(true);
		when(datasetDeniedProfile.hasRight(FeatureStatic.EXPORT)).thenReturn(true);
		when(fixture.actorService().getActiveProfiles(fixture.robot())).thenReturn(List.of(allowedProfile, datasetDeniedProfile));
		when(fixture.actorService().getRootScopes(fixture.robot(), allowedProfile)).thenReturn(List.of(allowedScope));
		when(fixture.actorService().getRootScopes(fixture.robot(), datasetDeniedProfile)).thenReturn(List.of(deniedScope));
		final var row = new DataExtractRow(10L, 20L, "S01", null, null, java.util.Map.of("AGE", "42"));
		when(fixture.extractService().getDataRows(fixture.datasetModel(), List.of(allowedScope), 5))
			.thenReturn(new DataExtract(List.of(row), true));

		final var result = fixture.tools().queryDataset("DEMOGRAPHICS", 5);

		assertEquals(List.of(row), result.rows());
		assertEquals(true, result.truncated());
		verify(fixture.extractService()).getDataRows(fixture.datasetModel(), List.of(allowedScope), 5);
		verify(fixture.actorService(), never()).getRootScopes(fixture.robot(), datasetDeniedProfile);
	}

	private Fixture fixture() {
		final var robot = new Robot();
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(robot, null));

		final var datasetModel = mock(DatasetModel.class);
		when(datasetModel.getId()).thenReturn("DEMOGRAPHICS");
		when(datasetModel.getLocalizedShortname("en")).thenReturn("Demographics");
		when(datasetModel.isExportable()).thenReturn(true);

		final var study = mock(Study.class);
		when(study.getDatasetModel("DEMOGRAPHICS")).thenReturn(datasetModel);
		when(study.getDefaultLanguageId()).thenReturn("en");
		final var studyService = mock(StudyService.class);
		when(studyService.getStudy()).thenReturn(study);

		final var acl = mock(ACL.class);
		final var rightsService = mock(RightsService.class);
		when(rightsService.getACL(robot)).thenReturn(acl);
		final var actorService = mock(ActorService.class);
		final var extractService = mock(ExtractService.class);
		final var tools = new DatasetTools(studyService, rightsService, actorService, extractService, new UnitOfWorkService());
		return new Fixture(tools, robot, datasetModel, actorService, extractService);
	}

	private FieldModel fieldModel(final String id, final boolean exportable) {
		final var fieldModel = mock(FieldModel.class);
		when(fieldModel.getId()).thenReturn(id);
		when(fieldModel.isExportable()).thenReturn(exportable);
		when(fieldModel.getLocalizedShortname("en")).thenReturn(id);
		when(fieldModel.getType()).thenReturn(FieldModelType.STRING);
		when(fieldModel.getDataType()).thenReturn(OperandType.STRING);
		when(fieldModel.getPossibleValues()).thenReturn(List.of());
		return fieldModel;
	}

	private record Fixture(
		DatasetTools tools,
		Robot robot,
		DatasetModel datasetModel,
		ActorService actorService,
		ExtractService extractService
	) {
	}
}
