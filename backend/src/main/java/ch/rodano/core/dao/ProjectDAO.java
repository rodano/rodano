package ch.rodano.core.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.model.jooq.tables.records.ProjectRecord;

import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;
import static ch.rodano.core.model.jooq.tables.Project.PROJECT;
import static ch.rodano.core.model.jooq.tables.ProjectLanguage.PROJECT_LANGUAGE;
import static ch.rodano.core.model.jooq.tables.ProjectRuleTag.PROJECT_RULE_TAG;

@Repository
public class ProjectDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public ProjectDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	public Optional<Study> findById(final UUID projectId) {
		return Optional.ofNullable(
			dslContext.selectFrom(PROJECT)
				.where(PROJECT.PROJECT_ID.eq(projectId))
				.fetchOne(this::mapToStudy)
		);
	}

	private Study mapToStudy(final ProjectRecord record) {
		if(record == null) {
			return null;
		}

		final Study study = new Study();

		study.setProjectId(record.getProjectId());
		study.setId(record.getCode());

		study.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		study.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		study.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		study.setUrl(record.getUrl());
		study.setEmail(record.getEmail());
		study.setColor(record.getColor());
		study.setIntroductionText(record.getIntroductionText());
		study.setSmtpTLS(record.getSmtpTls());
		study.setPasswordStrong(record.getPasswordStrong());
		study.setPasswordLength(record.getPasswordLength());
		study.setPasswordValidityDuration(record.getPasswordValidityDuration());
		study.setPasswordUniqueness(record.getPasswordUnique());
		study.setEproEnabled(record.getEproEnabled());

		if(record.getEproProfileId() != null) {
			study.setEproProfileId(getProfileCode(record.getEproProfileId()));
		}

		study.setClient(record.getClientName());
		study.setClientEmail(record.getClientEmail());
		study.setProtocolNo(record.getProtocolNo());
		study.setVersionNumber(record.getVersionNumber());

		if(record.getVersionDate() != null) {
			study.setVersionDate(record.getVersionDate().toString());
		}

		study.setConfigVersion(record.getConfigVersion());

		if(record.getConfigDate() != null) {
			study.setConfigDate(new Date(record.getConfigDate()));
		}
		study.setConfigUser(record.getConfigUser());

		study.setRuleTags(new TreeSet<>(loadRuleTags(record.getProjectId())));
		loadLanguages(study, record.getProjectId());

		return study;
	}

	private String getProfileCode(final UUID profileId) {
		return dslContext.select(PROFILE.CODE)
			.from(PROFILE)
			.where(PROFILE.PROFILE_ID.eq(profileId))
			.fetchOne(PROFILE.CODE);
	}

	private List<String> loadRuleTags(final UUID projectId) {
		return dslContext.select(PROJECT_RULE_TAG.TAG)
			.from(PROJECT_RULE_TAG)
			.where(PROJECT_RULE_TAG.PROJECT_ID.eq(projectId))
			.fetch(PROJECT_RULE_TAG.TAG);
	}

	private void loadLanguages(final Study study, final UUID projectId) {
		final var languageRecords = dslContext.selectFrom(PROJECT_LANGUAGE)
			.where(PROJECT_LANGUAGE.PROJECT_ID.eq(projectId))
			.fetch();

		final List<String> languageIds = new ArrayList<>();
		String defaultLanguageId = null;

		for(var langRecord : languageRecords) {
			final String languageCode = langRecord.getLanguage();
			languageIds.add(languageCode);

			if(langRecord.getIsDefault() != null && langRecord.getIsDefault()) {
				defaultLanguageId = languageCode;
			}
		}

		study.setLanguageIds(languageIds);

		if(defaultLanguageId != null) {
			study.setDefaultLanguageId(defaultLanguageId);
		}
	}
}
