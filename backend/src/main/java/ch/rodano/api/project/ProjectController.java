package ch.rodano.api.project;

import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.services.bll.export.views.AggregateWorkflowViewService;
import ch.rodano.core.services.bll.export.views.ExportViewService;
import ch.rodano.core.services.bll.project.ProjectService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeAncestorServiceImpl;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.project.ProjectIdResolver;

@RestController
@RequestMapping("/projects")
public class ProjectController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

	private final ProjectService projectService;
	private final ProjectIdResolver projectIdResolver;
	private final StudyService studyService;
	private final ProjectMapper projectMapper;
	private final ScopeAncestorServiceImpl scopeAncestorService;
	private final AggregateWorkflowViewService aggregateWorkflowViewService;
	private final ExportViewService exportViewService;
	private final RoleService roleService;
	private final UserSecurityService userSecurityService;

	public ProjectController(final ProjectService projectService,
							 final ProjectIdResolver projectIdResolver,
							 final StudyService studyService,
							 final ProjectMapper projectMapper,
							 final ScopeAncestorServiceImpl scopeAncestorService,
							 final AggregateWorkflowViewService aggregateWorkflowViewService,
							 final ExportViewService exportViewService,
							 final RoleService roleService,
							 final UserSecurityService userSecurityService) {
		this.projectService = projectService;
		this.projectIdResolver = projectIdResolver;
		this.studyService = studyService;
		this.projectMapper = projectMapper;
		this.scopeAncestorService = scopeAncestorService;
		this.aggregateWorkflowViewService = aggregateWorkflowViewService;
		this.exportViewService = exportViewService;
		this.roleService = roleService;
		this.userSecurityService = userSecurityService;
	}

	private Actor getCurrentActor() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null && authentication.getPrincipal() instanceof Actor) {
			return (Actor) authentication.getPrincipal();
		}
		throw new IllegalStateException("No authenticated actor found");
	}

	@GetMapping
	public ResponseEntity<List<ProjectDTO>> getAccessibleProjects() {
		final var actor = getCurrentActor();
		final var projects = projectService.getProjectsForActor(actor.getPk());
		final var projectDTOs = projects.stream()
			.map(projectMapper::toDTO)
			.toList();

		LOGGER.info("Retrieved {} accessible projects for user {}", projectDTOs.size(), actor.getPk());
		return ResponseEntity.ok(projectDTOs);
	}

	@PostMapping("/select/{projectId}")
	@Transactional
	public ResponseEntity<ProjectDTO> selectProject(@PathVariable final UUID projectId) {
		LOGGER.info("Project selection requested: {}", projectId);

		final var actor = getCurrentActor();
		final var accessibleProjects = projectService.getProjectsForActor(actor.getPk());

		final boolean hasAccess = accessibleProjects.stream()
			.anyMatch(p -> p.getProjectId().equals(projectId));

		if(!hasAccess) {
			LOGGER.warn("User {} attempted to access project {} without permission", actor.getPk(), projectId);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		projectIdResolver.setProjectId(projectId);

		try {
			studyService.loadStudyForProject(projectId);
			LOGGER.info("Study loaded successfully for project {}", projectId);

			LOGGER.info("Initializing views for project {}", projectId);
			scopeAncestorService.updateView();
			aggregateWorkflowViewService.updateView();
			exportViewService.updateViews();
			LOGGER.info("Views initialized successfully");
		}
		catch(Exception e) {
			LOGGER.error("Failed to load study for project {}", projectId, e);
			projectIdResolver.clearProject();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		final var project = projectService.getProjectById(projectId);

		return ResponseEntity.ok(projectMapper.toDTO(project));
	}

	@GetMapping("/current")
	public ResponseEntity<ProjectDTO> getCurrentProject() {
		if(!projectIdResolver.hasProject()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}

		final var projectId = projectIdResolver.id();
		final var project = projectService.getProjectById(projectId);

		return ResponseEntity.ok(projectMapper.toDTO(project));
	}

	@PostMapping("/clear")
	public ResponseEntity<Void> clearProject() {
		LOGGER.info("Clearing current project selection");
		projectIdResolver.clearProject();
		return ResponseEntity.ok().build();
	}
}
