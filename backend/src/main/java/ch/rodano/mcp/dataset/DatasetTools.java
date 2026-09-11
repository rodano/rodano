package ch.rodano.mcp.dataset;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.export.extract.ExtractService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.unitofwork.UnitOfWorkService;
import ch.rodano.core.utils.RightsService;

@Profile({ "api" })
@Component
@Transactional(readOnly = true)
public class DatasetTools {
	private static final int DEFAULT_MAX_ROWS = 20;
	private static final int MAX_ROWS = 100;

	private final StudyService studyService;
	private final RightsService rightsService;
	private final ActorService actorService;
	private final ExtractService extractService;
	private final UnitOfWorkService unitOfWorkService;

	public DatasetTools(
		final StudyService studyService,
		final RightsService rightsService,
		final ActorService actorService,
		final ExtractService extractService,
		final UnitOfWorkService unitOfWorkService
	) {
		this.studyService = studyService;
		this.rightsService = rightsService;
		this.actorService = actorService;
		this.extractService = extractService;
		this.unitOfWorkService = unitOfWorkService;
	}

	@McpTool(name = "describe_dataset", description = "Describe an exportable dataset and its fields when readable by the authenticated Rodano robot")
	public DatasetDescription describeDataset(
		@McpToolParam(description = "Dataset model ID", required = true) final String datasetModelId
	) {
		return unitOfWorkService.run(true, () -> {
			final var robot = currentRobot();
			final var datasetModel = readableExportableDataset(robot, datasetModelId);
			return describe(datasetModel);
		});
	}

	@McpTool(name = "query_dataset", description = "Read a bounded set of rows from one exportable dataset within the authenticated Rodano robot's scopes")
	public DatasetQueryResult queryDataset(
		@McpToolParam(description = "Dataset model ID", required = true) final String datasetModelId,
		@McpToolParam(description = "Maximum rows to return, from 1 to 100; defaults to 20", required = false) final Integer maxRows
	) {
		final var effectiveMaxRows = maxRows == null ? DEFAULT_MAX_ROWS : maxRows;
		if(effectiveMaxRows < 1 || effectiveMaxRows > MAX_ROWS) {
			throw new IllegalArgumentException(String.format("maxRows must be between 1 and %d", MAX_ROWS));
		}

		return unitOfWorkService.run(true, () -> {
			final var robot = currentRobot();
			final var datasetModel = readableExportableDataset(robot, datasetModelId);
			final var scopes = authorizedScopes(robot, datasetModel);
			if(scopes.isEmpty()) {
				throw new UnauthorizedException("No scope is available for this dataset");
			}
			final var extract = extractService.getDataRows(datasetModel, scopes, effectiveMaxRows);
			return new DatasetQueryResult(datasetModel.getId(), extract.rows(), extract.truncated());
		});
	}

	private Robot currentRobot() {
		final var authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null || !(authentication.getPrincipal() instanceof final Robot robot)) {
			throw new AccessDeniedException("A Rodano robot is required");
		}
		return robot;
	}

	private DatasetModel readableExportableDataset(final Robot robot, final String datasetModelId) {
		final var datasetModel = studyService.getStudy().getDatasetModel(datasetModelId);
		final var acl = rightsService.getACL(robot);
		acl.checkRight(FeatureStatic.EXPORT);
		acl.checkRight(datasetModel, Rights.READ);
		if(!datasetModel.isExportable()) {
			throw new UnauthorizedException(String.format("Dataset model %s is not exportable", datasetModel.getId()));
		}
		return datasetModel;
	}

	private List<Scope> authorizedScopes(final Robot robot, final DatasetModel datasetModel) {
		return actorService.getActiveProfiles(robot).stream()
			.filter(profile -> profile.hasRight(FeatureStatic.EXPORT))
			.filter(profile -> profile.hasRight(datasetModel, Rights.READ))
			.flatMap(profile -> actorService.getRootScopes(robot, profile).stream())
			.distinct()
			.toList();
	}

	private DatasetDescription describe(final DatasetModel datasetModel) {
		final var languageId = studyService.getStudy().getDefaultLanguageId();
		final var fields = datasetModel.getFieldModelsExportables().stream()
			.map(fieldModel -> new DatasetFieldDescription(
				fieldModel.getId(),
				fieldModel.getLocalizedShortname(languageId),
				fieldModel.getType().name(),
				fieldModel.getDataType().name(),
				fieldModel.getPossibleValues().stream()
					.map(value -> new DatasetPossibleValue(value.getId(), value.getLocalizedShortname(languageId)))
					.toList()
			))
			.toList();
		return new DatasetDescription(
			datasetModel.getId(),
			datasetModel.getLocalizedShortname(languageId),
			datasetModel.isScopeDocumentation(),
			datasetModel.isMultiple(),
			fields
		);
	}
}
