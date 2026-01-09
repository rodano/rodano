package ch.rodano.core.services.bll.configurator;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.configurator.ConfiguratorProjectDTO;
import ch.rodano.api.configurator.CreateProjectRequest;
import ch.rodano.api.configurator.ProjectConfigVersionDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.model.jooq.enums.ProjectConfigVersionStatus;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.dao.configurator.ConfiguratorDAOService;

@Service
@Transactional
public class ConfiguratorServiceImpl implements ConfiguratorService {

	private final ConfiguratorDAOService configuratorDAOService;
	private final UserSecurityService userSecurityService;

	public ConfiguratorServiceImpl(
		final ConfiguratorDAOService configuratorDAOService,
		final UserSecurityService userSecurityService
	) {
		this.configuratorDAOService = configuratorDAOService;
		this.userSecurityService = userSecurityService;
	}

	@Override
	public List<ConfiguratorProjectDTO> getAllProjects() {
		return configuratorDAOService.getAllProjects();
	}

	@Override
	public ConfiguratorProjectDTO getProject(final UUID projectId) {
		final var project = configuratorDAOService.getProject(projectId);
		if(project == null) {
			throw new NotFoundException("Project not found: " + projectId);
		}
		return project;
	}

	@Override
	public ConfiguratorProjectDTO createProject(final CreateProjectRequest request) {
		if(request.code() == null || request.code().isBlank()) {
			throw new IllegalArgumentException("Project code is required");
		}

		if(request.shortname() == null || request.shortname().isEmpty()) {
			throw new IllegalArgumentException("Project shortname is required");
		}

		if(request.longname() == null || request.longname().isEmpty()) {
			throw new IllegalArgumentException("Project longname is required");
		}

		if(configuratorDAOService.projectCodeExists(request.code())) {
			throw new IllegalArgumentException("Project code already exists: " + request.code());
		}

		return configuratorDAOService.createProject(request);
	}

	@Override
	public ProjectConfigVersionDTO getOrCreateDraft(final UUID projectId) {
		final var existingDraft = configuratorDAOService.getDraftVersion(projectId);
		if(existingDraft != null) {
			return existingDraft;
		}

		final var activeVersion = configuratorDAOService.getActiveVersion(projectId);
		if(activeVersion == null) {
			throw new NotFoundException("No active version found for project: " + projectId);
		}

		final var currentUser = userSecurityService.getCurrentUser();

		final var draft = new ProjectConfigVersionDTO(
			null,
			projectId,
			activeVersion.versionNumber() + 1,
			ProjectConfigVersionStatus.DRAFT,
			ZonedDateTime.now(),
			currentUser.getName(),
			null,
			null,
			"Draft version " + (activeVersion.versionNumber() + 1)
		);

		return configuratorDAOService.createVersion(draft, activeVersion.pk());
	}

	@Override
	public void publishDraft(final UUID projectId, final Long versionId, final String changeSummary) {
		final var draft = configuratorDAOService.getVersion(projectId, versionId);
		if(draft == null) {
			throw new NotFoundException("Version not found: " + versionId);
		}

		if(draft.status() != ProjectConfigVersionStatus.DRAFT) {
			throw new IllegalStateException("Can only publish DRAFT versions, current status: " + draft.status());
		}

		final var currentUser = userSecurityService.getCurrentUser();

		final var currentActive = configuratorDAOService.getActiveVersion(projectId);
		if(currentActive != null) {
			configuratorDAOService.archiveVersion(currentActive.pk());
		}

		final var finalChangeSummary = (changeSummary != null && !changeSummary.isBlank())
			? changeSummary
			: "Published version " + draft.versionNumber();

		configuratorDAOService.publishVersion(
			versionId,
			currentUser.getPk(),
			ZonedDateTime.now(),
			finalChangeSummary
		);

		configuratorDAOService.updateProjectActiveVersion(projectId, versionId);
	}

	@Override
	public List<ProjectConfigVersionDTO> getVersions(final UUID projectId) {
		return configuratorDAOService.getVersions(projectId);
	}

	@Override
	public ProjectConfigVersionDTO getVersion(final UUID projectId, final Long versionId) {
		final var version = configuratorDAOService.getVersion(projectId, versionId);
		if(version == null) {
			throw new NotFoundException("Version not found: " + versionId);
		}
		return version;
	}
}
