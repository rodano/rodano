package ch.rodano.api.logger;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.core.model.actor.ActorType;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.logger.LoggerService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.RightsService;

/**
 * This controller is specifically reserved to robots
 */
@Tag(name = "Logger Controller", description = "Uses logger to save messages from external sources (robots for instance)")
@RestController
@RequestMapping("/logger")
public class LoggerController extends AbstractSecuredController {
	private final LoggerService loggerService;

	public LoggerController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final LoggerService loggerService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.loggerService = loggerService;
	}

	@Operation(summary = "Create log", hidden = true)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void log(
		@RequestBody final LoggerDTO loggerDTO
	) {
		final var actor = currentActor();
		if(!ActorType.ROBOT.equals(actor.getType())) {
			throw new IllegalArgumentException("Only robot can create custom log");
		}
		loggerService.log((Robot) actor, loggerDTO.log(), loggerDTO.level());
	}
}
