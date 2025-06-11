package ch.rodano.api.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.RightsService;

public abstract class AbstractSecuredController {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final RequestContextService requestContextService;
	protected final StudyService studyService;
	protected final ActorService actorService;
	protected final RoleService roleService;
	protected final RightsService rightsService;

	public AbstractSecuredController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService
	) {
		this.requestContextService = requestContextService;
		this.studyService = studyService;
		this.actorService = actorService;
		this.roleService = roleService;
		this.rightsService = rightsService;
	}

	protected ResponseEntity<StreamingResponseBody> streamResponse(final StreamingResponseBody stream, final MediaType type) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.contentType(type)
			.body(stream);
	}

	protected ResponseEntity<StreamingResponseBody> fileResponse(final StreamingResponseBody stream, final MediaType type, final String filename) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.contentType(type)
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
			.body(stream);
	}

	protected ResponseEntity<StreamingResponseBody> exportResponse(final ExportFormat format, final StreamingResponseBody stream, final String filename) {
		return fileResponse(stream, MediaType.valueOf(format.getMimeType()), filename);
	}

	/**
	 * Get the current context
	 *
	 * @return The current context used to perform the database operations
	 */
	protected DatabaseActionContext currentContext() {
		return requestContextService.getDatabaseActionContext();
	}

	/**
	 * Get the current actor
	 *
	 * @return The actor hitting the API
	 */
	protected Actor currentActor() {
		return (Actor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/**
	 * Get the current roles
	 *
	 * @return The roles of the actor hitting the API
	 */
	protected List<Role> currentRoles() {
		return roleService.getRoles(currentActor());
	}

	/**
	 * Get the current active roles
	 *
	 * @return The active roles of the actor hitting the API
	 */
	protected List<Role> currentActiveRoles() {
		return roleService.getActiveRoles(currentActor());
	}

	/**
	 * Get the current active roles for a scope
	 *
	 * @return The active roles of the actor hitting the API for a scope
	 */
	protected List<Role> currentActiveRoles(final Scope scope) {
		return roleService.getActiveRoles(currentActor(), scope);
	}

	protected String[] currentLanguages() {
		return actorService.getLanguages(currentActor());
	}

	protected String getServerURL(final HttpServletRequest servletRequest) {
		return servletRequest.getRequestURL().toString().replace(servletRequest.getRequestURI(), "");
	}
}
