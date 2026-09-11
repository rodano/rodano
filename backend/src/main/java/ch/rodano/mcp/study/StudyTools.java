package ch.rodano.mcp.study;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.RightsService;

@Profile({ "api" })
@Component
public class StudyTools {
	private final StudyService studyService;
	private final RightsService rightsService;

	public StudyTools(final StudyService studyService, final RightsService rightsService) {
		this.studyService = studyService;
		this.rightsService = rightsService;
	}

	@McpTool(name = "describe_study", description = "Describe the study models readable by the authenticated Rodano robot")
	public StudySummary describeStudy() {
		final var authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null || !(authentication.getPrincipal() instanceof final Robot robot)) {
			throw new AccessDeniedException("A Rodano robot is required");
		}

		final var study = studyService.getStudy();
		final var acl = rightsService.getACL(robot);
		final var scopeModelIds = study.getScopeModels().stream()
			.filter(scopeModel -> acl.hasRight(scopeModel, Rights.READ))
			.map(scopeModel -> scopeModel.getId())
			.toList();
		final var datasetModelIds = study.getDatasetModels().stream()
			.filter(datasetModel -> acl.hasRight(datasetModel, Rights.READ))
			.map(datasetModel -> datasetModel.getId())
			.toList();

		return new StudySummary(
			study.getId(),
			study.getDefaultLocalizedShortname(),
			study.getDefaultLanguageId(),
			scopeModelIds,
			datasetModelIds
		);
	}
}
