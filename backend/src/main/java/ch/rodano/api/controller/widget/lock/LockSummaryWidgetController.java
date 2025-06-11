package ch.rodano.api.controller.widget.lock;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
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
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.statistics.LockSummaryService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Widgets")
@RestController
@RequestMapping("/widget/lock-summary")
@Transactional(readOnly = true)
public class LockSummaryWidgetController extends AbstractSecuredController {
	private final LockSummaryService lockSummaryService;
	private final ScopeDAOService scopeDAOService;

	public LockSummaryWidgetController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final LockSummaryService lockSummaryService,
		final ScopeDAOService scopeDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.lockSummaryService = lockSummaryService;
		this.scopeDAOService = scopeDAOService;
	}

	@Operation(summary = "Get the lock summary")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public SummaryDTO getSummary(
		@RequestParam final Optional<Long> scopePk
	) {
		final var currentActor = currentActor();
		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();

		final Scope scope;
		if(scopePk.isPresent()) {
			scope = scopeDAOService.getScopeByPk(scopePk.get());
			final var currentRoles = currentActiveRoles(scope);
			rightsService.checkRight(currentActor, currentRoles, scope);
			rightsService.checkRight(currentActor, currentRoles, leafScopeModel, Rights.READ);
		}
		else {
			scope = actorService.getRootScope(currentActor(), leafScopeModel, Rights.READ).orElseThrow();
		}

		return lockSummaryService.getSummary(scope, leafScopeModel);
	}

	@Operation(summary = "Export the scope lock summary for a root scope")
	@GetMapping("export/scopes")
	public ResponseEntity<StreamingResponseBody> getScopeExport(
		@RequestParam final Optional<Long> scopePk
	) {
		final var currentActor = currentActor();
		final var languages = currentLanguages();
		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();

		final Scope scope;
		if(scopePk.isPresent()) {
			scope = scopeDAOService.getScopeByPk(scopePk.get());
			final var currentRoles = currentActiveRoles(scope);
			rightsService.checkRight(currentActor, currentRoles, scope);
			rightsService.checkRight(currentActor, currentRoles, leafScopeModel, Rights.READ);
		}
		else {
			scope = actorService.getRootScope(currentActor(), leafScopeModel, Rights.READ).orElseThrow();
		}

		//send response
		final StreamingResponseBody stream = os -> lockSummaryService.getScopeExport(os, scope, leafScopeModel, languages);
		final var filename = studyService.getStudy().generateFilename("scopes_lock_summary", ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}

	@Operation(summary = "Export the event lock summary for a root scope")
	@GetMapping("export/events")
	public ResponseEntity<StreamingResponseBody> getEventExport(
		@RequestParam final Optional<Long> scopePk
	) {
		final var currentActor = currentActor();
		final var languages = currentLanguages();
		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();

		final Scope scope;
		if(scopePk.isPresent()) {
			scope = scopeDAOService.getScopeByPk(scopePk.get());
			final var currentRoles = currentActiveRoles(scope);
			rightsService.checkRight(currentActor, currentRoles, scope);
			rightsService.checkRight(currentActor, currentRoles, leafScopeModel, Rights.READ);
		}
		else {
			scope = actorService.getRootScope(currentActor(), leafScopeModel, Rights.READ).orElseThrow();
		}

		//send response
		final StreamingResponseBody stream = os -> lockSummaryService.getEventExport(os, scope, leafScopeModel, languages);
		final var filename = studyService.getStudy().generateFilename("events_lock_summary", ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}
}
