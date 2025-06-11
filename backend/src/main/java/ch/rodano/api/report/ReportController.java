package ch.rodano.api.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.export.report.ReportService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Report", description = "Get data reports")
@RestController
@RequestMapping("reports")
@Validated
@Transactional(readOnly = true)
public class ReportController extends AbstractSecuredController {
	private final ReportService reportService;
	private final ScopeDAOService scopeDAOService;

	public ReportController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ReportService reportService,
		final ScopeDAOService scopeDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.reportService = reportService;
		this.scopeDAOService = scopeDAOService;
	}

	@Operation(summary = "Get the transfers report")
	@GetMapping("transfers")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> getTransferReport(
		@RequestParam final String scopeModelId,
		@RequestParam final Optional<Long> scopePk
	) {
		final var currentActor = currentActor();
		var currentRoles = currentActiveRoles();

		final var study = studyService.getStudy();
		final var scopeModel = study.getScopeModel(scopeModelId);

		//for the root scope, use the pk provided in the URL or retrieve root scopes
		final Collection<Scope> scopes = new ArrayList<>();
		if(scopePk.isPresent()) {
			final var scope = scopeDAOService.getScopeByPk(scopePk.get());
			currentRoles = currentActiveRoles(scope);
			rightsService.checkRight(currentActor, currentRoles, FeatureStatic.EXPORT);
			scopes.add(scope);
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor, FeatureStatic.EXPORT));
		}

		//send response
		final StreamingResponseBody stream = os -> reportService.getTransferReport(os, scopes, scopeModel);
		final var scopeModelLabel = StringUtils.replace(scopeModel.getDefaultLocalizedShortname().toLowerCase(), " ", "_");
		final var filename = study.generateFilename(String.format("%s_transfers", scopeModelLabel), ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}

	@Operation(summary = "Get the events report")
	@GetMapping("events")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> getEventsReport(
		@RequestParam final String scopeModelId,
		@RequestParam final Optional<Long> scopePk
	) {
		final var currentActor = currentActor();
		var currentRoles = currentActiveRoles();

		final var study = studyService.getStudy();
		final var scopeModel = study.getScopeModel(scopeModelId);

		//for the root scope, use the pk provided in the URL or retrieve root scopes
		final Collection<Scope> scopes = new ArrayList<>();
		if(scopePk.isPresent()) {
			final var scope = scopeDAOService.getScopeByPk(scopePk.get());
			currentRoles = currentActiveRoles(scope);
			rightsService.checkRight(currentActor, currentRoles, FeatureStatic.EXPORT);
			scopes.add(scope);
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor, FeatureStatic.EXPORT));
		}

		//send response
		final StreamingResponseBody stream = os -> reportService.getVisitReport(os, scopes, scopeModel);
		final var scopeModelLabel = StringUtils.replace(scopeModel.getDefaultLocalizedShortname().toLowerCase(), " ", "_");
		final var filename = study.generateFilename(String.format("%s_events", scopeModelLabel), ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}
}
