package ch.rodano.api.timeline;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.graph.timeline.TimelineService;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Timeline")
@RestController
@RequestMapping("/scopes/{scopePk}/timeline")
@Transactional(readOnly = true)
public class TimelineController extends AbstractSecuredController {

	private final TimelineService timelineService;
	private final UtilsService utilsService;
	private final ScopeDAOService scopeDAOService;

	public TimelineController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final TimelineService timelineService,
		final UtilsService utilsService,
		final ScopeDAOService scopeDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.timelineService = timelineService;
		this.utilsService = utilsService;
		this.scopeDAOService = scopeDAOService;
	}

	@Operation(summary = "Get scope timeline")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<TimelineGraphDataDTO> getTimeline(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.READ);

		return timelineService.getTimelineGraphs(currentActor, currentRoles, scope, studyService.getStudy()).stream()
			.map(TimelineGraphDataDTO::new)
			.toList();
	}
}
