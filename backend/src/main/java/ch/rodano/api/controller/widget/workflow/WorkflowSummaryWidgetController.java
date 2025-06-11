package ch.rodano.api.controller.widget.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.widget.SummaryDTO;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.widget.workflow.WorkflowSummaryService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Widgets")
@RestController
@RequestMapping("/widget/workflow-summary")
@Transactional(readOnly = true)
public class WorkflowSummaryWidgetController extends AbstractSecuredController {

	private final WorkflowSummaryService workflowSummaryService;
	private final ScopeDAOService scopeDAOService;

	public WorkflowSummaryWidgetController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final WorkflowSummaryService workflowSummaryService,
		final ScopeDAOService scopeDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.workflowSummaryService = workflowSummaryService;
		this.scopeDAOService = scopeDAOService;
	}

	@Operation(summary = "Get the workflow summary")
	@GetMapping("{workflowSummaryId}")
	@ResponseStatus(HttpStatus.OK)
	public SummaryDTO getSummary(
		@PathVariable final String workflowSummaryId,
		@RequestParam final Optional<Long> scopePk
	) {
		final var currentActor = currentActor();
		final var workflowSummary = studyService.getStudy().getWorkflowSummary(workflowSummaryId);

		//use the scope pk provided in the URL or retrieve root scopes of the user
		final Scope scope;
		if(scopePk.isPresent()) {
			scope = scopeDAOService.getScopeByPk(scopePk.get());
			final var currentRoles = currentActiveRoles(scope);
			for(final var workflow : workflowSummary.getWorkflows()) {
				rightsService.checkRight(currentActor, currentRoles, workflow);
			}
		}
		else {
			scope = actorService.getRootScope(currentActor, workflowSummary.getWorkflows().get(0)).orElseThrow();
		}

		return workflowSummaryService.getSummary(workflowSummary, scope);
	}

	@Operation(summary = "Export the workflow summary")
	@GetMapping("{workflowSummaryId}/export")
	public ResponseEntity<StreamingResponseBody> getExport(
		@PathVariable final String workflowSummaryId,
		@RequestParam final Optional<Long> scopePk
	) {
		final var currentActor = currentActor();
		final var languages = currentLanguages();
		final var study = studyService.getStudy();
		final var summary = study.getWorkflowSummary(workflowSummaryId);

		//use the scope pk provided in the URL or retrieve root scopes of the user
		final List<Scope> scopes = new ArrayList<>();
		final List<Role> roles;
		if(scopePk.isPresent()) {
			final var scope = scopeDAOService.getScopeByPk(scopePk.get());
			roles = currentActiveRoles(scope);
			for(final Workflow workflow : summary.getWorkflows()) {
				rightsService.checkRight(currentActor, roles, workflow);
			}
			scopes.add(scope);
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor, summary.getWorkflows().get(0)));
			if(scopes.isEmpty()) {
				throw new UnsupportedOperationException("No scope available for the export");
			}
			//TODO do not rely on only one scope to decide if deleted data must be included
			roles = currentActiveRoles(scopes.get(0));
		}

		final var includeDeleted = rightsService.hasRight(roles, FeatureStatic.MANAGE_DELETED_DATA);

		//send response
		final StreamingResponseBody stream = os -> workflowSummaryService.getExport(os, summary, scopes, includeDeleted, languages);
		final var title = summary.getLocalizedTitle(study.getDefaultLanguageId()).replaceAll(" ", "_").toLowerCase();
		final var filename = study.generateFilename(title, ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}

	@Operation(summary = "Export the historical workflow summary")
	@GetMapping("{workflowSummaryId}/export/history")
	public ResponseEntity<StreamingResponseBody> getHistoricalExport(
		@PathVariable final String workflowSummaryId,
		@RequestParam final Optional<Long> scopePk
	) {
		final var currentActor = currentActor();
		final var languages = currentLanguages();
		final var study = studyService.getStudy();
		final var summary = study.getWorkflowSummary(workflowSummaryId);

		//use the scope pk provided in the URL or retrieve root scopes of the user
		final List<Scope> scopes = new ArrayList<>();
		final List<Role> roles;
		if(scopePk.isPresent()) {
			final var scope = scopeDAOService.getScopeByPk(scopePk.get());
			roles = currentActiveRoles(scope);
			for(final Workflow workflow : summary.getWorkflows()) {
				rightsService.checkRight(currentActor, roles, workflow);
			}
			scopes.add(scope);
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor, summary.getWorkflows().get(0)));
			if(scopes.isEmpty()) {
				throw new UnsupportedOperationException("No scope available for the export");
			}
			//TODO do not rely on only one scope to decide if deleted data must be included
			roles = currentActiveRoles(scopes.get(0));
		}

		final var includeDeleted = rightsService.hasRight(roles, FeatureStatic.MANAGE_DELETED_DATA);

		//send response
		final StreamingResponseBody stream = os -> workflowSummaryService.getHistoricalExport(os, summary, scopes, includeDeleted, languages);
		final var title = summary.getLocalizedTitle(study.getDefaultLanguageId()).replaceAll(" ", "_").toLowerCase();
		final var filename = study.generateFilename(String.format("%s_history", title), ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}

}
