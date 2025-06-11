package ch.rodano.api.controller.widget.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.dto.widget.workflow.WorkflowWidgetDTO;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatusInfo;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.widget.workflow.WorkflowWidgetService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Widgets")
@RestController
@RequestMapping("/widget/workflow")
@Transactional(readOnly = true)
public class WorkflowWidgetController extends AbstractSecuredController {
	private final WorkflowWidgetService workflowWidgetService;
	private final ScopeDAOService scopeDAOService;

	private final Integer defaultPageSize;

	public WorkflowWidgetController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final WorkflowWidgetService workflowWidgetService,
		final ScopeDAOService scopeDAOService,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.workflowWidgetService = workflowWidgetService;
		this.scopeDAOService = scopeDAOService;
		this.defaultPageSize = defaultPageSize;
	}

	@Operation(summary = "Get the workflow widget")
	@GetMapping("{widgetId}")
	@ResponseStatus(HttpStatus.OK)
	public WorkflowWidgetDTO getWidget(
		@PathVariable final String widgetId
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		final var study = studyService.getStudy();

		// Retrieve widget and check rights
		final var widget = study.getWorkflowWidget(widgetId);
		for(final var workflow : widget.getWorkflows()) {
			rightsService.checkRight(currentActor, currentRoles, workflow);
		}

		return new WorkflowWidgetDTO(widget);
	}

	@Operation(summary = "Get the workflows widget data")
	@GetMapping("{widgetId}/data")
	public PagedResult<WorkflowStatusInfo> getData(
		@PathVariable final String widgetId,
		@RequestParam final Optional<List<Long>> scopePks,
		@Parameter(description = "Full text search on scope code and workflowable model") @RequestParam final Optional<String> fullText,
		@Parameter(description = "Order the results by which property?") @RequestParam final Optional<String> sortBy,
		@Parameter(description = "Use the ascending order?") @RequestParam final Optional<Boolean> orderAscending,
		@Parameter(description = "Page size") @RequestParam final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam final Optional<Integer> pageIndex
	) {
		final var currentActor = currentActor();
		final var study = studyService.getStudy();
		final var widget = study.getWorkflowWidget(widgetId);

		//use the scope pks provided in the URL or retrieve root scopes of the user
		final Collection<Scope> scopes = new ArrayList<>();
		if(scopePks.isPresent()) {
			scopes.addAll(scopeDAOService.getScopesByPks(scopePks.get()));
			for(final var scope : scopes) {
				final var currentRoles = currentActiveRoles(scope);
				for(final Workflow workflow : widget.getWorkflows()) {
					rightsService.checkRight(currentActor, currentRoles, workflow);
				}
			}
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor, widget.getWorkflows().get(0)));
		}

		return workflowWidgetService.getData(
			widget,
			scopes,
			currentLanguages(),
			fullText,
			sortBy,
			orderAscending,
			Optional.of(pageSize.orElse(defaultPageSize)),
			Optional.of(pageIndex.orElse(0))
		);
	}

	@Operation(summary = "Export the workflow widget")
	@GetMapping("{widgetId}/export")
	public ResponseEntity<StreamingResponseBody> getExport(
		@PathVariable final String widgetId,
		@RequestParam final Optional<List<Long>> scopePks
	) {
		final var currentActor = currentActor();
		final var languages = currentLanguages();
		final var study = studyService.getStudy();
		final var widget = study.getWorkflowWidget(widgetId);

		//use the scope pks provided in the URL or retrieve root scopes of the user
		final Collection<Scope> scopes = new ArrayList<>();
		if(scopePks.isPresent()) {
			scopes.addAll(scopeDAOService.getScopesByPks(scopePks.get()));
			for(final var scope : scopes) {
				final var currentRoles = currentActiveRoles(scope);
				for(final Workflow workflow : widget.getWorkflows()) {
					rightsService.checkRight(currentActor, currentRoles, workflow);
				}
			}
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor, widget.getWorkflows().get(0)));
		}

		//send response
		final StreamingResponseBody stream = os -> workflowWidgetService.getExport(os, widget, scopes, languages);
		final var widgetLabel = StringUtils.replace(widget.getLocalizedShortname(languages).toLowerCase(), " ", "_");
		final var filename = study.generateFilename(widgetLabel, ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}
}
