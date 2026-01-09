package ch.rodano.core.services.dao.configurator;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.JSON;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rodano.api.configurator.ConfiguratorProjectDTO;
import ch.rodano.api.configurator.CreateProjectRequest;
import ch.rodano.api.configurator.ProjectConfigVersionDTO;
import ch.rodano.core.model.jooq.enums.ProjectConfigVersionStatus;
import ch.rodano.core.model.jooq.enums.ProjectStatus;

import static ch.rodano.core.model.jooq.Tables.PROJECT;
import static ch.rodano.core.model.jooq.Tables.PROJECT_CONFIG_VERSION;
import static ch.rodano.core.model.jooq.Tables.USER;

@Service
public class ConfiguratorDAOServiceImpl implements ConfiguratorDAOService {

	private final DSLContext dsl;
	private final ObjectMapper objectMapper;

	public ConfiguratorDAOServiceImpl(final DSLContext dsl) {
		this.dsl = dsl;
		this.objectMapper = new ObjectMapper();
	}

	private Map<String, String> parseJsonToMap(final JSON json) {
		if(json == null) {
			return Map.of();
		}
		try {
			return objectMapper.readValue(json.data(), new TypeReference<>() {
			});
		}
		catch(Exception e) {
			return Map.of();
		}
	}

	@Override
	public List<ConfiguratorProjectDTO> getAllProjects() {
		final var activeVersion = PROJECT_CONFIG_VERSION.as("active_version");
		final var draftVersion = PROJECT_CONFIG_VERSION.as("draft_version");

		return dsl.select(
				PROJECT.PROJECT_ID,
				PROJECT.CODE,
				PROJECT.SHORTNAME,
				PROJECT.LONGNAME,
				PROJECT.DESCRIPTION,
				PROJECT.URL,
				PROJECT.COLOR,
				PROJECT.INTRODUCTION_TEXT,
				PROJECT.VERSION_DATE,
				PROJECT.STATUS,
				PROJECT.CREATED,
				activeVersion.PK.as("activeConfigVersionId"),
				activeVersion.VERSION_NUMBER.as("activeVersionNumber"),
				draftVersion.PK.as("draftConfigVersionId"),
				draftVersion.VERSION_NUMBER.as("draftVersionNumber")
			)
			.from(PROJECT)
			.leftJoin(activeVersion)
			.on(activeVersion.PK.eq(PROJECT.ACTIVE_CONFIG_VERSION_FK))
			.leftJoin(draftVersion)
			.on(draftVersion.PROJECT_ID.eq(PROJECT.PROJECT_ID)
				.and(draftVersion.STATUS.eq(ProjectConfigVersionStatus.DRAFT)))
			.orderBy(PROJECT.CREATED.desc())
			.fetch(record -> new ConfiguratorProjectDTO(
				record.get(PROJECT.PROJECT_ID),
				record.get(PROJECT.CODE),
				parseJsonToMap(record.get(PROJECT.SHORTNAME, JSON.class)),
				parseJsonToMap(record.get(PROJECT.LONGNAME, JSON.class)),
				parseJsonToMap(record.get(PROJECT.DESCRIPTION, JSON.class)),
				record.get(PROJECT.URL),
				record.get(PROJECT.COLOR),
				record.get(PROJECT.INTRODUCTION_TEXT),
				record.get(PROJECT.VERSION_DATE),
				record.get(PROJECT.STATUS),
				record.get(PROJECT.CREATED),
				record.get("activeConfigVersionId", Long.class),
				record.get("activeVersionNumber", Integer.class),
				record.get("draftConfigVersionId", Long.class),
				record.get("draftVersionNumber", Integer.class)
			));
	}

	@Override
	public ConfiguratorProjectDTO getProject(final UUID projectId) {
		final var activeVersion = PROJECT_CONFIG_VERSION.as("active_version");
		final var draftVersion = PROJECT_CONFIG_VERSION.as("draft_version");

		return dsl.select(
				PROJECT.PROJECT_ID,
				PROJECT.CODE,
				PROJECT.SHORTNAME,
				PROJECT.LONGNAME,
				PROJECT.DESCRIPTION,
				PROJECT.URL,
				PROJECT.COLOR,
				PROJECT.INTRODUCTION_TEXT,
				PROJECT.VERSION_DATE,
				PROJECT.STATUS,
				PROJECT.CREATED,
				activeVersion.PK.as("activeConfigVersionId"),
				activeVersion.VERSION_NUMBER.as("activeVersionNumber"),
				draftVersion.PK.as("draftConfigVersionId"),
				draftVersion.VERSION_NUMBER.as("draftVersionNumber")
			)
			.from(PROJECT)
			.leftJoin(activeVersion)
			.on(activeVersion.PK.eq(PROJECT.ACTIVE_CONFIG_VERSION_FK))
			.leftJoin(draftVersion)
			.on(draftVersion.PROJECT_ID.eq(PROJECT.PROJECT_ID)
				.and(draftVersion.STATUS.eq(ProjectConfigVersionStatus.DRAFT)))
			.where(PROJECT.PROJECT_ID.eq(projectId))
			.fetchOne(record -> new ConfiguratorProjectDTO(
				record.get(PROJECT.PROJECT_ID),
				record.get(PROJECT.CODE),
				parseJsonToMap(record.get(PROJECT.SHORTNAME, JSON.class)),
				parseJsonToMap(record.get(PROJECT.LONGNAME, JSON.class)),
				parseJsonToMap(record.get(PROJECT.DESCRIPTION, JSON.class)),
				record.get(PROJECT.URL),
				record.get(PROJECT.COLOR),
				record.get(PROJECT.INTRODUCTION_TEXT),
				record.get(PROJECT.VERSION_DATE),
				record.get(PROJECT.STATUS),
				record.get(PROJECT.CREATED),
				record.get("activeConfigVersionId", Long.class),
				record.get("activeVersionNumber", Integer.class),
				record.get("draftConfigVersionId", Long.class),
				record.get("draftVersionNumber", Integer.class)
			));
	}

	@Override
	public boolean projectCodeExists(final String code) {
		return dsl.fetchExists(
			dsl.selectFrom(PROJECT)
				.where(PROJECT.CODE.eq(code))
		);
	}

	@Override
	public ConfiguratorProjectDTO createProject(final CreateProjectRequest request) {
		final var projectId = UUID.randomUUID();
		final var now = ZonedDateTime.now();

		try {
			dsl.insertInto(PROJECT)
				.set(PROJECT.PROJECT_ID, projectId)
				.set(PROJECT.CODE, request.code())
				.set(PROJECT.SHORTNAME, objectMapper.writeValueAsString(request.shortname()))
				.set(PROJECT.LONGNAME, objectMapper.writeValueAsString(request.longname()))
				.set(PROJECT.DESCRIPTION, request.description() != null
					? objectMapper.writeValueAsString(request.description())
					: null)
				.set(PROJECT.URL, request.url())
				.set(PROJECT.COLOR, request.color() != null ? request.color() : "#5bd4d4")
				.set(PROJECT.INTRODUCTION_TEXT, "")
				.set(PROJECT.SMTP_TLS, false)
				.set(PROJECT.PASSWORD_STRONG, true)
				.set(PROJECT.PASSWORD_LENGTH, 12)
				.set(PROJECT.PASSWORD_VALIDITY_DURATION, 0)
				.set(PROJECT.PASSWORD_UNIQUE, false)
				.set(PROJECT.EPRO_ENABLED, false)
				.set(PROJECT.STATUS, ProjectStatus.ACTIVE)
				.set(PROJECT.CREATED, now)
				.execute();

			final var versionPk = dsl.insertInto(PROJECT_CONFIG_VERSION)
				.set(PROJECT_CONFIG_VERSION.PROJECT_ID, projectId)
				.set(PROJECT_CONFIG_VERSION.VERSION_NUMBER, 1)
				.set(PROJECT_CONFIG_VERSION.STATUS, ProjectConfigVersionStatus.PUBLISHED)
				.set(PROJECT_CONFIG_VERSION.CREATED_AT, now)
				.set(PROJECT_CONFIG_VERSION.PUBLISHED_AT, now)
				.set(PROJECT_CONFIG_VERSION.CONFIG_SNAPSHOT, "{}")
				.set(PROJECT_CONFIG_VERSION.CHANGE_SUMMARY, "Initial configuration")
				.returning(PROJECT_CONFIG_VERSION.PK)
				.fetchOne()
				.getPk();


			dsl.update(PROJECT)
				.set(PROJECT.ACTIVE_CONFIG_VERSION_FK, versionPk)
				.where(PROJECT.PROJECT_ID.eq(projectId))
				.execute();

			return getProject(projectId);

		}
		catch(Exception e) {
			throw new RuntimeException("Failed to create project", e);
		}
	}

	@Override
	public ProjectConfigVersionDTO getDraftVersion(final UUID projectId) {
		final var createdByUser = USER.as("created_by_user");

		return dsl.select(
				PROJECT_CONFIG_VERSION.PK,
				PROJECT_CONFIG_VERSION.PROJECT_ID,
				PROJECT_CONFIG_VERSION.VERSION_NUMBER,
				PROJECT_CONFIG_VERSION.STATUS,
				PROJECT_CONFIG_VERSION.CREATED_AT,
				createdByUser.NAME.as("createdByName"),
				PROJECT_CONFIG_VERSION.PUBLISHED_AT,
				PROJECT_CONFIG_VERSION.CHANGE_SUMMARY
			)
			.from(PROJECT_CONFIG_VERSION)
			.leftJoin(createdByUser)
			.on(PROJECT_CONFIG_VERSION.CREATED_BY.eq(createdByUser.PK))
			.where(PROJECT_CONFIG_VERSION.PROJECT_ID.eq(projectId)
				.and(PROJECT_CONFIG_VERSION.STATUS.eq(ProjectConfigVersionStatus.DRAFT)))
			.fetchOne(record -> new ProjectConfigVersionDTO(
				record.get(PROJECT_CONFIG_VERSION.PK),
				record.get(PROJECT_CONFIG_VERSION.PROJECT_ID),
				record.get(PROJECT_CONFIG_VERSION.VERSION_NUMBER),
				record.get(PROJECT_CONFIG_VERSION.STATUS),
				record.get(PROJECT_CONFIG_VERSION.CREATED_AT),
				record.get("createdByName", String.class),
				record.get(PROJECT_CONFIG_VERSION.PUBLISHED_AT),
				null,
				record.get(PROJECT_CONFIG_VERSION.CHANGE_SUMMARY)
			));
	}

	@Override
	public ProjectConfigVersionDTO getActiveVersion(final UUID projectId) {
		final var createdByUser = USER.as("created_by_user");
		final var publishedByUser = USER.as("published_by_user");

		return dsl.select(
				PROJECT_CONFIG_VERSION.PK,
				PROJECT_CONFIG_VERSION.PROJECT_ID,
				PROJECT_CONFIG_VERSION.VERSION_NUMBER,
				PROJECT_CONFIG_VERSION.STATUS,
				PROJECT_CONFIG_VERSION.CREATED_AT,
				createdByUser.NAME.as("createdByName"),
				PROJECT_CONFIG_VERSION.PUBLISHED_AT,
				publishedByUser.NAME.as("publishedByName"),
				PROJECT_CONFIG_VERSION.CHANGE_SUMMARY
			)
			.from(PROJECT_CONFIG_VERSION)
			.leftJoin(createdByUser)
			.on(PROJECT_CONFIG_VERSION.CREATED_BY.eq(createdByUser.PK))
			.leftJoin(publishedByUser)
			.on(PROJECT_CONFIG_VERSION.PUBLISHED_BY.eq(publishedByUser.PK))
			.where(PROJECT_CONFIG_VERSION.PROJECT_ID.eq(projectId)
				.and(PROJECT_CONFIG_VERSION.STATUS.eq(ProjectConfigVersionStatus.PUBLISHED)))
			.fetchOne(record -> new ProjectConfigVersionDTO(
				record.get(PROJECT_CONFIG_VERSION.PK),
				record.get(PROJECT_CONFIG_VERSION.PROJECT_ID),
				record.get(PROJECT_CONFIG_VERSION.VERSION_NUMBER),
				record.get(PROJECT_CONFIG_VERSION.STATUS),
				record.get(PROJECT_CONFIG_VERSION.CREATED_AT),
				record.get("createdByName", String.class),
				record.get(PROJECT_CONFIG_VERSION.PUBLISHED_AT),
				record.get("publishedByName", String.class),
				record.get(PROJECT_CONFIG_VERSION.CHANGE_SUMMARY)
			));
	}

	@Override
	public ProjectConfigVersionDTO createVersion(final ProjectConfigVersionDTO draft, final Long basedOnVersionId) {
		final var baseConfigSnapshot = dsl.select(PROJECT_CONFIG_VERSION.CONFIG_SNAPSHOT)
			.from(PROJECT_CONFIG_VERSION)
			.where(PROJECT_CONFIG_VERSION.PK.eq(basedOnVersionId))
			.fetchOne(PROJECT_CONFIG_VERSION.CONFIG_SNAPSHOT);

		final var pk = dsl.insertInto(PROJECT_CONFIG_VERSION)
			.set(PROJECT_CONFIG_VERSION.PROJECT_ID, draft.projectId())
			.set(PROJECT_CONFIG_VERSION.VERSION_NUMBER, draft.versionNumber())
			.set(PROJECT_CONFIG_VERSION.STATUS, ProjectConfigVersionStatus.DRAFT)
			.set(PROJECT_CONFIG_VERSION.CREATED_AT, draft.createdAt())
			.set(PROJECT_CONFIG_VERSION.CONFIG_SNAPSHOT, baseConfigSnapshot)
			.set(PROJECT_CONFIG_VERSION.CHANGE_SUMMARY, draft.changeSummary())
			.returning(PROJECT_CONFIG_VERSION.PK)
			.fetchOne()
			.getPk();

		return getVersion(draft.projectId(), pk);
	}

	@Override
	public void publishVersion(final Long versionId, final Long publishedByUserId,
							   final ZonedDateTime publishedAt, final String changeSummary) {
		dsl.update(PROJECT_CONFIG_VERSION)
			.set(PROJECT_CONFIG_VERSION.STATUS, ProjectConfigVersionStatus.PUBLISHED)
			.set(PROJECT_CONFIG_VERSION.PUBLISHED_BY, publishedByUserId)
			.set(PROJECT_CONFIG_VERSION.PUBLISHED_AT, publishedAt)
			.set(PROJECT_CONFIG_VERSION.CHANGE_SUMMARY, changeSummary)
			.where(PROJECT_CONFIG_VERSION.PK.eq(versionId))
			.execute();
	}

	@Override
	public void archiveVersion(final Long versionId) {
		dsl.update(PROJECT_CONFIG_VERSION)
			.set(PROJECT_CONFIG_VERSION.STATUS, ProjectConfigVersionStatus.ARCHIVED)
			.where(PROJECT_CONFIG_VERSION.PK.eq(versionId))
			.execute();
	}

	@Override
	public void updateProjectActiveVersion(final UUID projectId, final Long versionId) {
		dsl.update(PROJECT)
			.set(PROJECT.ACTIVE_CONFIG_VERSION_FK, versionId)
			.where(PROJECT.PROJECT_ID.eq(projectId))
			.execute();
	}

	@Override
	public List<ProjectConfigVersionDTO> getVersions(final UUID projectId) {
		final var createdByUser = USER.as("created_by_user");
		final var publishedByUser = USER.as("published_by_user");

		return dsl.select(
				PROJECT_CONFIG_VERSION.PK,
				PROJECT_CONFIG_VERSION.PROJECT_ID,
				PROJECT_CONFIG_VERSION.VERSION_NUMBER,
				PROJECT_CONFIG_VERSION.STATUS,
				PROJECT_CONFIG_VERSION.CREATED_AT,
				createdByUser.NAME.as("createdByName"),
				PROJECT_CONFIG_VERSION.PUBLISHED_AT,
				publishedByUser.NAME.as("publishedByName"),
				PROJECT_CONFIG_VERSION.CHANGE_SUMMARY
			)
			.from(PROJECT_CONFIG_VERSION)
			.leftJoin(createdByUser)
			.on(PROJECT_CONFIG_VERSION.CREATED_BY.eq(createdByUser.PK))
			.leftJoin(publishedByUser)
			.on(PROJECT_CONFIG_VERSION.PUBLISHED_BY.eq(publishedByUser.PK))
			.where(PROJECT_CONFIG_VERSION.PROJECT_ID.eq(projectId))
			.orderBy(PROJECT_CONFIG_VERSION.VERSION_NUMBER.desc())
			.fetch(record -> new ProjectConfigVersionDTO(
				record.get(PROJECT_CONFIG_VERSION.PK),
				record.get(PROJECT_CONFIG_VERSION.PROJECT_ID),
				record.get(PROJECT_CONFIG_VERSION.VERSION_NUMBER),
				record.get(PROJECT_CONFIG_VERSION.STATUS),
				record.get(PROJECT_CONFIG_VERSION.CREATED_AT),
				record.get("createdByName", String.class),
				record.get(PROJECT_CONFIG_VERSION.PUBLISHED_AT),
				record.get("publishedByName", String.class),
				record.get(PROJECT_CONFIG_VERSION.CHANGE_SUMMARY)
			));
	}

	@Override
	public ProjectConfigVersionDTO getVersion(final UUID projectId, final Long versionId) {
		final var createdByUser = USER.as("created_by_user");
		final var publishedByUser = USER.as("published_by_user");

		return dsl.select(
				PROJECT_CONFIG_VERSION.PK,
				PROJECT_CONFIG_VERSION.PROJECT_ID,
				PROJECT_CONFIG_VERSION.VERSION_NUMBER,
				PROJECT_CONFIG_VERSION.STATUS,
				PROJECT_CONFIG_VERSION.CREATED_AT,
				createdByUser.NAME.as("createdByName"),
				PROJECT_CONFIG_VERSION.PUBLISHED_AT,
				publishedByUser.NAME.as("publishedByName"),
				PROJECT_CONFIG_VERSION.CHANGE_SUMMARY
			)
			.from(PROJECT_CONFIG_VERSION)
			.leftJoin(createdByUser)
			.on(PROJECT_CONFIG_VERSION.CREATED_BY.eq(createdByUser.PK))
			.leftJoin(publishedByUser)
			.on(PROJECT_CONFIG_VERSION.PUBLISHED_BY.eq(publishedByUser.PK))
			.where(PROJECT_CONFIG_VERSION.PROJECT_ID.eq(projectId)
				.and(PROJECT_CONFIG_VERSION.PK.eq(versionId)))
			.fetchOne(record -> new ProjectConfigVersionDTO(
				record.get(PROJECT_CONFIG_VERSION.PK),
				record.get(PROJECT_CONFIG_VERSION.PROJECT_ID),
				record.get(PROJECT_CONFIG_VERSION.VERSION_NUMBER),
				record.get(PROJECT_CONFIG_VERSION.STATUS),
				record.get(PROJECT_CONFIG_VERSION.CREATED_AT),
				record.get("createdByName", String.class),
				record.get(PROJECT_CONFIG_VERSION.PUBLISHED_AT),
				record.get("publishedByName", String.class),
				record.get(PROJECT_CONFIG_VERSION.CHANGE_SUMMARY)
			));
	}
}
