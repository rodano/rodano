package ch.rodano.api.controller.widget.overdue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.dto.widget.overdue.OverdueDTO;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.widget.overdue.OverdueWidgetService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Profile("!migration")
@Tag(name = "Widget Patient Overdue")
@RestController
@RequestMapping("/widget/overdue")
@Transactional(readOnly = true)
public class OverdueController extends AbstractSecuredController {
	private final ScopeDAOService scopeDAOService;
	private final OverdueWidgetService overdueWidgetService;

	private final Integer defaultPageSize;

	public OverdueController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final OverdueWidgetService overdueWidgetService,
		final ScopeDAOService scopeDAOService,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.scopeDAOService = scopeDAOService;
		this.overdueWidgetService = overdueWidgetService;
		this.defaultPageSize = defaultPageSize;
	}

	@Operation(summary = "Get widget data")
	@GetMapping("{widgetId}")
	public PagedResult<OverdueDTO> getData(
		@PathVariable final String widgetId,
		@RequestParam final Optional<List<Long>> scopePks,
		@Parameter(description = "Full text search on scope code and workflowable model") @RequestParam(name = "fullText") final Optional<String> fullText,
		@Parameter(description = "Order the results by which property?") @RequestParam(name = "sortBy") final Optional<String> sortBy,
		@Parameter(description = "Use the ascending order?") @RequestParam(name = "orderAscending") final Optional<Boolean> orderAscending,
		@Parameter(description = "Page size") @RequestParam(name = "pageSize") final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam(name = "pageIndex") final Optional<Integer> pageIndex
	) {
		final var currentActor = currentActor();

		//use the ancestor scope pks provided in the URL or retrieve root scopes of the user
		final Collection<Scope> scopes = new ArrayList<>();
		if(scopePks.isPresent()) {
			scopes.addAll(scopeDAOService.getScopesByPks(scopePks.get()));
			for(final var scope : scopes) {
				final var scopeRoles = currentActiveRoles(scope);
				rightsService.checkRight(currentActor, scopeRoles, scope);
			}
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor));
		}

		return overdueWidgetService.getData(
			widgetId,
			scopes,
			currentLanguages(),
			fullText,
			sortBy,
			orderAscending,
			Optional.of(pageSize.orElse(defaultPageSize)),
			Optional.of(pageIndex.orElse(0))
		);
	}

	@Operation(summary = "Export widget data")
	@GetMapping("{widgetId}/export")
	public ResponseEntity<StreamingResponseBody> getExport(
		@PathVariable final String widgetId,
		@RequestParam final Optional<List<Long>> scopePks
	) {
		final var currentActor = currentActor();

		//use the ancestor scope pks provided in the URL or retrieve root scopes of the user
		final Collection<Scope> scopes = new ArrayList<>();
		if(scopePks.isPresent()) {
			scopes.addAll(scopeDAOService.getScopesByPks(scopePks.get()));
			for(final var scope : scopes) {
				final var scopeRoles = currentActiveRoles(scope);
				rightsService.checkRight(currentActor, scopeRoles, scope);
			}
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor));
		}

		final StreamingResponseBody stream = os -> overdueWidgetService.export(widgetId, os, scopes, currentLanguages());
		final var basename = overdueWidgetService.getExportFilename(widgetId);
		final var filename = studyService.getStudy().generateFilename(basename, ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}
}
