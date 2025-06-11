package ch.rodano.api.controller.widget.dashboard;

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.core.model.user.User;
import ch.rodano.core.plugins.dashboard.DashboardData;
import ch.rodano.core.plugins.dashboard.DashboardPlugin;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Widgets")
@RestController
@RequestMapping("/widget/dashboard")
@Transactional(readOnly = true)
public class DashboardWidgetController extends AbstractSecuredController {
	private final DSLContext create;
	private final Optional<DashboardPlugin> dashboardPlugin;

	public DashboardWidgetController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final Optional<DashboardPlugin> dashboardPlugin,
		final DSLContext create
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.create = create;
		this.dashboardPlugin = dashboardPlugin;
	}

	@Operation(summary = "General information widget")
	@GetMapping("general-information")
	@ResponseStatus(HttpStatus.OK)
	public List<DashboardData> getGeneralInformation() {
		final var currentUser = (User) currentActor();
		return dashboardPlugin.map(d -> d.getGeneralInformation(create, currentUser)).orElse(null);
	}
}
