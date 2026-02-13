package ch.rodano.core.services.bll.configurator;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.rodano.api.configurator.dto.ConfigSnapshotDTO;
import ch.rodano.api.configurator.dto.ConfiguratorProjectDTO;
import ch.rodano.api.configurator.dto.ProjectConfigVersionDTO;
import ch.rodano.api.configurator.request.CreateProjectRequest;
import ch.rodano.api.configurator.request.UpdateProjectRequest;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.model.jooq.enums.ProjectConfigVersionStatus;
import ch.rodano.core.model.jooq.enums.ProjectStatus;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.dao.configurator.ConfiguratorDAOService;

@Service
@Transactional
public class ConfiguratorServiceImpl implements ConfiguratorService {

	private final ConfiguratorDAOService configuratorDAOService;
	private final UserSecurityService userSecurityService;
	private final ObjectMapper objectMapper;

	public ConfiguratorServiceImpl(
		final ConfiguratorDAOService configuratorDAOService,
		final UserSecurityService userSecurityService,
		final ObjectMapper objectMapper
	) {
		this.configuratorDAOService = configuratorDAOService;
		this.userSecurityService = userSecurityService;
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
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
	public ConfiguratorProjectDTO updateProject(final UUID projectId, final UpdateProjectRequest request) {
		final var existingProject = configuratorDAOService.getProject(projectId);
		if(existingProject == null) {
			throw new NotFoundException("Project not found: " + projectId);
		}

		configuratorDAOService.updateProject(projectId, request);

		return configuratorDAOService.getProject(projectId);
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

		final var project = configuratorDAOService.getProject(projectId);
		final boolean isFirstPublish = project.status() == null;

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

		if(isFirstPublish) {
			configuratorDAOService.updateProjectStatus(projectId, ProjectStatus.ACTIVE);
		}
	}

	@Override
	public void archiveDraft(final UUID projectId, final Long versionId) {
		final var version = configuratorDAOService.getVersion(projectId, versionId);
		if(version == null) {
			throw new NotFoundException("Version not found: " + versionId);
		}

		if(version.status() != ProjectConfigVersionStatus.DRAFT) {
			throw new IllegalStateException("Can only archive DRAFT versions, current status: " + version.status());
		}

		configuratorDAOService.archiveVersion(versionId);
	}

	@Override
	public void restoreDraft(final UUID projectId, final Long versionId) {
		final var version = configuratorDAOService.getVersion(projectId, versionId);
		if(version == null) {
			throw new NotFoundException("Version not found: " + versionId);
		}

		if(version.status() != ProjectConfigVersionStatus.ARCHIVED) {
			throw new IllegalStateException("Can only restore ARCHIVED versions, current status: " + version.status());
		}

		configuratorDAOService.restoreDraft(versionId);
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

	@Override
	public void createSnapshot(final UUID projectId, final Long versionId, final String summary) {
		final var project = configuratorDAOService.getProject(projectId);
		final var version = configuratorDAOService.getVersion(projectId, versionId);

		if(version.status() != ProjectConfigVersionStatus.DRAFT) {
			throw new IllegalStateException("Can only create snapshots for DRAFT versions");
		}

		final var configSnapshotJson = configuratorDAOService.getConfigSnapshot(versionId);

		ConfigSnapshotDTO snapshots;
		try {
			if(configSnapshotJson == null || configSnapshotJson.isEmpty() || "{}".equals(configSnapshotJson)) {
				snapshots = new ConfigSnapshotDTO(new java.util.ArrayList<>(), -1);
			}
			else {
				snapshots = objectMapper.readValue(configSnapshotJson, ConfigSnapshotDTO.class);
			}
		}
		catch(Exception e) {
			snapshots = new ConfigSnapshotDTO(new java.util.ArrayList<>(), -1);
		}

		final var snapshotList = new java.util.ArrayList<>(snapshots.snapshots());
		if(snapshots.currentIndex() >= 0 && snapshots.currentIndex() < snapshotList.size() - 1) {
			snapshotList.subList(snapshots.currentIndex() + 1, snapshotList.size()).clear();
		}

		final var newSnapshot = new ConfigSnapshotDTO.SnapshotEntry(
			ZonedDateTime.now(),
			summary,
			project
		);
		snapshotList.add(newSnapshot);

		final var updatedSnapshots = new ConfigSnapshotDTO(
			snapshotList,
			snapshotList.size() - 1
		);

		try {
			final var snapshotJson = objectMapper.writeValueAsString(updatedSnapshots);
			configuratorDAOService.updateConfigSnapshot(versionId, snapshotJson);
			incrementVersionNumber(versionId);
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to create snapshot", e);
		}
	}

	private void incrementVersionNumber(final Long versionId) {
		configuratorDAOService.incrementVersionNumber(versionId);
	}

	@Override
	public void rollbackSnapshot(final UUID projectId, final Long versionId) {
		final var version = configuratorDAOService.getVersion(projectId, versionId);

		if(version.status() != ProjectConfigVersionStatus.DRAFT) {
			throw new IllegalStateException("Can only rollback DRAFT versions");
		}

		final var configSnapshotJson = configuratorDAOService.getConfigSnapshot(versionId);

		try {
			final var snapshots = objectMapper.readValue(configSnapshotJson, ConfigSnapshotDTO.class);

			if(snapshots.currentIndex() == null || snapshots.currentIndex() <= 0) {
				throw new IllegalStateException("No previous snapshot to rollback to");
			}

			final var newIndex = snapshots.currentIndex() - 1;
			final var snapshotToRestore = snapshots.snapshots().get(newIndex);

			final var updateRequest = buildUpdateRequest(snapshotToRestore.data());

			configuratorDAOService.updateProject(projectId, updateRequest);

			final var updated = new ConfigSnapshotDTO(snapshots.snapshots(), newIndex);
			configuratorDAOService.updateConfigSnapshot(versionId, objectMapper.writeValueAsString(updated));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to rollback snapshot", e);
		}
	}

	@Override
	public void rollForwardSnapshot(final UUID projectId, final Long versionId) {
		final var version = configuratorDAOService.getVersion(projectId, versionId);

		if(version.status() != ProjectConfigVersionStatus.DRAFT) {
			throw new IllegalStateException("Can only roll forward DRAFT versions");
		}

		final var configSnapshotJson = configuratorDAOService.getConfigSnapshot(versionId);

		try {
			final var snapshots = objectMapper.readValue(configSnapshotJson, ConfigSnapshotDTO.class);

			if(snapshots.currentIndex() == null || snapshots.currentIndex() >= snapshots.snapshots().size() - 1) {
				throw new IllegalStateException("No next snapshot to roll forward to");
			}

			final var newIndex = snapshots.currentIndex() + 1;
			final var snapshotToRestore = snapshots.snapshots().get(newIndex);

			final var updateRequest = buildUpdateRequest(snapshotToRestore.data());

			configuratorDAOService.updateProject(projectId, updateRequest);

			final var updated = new ConfigSnapshotDTO(snapshots.snapshots(), newIndex);
			configuratorDAOService.updateConfigSnapshot(versionId, objectMapper.writeValueAsString(updated));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to roll forward snapshot", e);
		}
	}

	@Override
	public ConfigSnapshotDTO getSnapshots(final UUID projectId, final Long versionId) {
		final var configSnapshotJson = configuratorDAOService.getConfigSnapshot(versionId);

		try {
			if(configSnapshotJson == null || configSnapshotJson.isEmpty() || "{}".equals(configSnapshotJson)) {
				return new ConfigSnapshotDTO(List.of(), -1);
			}
			return objectMapper.readValue(configSnapshotJson, ConfigSnapshotDTO.class);
		}
		catch(Exception e) {
			return new ConfigSnapshotDTO(List.of(), -1);
		}
	}

	private UpdateProjectRequest buildUpdateRequest(final ConfiguratorProjectDTO data) {
		return new UpdateProjectRequest(
			data.code(),
			data.shortname(),
			data.longname(),
			data.description(),
			data.url(),
			data.color(),
			data.introductionText(),
			data.versionDate(),
			data.email(),
			data.smtpTls(),
			data.passwordStrong(),
			data.passwordLength(),
			data.passwordValidityDuration(),
			data.passwordUnique(),
			data.eproEnabled(),
			data.eproProfileId(),
			data.clientName(),
			data.clientEmail(),
			data.protocolNo(),
			data.versionNumber(),
			data.languages(),
			data.ruleTags()
		);
	}
}
