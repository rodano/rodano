package ch.rodano.core.services.bll.configurator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.DeserializationFeature;
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

	private final SnapshotRestoreService snapshotRestoreService;

	public ConfiguratorServiceImpl(
		final ConfiguratorDAOService configuratorDAOService,
		final UserSecurityService userSecurityService,
		final ObjectMapper objectMapper,
		final SnapshotRestoreService snapshotRestoreService
	) {
		this.configuratorDAOService = configuratorDAOService;
		this.userSecurityService = userSecurityService;
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.snapshotRestoreService = snapshotRestoreService;
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
		final var version = configuratorDAOService.getVersion(projectId, versionId);
		if(version.status() != ProjectConfigVersionStatus.DRAFT) {
			throw new IllegalStateException("Can only create snapshots for DRAFT versions");
		}

		final var tableData = snapshotRestoreService.captureProjectData(projectId);

		final var configSnapshotRaw = configuratorDAOService.getConfigSnapshot(versionId);
		ConfigSnapshotDTO snapshots;
		try {
			if(configSnapshotRaw == null || configSnapshotRaw.isEmpty() || "{}".equals(configSnapshotRaw)) {
				snapshots = new ConfigSnapshotDTO(new java.util.ArrayList<>(), -1);
			}
			else {
				snapshots = objectMapper.readValue(decompressFromBase64(configSnapshotRaw), ConfigSnapshotDTO.class);
			}
		}
		catch(Exception e) {
			snapshots = new ConfigSnapshotDTO(new java.util.ArrayList<>(), -1);
		}

		final var snapshotList = new java.util.ArrayList<>(snapshots.snapshots());
		if(snapshots.currentIndex() >= 0 && snapshots.currentIndex() < snapshotList.size() - 1) {
			snapshotList.subList(snapshots.currentIndex() + 1, snapshotList.size()).clear();
		}

		snapshotList.add(new ConfigSnapshotDTO.SnapshotEntry(ZonedDateTime.now(), summary, tableData));

		final var updatedSnapshots = new ConfigSnapshotDTO(snapshotList, snapshotList.size() - 1);
		try {
			configuratorDAOService.updateConfigSnapshot(versionId, compressToBase64(objectMapper.writeValueAsString(updatedSnapshots)));
			configuratorDAOService.incrementVersionNumber(versionId);
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to create snapshot", e);
		}
	}

	@Override
	public void rollbackSnapshot(final UUID projectId, final Long versionId) {
		final var version = configuratorDAOService.getVersion(projectId, versionId);
		if(version.status() != ProjectConfigVersionStatus.DRAFT) {
			throw new IllegalStateException("Can only rollback DRAFT versions");
		}

		try {
			final var snapshots = objectMapper.readValue(
				decompressFromBase64(configuratorDAOService.getConfigSnapshot(versionId)),
				ConfigSnapshotDTO.class);

			if(snapshots.currentIndex() == null || snapshots.currentIndex() <= 0) {
				throw new IllegalStateException("No previous snapshot to rollback to");
			}

			final var newIndex = snapshots.currentIndex() - 1;
			snapshotRestoreService.restoreProjectData(projectId, snapshots.snapshots().get(newIndex).tables());

			configuratorDAOService.updateConfigSnapshot(versionId,
				compressToBase64(objectMapper.writeValueAsString(new ConfigSnapshotDTO(snapshots.snapshots(), newIndex))));
		}
		catch(RuntimeException e) {
			throw e;
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

		try {
			final var snapshots = objectMapper.readValue(
				decompressFromBase64(configuratorDAOService.getConfigSnapshot(versionId)),
				ConfigSnapshotDTO.class);

			if(snapshots.currentIndex() == null || snapshots.currentIndex() >= snapshots.snapshots().size() - 1) {
				throw new IllegalStateException("No next snapshot to roll forward to");
			}

			final var newIndex = snapshots.currentIndex() + 1;
			snapshotRestoreService.restoreProjectData(projectId, snapshots.snapshots().get(newIndex).tables());

			configuratorDAOService.updateConfigSnapshot(versionId,
				compressToBase64(objectMapper.writeValueAsString(new ConfigSnapshotDTO(snapshots.snapshots(), newIndex))));
		}
		catch(RuntimeException e) {
			throw e;
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
			return objectMapper.readValue(decompressFromBase64(configSnapshotJson), ConfigSnapshotDTO.class);
		}
		catch(Exception e) {
			return new ConfigSnapshotDTO(List.of(), -1);
		}
	}

	@Override
	public ConfiguratorProjectDTO cloneProject(final UUID sourceProjectId, final CreateProjectRequest request) {
		final var newProject = configuratorDAOService.createProject(request);
		snapshotRestoreService.cloneProjectData(sourceProjectId, newProject.projectId());
		return newProject;
	}

	private String compressToBase64(final String json) throws IOException {
		final var bos = new ByteArrayOutputStream();
		try(final var gz = new GZIPOutputStream(bos)) {
			gz.write(json.getBytes(StandardCharsets.UTF_8));
		}
		return Base64.getEncoder().encodeToString(bos.toByteArray());
	}

	private String decompressFromBase64(final String base64) throws IOException {
		if(base64 == null || base64.startsWith("{")) {
			return base64;
		}
		final byte[] compressed = Base64.getDecoder().decode(base64);
		try(final var gz = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
			return new String(gz.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
