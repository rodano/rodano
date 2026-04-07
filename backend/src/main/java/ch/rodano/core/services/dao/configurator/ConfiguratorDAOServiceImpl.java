package ch.rodano.core.services.dao.configurator;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.impl.DSL;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rodano.api.configurator.dto.ConfiguratorProjectDTO;
import ch.rodano.api.configurator.dto.ProjectConfigVersionDTO;
import ch.rodano.api.configurator.dto.ProjectLanguageDTO;
import ch.rodano.api.configurator.dto.ProjectRuleTagDTO;
import ch.rodano.api.configurator.request.CreateProjectRequest;
import ch.rodano.api.configurator.request.UpdateProjectRequest;
import ch.rodano.core.model.jooq.enums.ProjectConfigVersionStatus;
import ch.rodano.core.model.jooq.enums.ProjectStatus;

import static ch.rodano.core.model.jooq.Tables.PROJECT;
import static ch.rodano.core.model.jooq.Tables.PROJECT_CONFIG_VERSION;
import static ch.rodano.core.model.jooq.Tables.USER;
import static ch.rodano.core.model.jooq.tables.ProjectLanguage.PROJECT_LANGUAGE;
import static ch.rodano.core.model.jooq.tables.ProjectRuleTag.PROJECT_RULE_TAG;

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
	@Transactional(readOnly = true)
	@Cacheable(value = "projects")
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
				activeVersion.STATUS.as("activeConfigVersionStatus"),
				draftVersion.PK.as("draftConfigVersionId"),
				draftVersion.VERSION_NUMBER.as("draftVersionNumber"),
				draftVersion.STATUS.as("draftConfigVersionStatus"),
				DSL.exists(DSL.selectOne()
					.from(PROJECT_CONFIG_VERSION)
					.where(PROJECT_CONFIG_VERSION.PROJECT_ID.eq(PROJECT.PROJECT_ID)
						.and(PROJECT_CONFIG_VERSION.STATUS.eq(ProjectConfigVersionStatus.ARCHIVED)))
				).as("hasArchivedVersion"),
				PROJECT.EMAIL,
				PROJECT.SMTP_TLS,
				PROJECT.PASSWORD_STRONG,
				PROJECT.PASSWORD_LENGTH,
				PROJECT.PASSWORD_VALIDITY_DURATION,
				PROJECT.PASSWORD_UNIQUE,
				PROJECT.EPRO_ENABLED,
				PROJECT.EPRO_PROFILE_ID,
				PROJECT.CLIENT_NAME,
				PROJECT.CLIENT_EMAIL,
				PROJECT.PROTOCOL_NO,
				PROJECT.VERSION_NUMBER
			)
			.from(PROJECT)
			.leftJoin(activeVersion)
			.on(activeVersion.PK.eq(PROJECT.ACTIVE_CONFIG_VERSION_FK))
			.leftJoin(draftVersion)
			.on(draftVersion.PROJECT_ID.eq(PROJECT.PROJECT_ID)
				.and(draftVersion.STATUS.in(ProjectConfigVersionStatus.DRAFT, ProjectConfigVersionStatus.ARCHIVED))
				.and(draftVersion.PK.ne(activeVersion.PK).or(activeVersion.PK.isNull())))
			.orderBy(PROJECT.CREATED.desc())
			.fetch(record -> {
				final var projectId = record.get(PROJECT.PROJECT_ID);
				return new ConfiguratorProjectDTO(
					projectId,
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
					record.get("activeConfigVersionStatus", ProjectConfigVersionStatus.class),
					record.get("draftConfigVersionId", Long.class),
					record.get("draftVersionNumber", Integer.class),
					record.get("draftConfigVersionStatus", ProjectConfigVersionStatus.class),
					record.get("hasArchivedVersion", Boolean.class),
					record.get(PROJECT.EMAIL),
					record.get(PROJECT.SMTP_TLS),
					record.get(PROJECT.PASSWORD_STRONG),
					record.get(PROJECT.PASSWORD_LENGTH),
					record.get(PROJECT.PASSWORD_VALIDITY_DURATION),
					record.get(PROJECT.PASSWORD_UNIQUE),
					record.get(PROJECT.EPRO_ENABLED),
					record.get(PROJECT.EPRO_PROFILE_ID),
					record.get(PROJECT.CLIENT_NAME),
					record.get(PROJECT.CLIENT_EMAIL),
					record.get(PROJECT.PROTOCOL_NO),
					record.get(PROJECT.VERSION_NUMBER),
					getProjectLanguages(projectId),
					getProjectRuleTags(projectId)
				);
			});
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "project", key = "#projectId")
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
				activeVersion.STATUS.as("activeConfigVersionStatus"),
				draftVersion.PK.as("draftConfigVersionId"),
				draftVersion.VERSION_NUMBER.as("draftVersionNumber"),
				draftVersion.STATUS.as("draftConfigVersionStatus"),
				DSL.exists(DSL.selectOne()
					.from(PROJECT_CONFIG_VERSION)
					.where(PROJECT_CONFIG_VERSION.PROJECT_ID.eq(PROJECT.PROJECT_ID)
						.and(PROJECT_CONFIG_VERSION.STATUS.eq(ProjectConfigVersionStatus.ARCHIVED)))
				).as("hasArchivedVersion"),
				PROJECT.EMAIL,
				PROJECT.SMTP_TLS,
				PROJECT.PASSWORD_STRONG,
				PROJECT.PASSWORD_LENGTH,
				PROJECT.PASSWORD_VALIDITY_DURATION,
				PROJECT.PASSWORD_UNIQUE,
				PROJECT.EPRO_ENABLED,
				PROJECT.EPRO_PROFILE_ID,
				PROJECT.CLIENT_NAME,
				PROJECT.CLIENT_EMAIL,
				PROJECT.PROTOCOL_NO,
				PROJECT.VERSION_NUMBER
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
				record.get("activeConfigVersionStatus", ProjectConfigVersionStatus.class),
				record.get("draftConfigVersionId", Long.class),
				record.get("draftVersionNumber", Integer.class),
				record.get("draftConfigVersionStatus", ProjectConfigVersionStatus.class),
				record.get("hasArchivedVersion", Boolean.class),
				record.get(PROJECT.EMAIL),
				record.get(PROJECT.SMTP_TLS),
				record.get(PROJECT.PASSWORD_STRONG),
				record.get(PROJECT.PASSWORD_LENGTH),
				record.get(PROJECT.PASSWORD_VALIDITY_DURATION),
				record.get(PROJECT.PASSWORD_UNIQUE),
				record.get(PROJECT.EPRO_ENABLED),
				record.get(PROJECT.EPRO_PROFILE_ID),
				record.get(PROJECT.CLIENT_NAME),
				record.get(PROJECT.CLIENT_EMAIL),
				record.get(PROJECT.PROTOCOL_NO),
				record.get(PROJECT.VERSION_NUMBER),
				getProjectLanguages(projectId),
				getProjectRuleTags(projectId)
			));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean projectCodeExists(final String code) {
		return dsl.fetchExists(
			dsl.selectFrom(PROJECT)
				.where(PROJECT.CODE.eq(code))
		);
	}

	@Override
	@Transactional
	@CacheEvict(value = "projects", allEntries = true)
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
				.set(PROJECT.STATUS, (ProjectStatus) null)
				.set(PROJECT.CREATED, now)
				.execute();

			if(request.languages() != null && !request.languages().isEmpty()) {
				var insert = dsl.insertInto(PROJECT_LANGUAGE,
					PROJECT_LANGUAGE.PROJECT_ID,
					PROJECT_LANGUAGE.LANGUAGE,
					PROJECT_LANGUAGE.IS_DEFAULT);

				for(final var lang : request.languages()) {
					insert = insert.values(projectId, lang.languageCode(), lang.isDefault());
				}
				insert.execute();
			}

			dsl.insertInto(PROJECT_CONFIG_VERSION)
				.set(PROJECT_CONFIG_VERSION.PROJECT_ID, projectId)
				.set(PROJECT_CONFIG_VERSION.VERSION_NUMBER, 1)
				.set(PROJECT_CONFIG_VERSION.STATUS, ProjectConfigVersionStatus.DRAFT)
				.set(PROJECT_CONFIG_VERSION.CREATED_AT, now)
				.set(PROJECT_CONFIG_VERSION.CONFIG_SNAPSHOT, "{}")
				.set(PROJECT_CONFIG_VERSION.CHANGE_SUMMARY, "Initial configuration")
				.execute();

			return getProject(projectId);

		}
		catch(Exception e) {
			throw new RuntimeException("Failed to create project", e);
		}
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "projects", allEntries = true),
		@CacheEvict(value = "project", key = "#projectId")
	})
	public void updateProject(final UUID projectId, final UpdateProjectRequest request) {
		try {
			final var query = dsl.updateQuery(PROJECT);

			if(request.code() != null) {
				query.addValue(PROJECT.CODE, request.code());
			}
			if(request.shortname() != null) {
				query.addValue(PROJECT.SHORTNAME, objectMapper.writeValueAsString(request.shortname()));
			}
			if(request.longname() != null) {
				query.addValue(PROJECT.LONGNAME, objectMapper.writeValueAsString(request.longname()));
			}
			if(request.description() != null) {
				query.addValue(PROJECT.DESCRIPTION, objectMapper.writeValueAsString(request.description()));
			}
			if(request.url() != null) {
				query.addValue(PROJECT.URL, request.url());
			}
			if(request.color() != null) {
				query.addValue(PROJECT.COLOR, request.color());
			}
			if(request.introductionText() != null) {
				query.addValue(PROJECT.INTRODUCTION_TEXT, request.introductionText());
			}
			if(request.versionDate() != null) {
				query.addValue(PROJECT.VERSION_DATE, request.versionDate());
			}
			if(request.email() != null) {
				query.addValue(PROJECT.EMAIL, request.email());
			}
			if(request.smtpTls() != null) {
				query.addValue(PROJECT.SMTP_TLS, request.smtpTls());
			}
			if(request.passwordStrong() != null) {
				query.addValue(PROJECT.PASSWORD_STRONG, request.passwordStrong());
			}
			if(request.passwordLength() != null) {
				query.addValue(PROJECT.PASSWORD_LENGTH, request.passwordLength());
			}
			if(request.passwordValidityDuration() != null) {
				query.addValue(PROJECT.PASSWORD_VALIDITY_DURATION, request.passwordValidityDuration());
			}
			if(request.passwordUnique() != null) {
				query.addValue(PROJECT.PASSWORD_UNIQUE, request.passwordUnique());
			}
			if(request.eproEnabled() != null) {
				query.addValue(PROJECT.EPRO_ENABLED, request.eproEnabled());
			}
			if(request.eproProfileId() != null) {
				query.addValue(PROJECT.EPRO_PROFILE_ID, request.eproProfileId());
			}
			if(request.clientName() != null) {
				query.addValue(PROJECT.CLIENT_NAME, request.clientName());
			}
			if(request.clientEmail() != null) {
				query.addValue(PROJECT.CLIENT_EMAIL, request.clientEmail());
			}
			if(request.protocolNo() != null) {
				query.addValue(PROJECT.PROTOCOL_NO, request.protocolNo());
			}
			if(request.versionNumber() != null) {
				query.addValue(PROJECT.VERSION_NUMBER, request.versionNumber());
			}

			query.addConditions(PROJECT.PROJECT_ID.eq(projectId));
			query.execute();

			if(request.languages() != null) {
				updateProjectLanguages(projectId, request.languages());
			}

			if(request.ruleTags() != null) {
				updateProjectRuleTags(projectId, request.ruleTags());
			}

		}
		catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize JSON fields", e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "draftVersion", key = "#projectId")
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
	@Transactional(readOnly = true)
	@Cacheable(value = "activeVersion", key = "#projectId")
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
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "draftVersion", key = "#draft.projectId"),
		@CacheEvict(value = "projectVersions", key = "#draft.projectId")
	})
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
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "projects", allEntries = true),
		@CacheEvict(value = "project", allEntries = true),
		@CacheEvict(value = "draftVersion", allEntries = true),
		@CacheEvict(value = "activeVersion", allEntries = true),
		@CacheEvict(value = "projectVersions", allEntries = true),
		@CacheEvict(value = "projectVersion", allEntries = true)
	})
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
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "projects", allEntries = true),
		@CacheEvict(value = "project", allEntries = true),
		@CacheEvict(value = "draftVersion", allEntries = true),
		@CacheEvict(value = "activeVersion", allEntries = true),
		@CacheEvict(value = "projectVersions", allEntries = true),
		@CacheEvict(value = "projectVersion", allEntries = true)
	})
	public void archiveVersion(final Long versionId) {
		dsl.update(PROJECT_CONFIG_VERSION)
			.set(PROJECT_CONFIG_VERSION.STATUS, ProjectConfigVersionStatus.ARCHIVED)
			.where(PROJECT_CONFIG_VERSION.PK.eq(versionId))
			.execute();
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "draftVersion", allEntries = true),
		@CacheEvict(value = "projectVersions", allEntries = true),
		@CacheEvict(value = "projectVersion", allEntries = true)
	})
	public void restoreDraft(final Long versionId) {
		dsl.update(PROJECT_CONFIG_VERSION)
			.set(PROJECT_CONFIG_VERSION.STATUS, ProjectConfigVersionStatus.DRAFT)
			.where(PROJECT_CONFIG_VERSION.PK.eq(versionId))
			.execute();
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "projects", allEntries = true),
		@CacheEvict(value = "project", key = "#projectId"),
		@CacheEvict(value = "activeVersion", key = "#projectId")
	})
	public void updateProjectActiveVersion(final UUID projectId, final Long versionId) {
		dsl.update(PROJECT)
			.set(PROJECT.ACTIVE_CONFIG_VERSION_FK, versionId)
			.where(PROJECT.PROJECT_ID.eq(projectId))
			.execute();
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "projectVersions", key = "#projectId")
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
	@Transactional(readOnly = true)
	@Cacheable(value = "projectVersion", key = "#projectId + '-' + #versionId")
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

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "projects", allEntries = true),
		@CacheEvict(value = "project", key = "#projectId")
	})
	public void updateProjectStatus(final UUID projectId, final ProjectStatus status) {
		dsl.update(PROJECT)
			.set(PROJECT.STATUS, status)
			.where(PROJECT.PROJECT_ID.eq(projectId))
			.execute();
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "configSnapshot", key = "#versionId")
	public String getConfigSnapshot(final Long versionId) {
		return dsl.select(PROJECT_CONFIG_VERSION.CONFIG_SNAPSHOT)
			.from(PROJECT_CONFIG_VERSION)
			.where(PROJECT_CONFIG_VERSION.PK.eq(versionId))
			.fetchOne(PROJECT_CONFIG_VERSION.CONFIG_SNAPSHOT);
	}

	@Override
	@Transactional
	@CacheEvict(value = "configSnapshot", key = "#versionId")
	public void updateConfigSnapshot(final Long versionId, final String snapshotJson) {
		dsl.update(PROJECT_CONFIG_VERSION)
			.set(PROJECT_CONFIG_VERSION.CONFIG_SNAPSHOT, snapshotJson)
			.where(PROJECT_CONFIG_VERSION.PK.eq(versionId))
			.execute();
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "projectVersions", allEntries = true),
		@CacheEvict(value = "projectVersion", allEntries = true)
	})
	public void incrementVersionNumber(final Long versionId) {
		dsl.update(PROJECT_CONFIG_VERSION)
			.set(PROJECT_CONFIG_VERSION.VERSION_NUMBER, PROJECT_CONFIG_VERSION.VERSION_NUMBER.plus(1))
			.where(PROJECT_CONFIG_VERSION.PK.eq(versionId))
			.execute();
	}

	private List<ProjectLanguageDTO> getProjectLanguages(final UUID projectId) {
		return dsl.select(
				PROJECT_LANGUAGE.LANGUAGE,
				PROJECT_LANGUAGE.IS_DEFAULT
			)
			.from(PROJECT_LANGUAGE)
			.where(PROJECT_LANGUAGE.PROJECT_ID.eq(projectId))
			.orderBy(PROJECT_LANGUAGE.IS_DEFAULT.desc(), PROJECT_LANGUAGE.LANGUAGE.asc())
			.fetch(record -> new ProjectLanguageDTO(
				record.get(PROJECT_LANGUAGE.LANGUAGE),
				record.get(PROJECT_LANGUAGE.IS_DEFAULT)
			));
	}

	private List<ProjectRuleTagDTO> getProjectRuleTags(final UUID projectId) {
		return dsl.select(PROJECT_RULE_TAG.TAG)
			.from(PROJECT_RULE_TAG)
			.where(PROJECT_RULE_TAG.PROJECT_ID.eq(projectId))
			.orderBy(PROJECT_RULE_TAG.TAG.asc())
			.fetch(record -> new ProjectRuleTagDTO(
				record.get(PROJECT_RULE_TAG.TAG)
			));
	}

	private void updateProjectLanguages(final UUID projectId, final List<ProjectLanguageDTO> languages) {
		dsl.deleteFrom(PROJECT_LANGUAGE)
			.where(PROJECT_LANGUAGE.PROJECT_ID.eq(projectId))
			.execute();

		if(!languages.isEmpty()) {
			var insert = dsl.insertInto(PROJECT_LANGUAGE,
				PROJECT_LANGUAGE.PROJECT_ID,
				PROJECT_LANGUAGE.LANGUAGE,
				PROJECT_LANGUAGE.IS_DEFAULT);

			for(final var lang : languages) {
				insert = insert.values(projectId, lang.languageCode(), lang.isDefault());
			}

			insert.execute();
		}
	}

	private void updateProjectRuleTags(final UUID projectId, final List<ProjectRuleTagDTO> ruleTags) {
		dsl.deleteFrom(PROJECT_RULE_TAG)
			.where(PROJECT_RULE_TAG.PROJECT_ID.eq(projectId))
			.execute();

		if(!ruleTags.isEmpty()) {
			var insert = dsl.insertInto(PROJECT_RULE_TAG,
				PROJECT_RULE_TAG.PROJECT_ID,
				PROJECT_RULE_TAG.TAG);

			for(final var tag : ruleTags) {
				insert = insert.values(projectId, tag.tag());
			}

			insert.execute();
		}
	}
}
