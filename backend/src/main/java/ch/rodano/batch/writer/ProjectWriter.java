package ch.rodano.batch.writer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Project;
import ch.rodano.core.model.jooq.enums.ProjectStatus;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.core.model.jooq.tables.Project.PROJECT;
import static ch.rodano.core.model.jooq.tables.ProjectLanguage.PROJECT_LANGUAGE;
import static ch.rodano.core.model.jooq.tables.ProjectRuleTag.PROJECT_RULE_TAG;

public class ProjectWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectWriter.class);
	private static final DateTimeFormatter VERSION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Project> wrapped = (ProjectScoped<Project>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Project project = wrapped.getPayload();

				LocalDate versionDate = null;
				final String rawDate = project.getVersionDate();
				if(rawDate != null && !rawDate.isBlank()) {
					try {
						versionDate = LocalDate.parse(rawDate, VERSION_DATE_FORMAT);
					}
					catch(Exception e) {
						LOGGER.warn("Invalid version date '{}' for project {} — storing null", rawDate, projectId);
					}
				}

				tx.insertInto(PROJECT)
					.set(PROJECT.PROJECT_ID, projectId)
					.set(PROJECT.CODE, project.getId())
					.set(PROJECT.URL, project.getUrl())
					.set(PROJECT.EMAIL, project.getEmail())
					.set(PROJECT.COLOR, project.getColor())
					.set(PROJECT.INTRODUCTION_TEXT, project.getIntroductionText())
					.set(PROJECT.SMTP_TLS, project.isSmtpTLS())
					.set(PROJECT.PASSWORD_STRONG, project.isPasswordStrong())
					.set(PROJECT.PASSWORD_LENGTH, project.getPasswordLength())
					.set(PROJECT.PASSWORD_VALIDITY_DURATION, project.getPasswordValidityDuration())
					.set(PROJECT.PASSWORD_UNIQUE, project.isPasswordUniqueness())
					.set(PROJECT.EPRO_ENABLED, project.isEproEnabled())
					.set(PROJECT.CLIENT_NAME, project.getClient())
					.set(PROJECT.CLIENT_EMAIL, project.getClientEmail())
					.set(PROJECT.PROTOCOL_NO, project.getProtocolNo())
					.set(PROJECT.VERSION_NUMBER, project.getVersionNumber())
					.set(PROJECT.VERSION_DATE, versionDate)
					.set(PROJECT.CONFIG_VERSION, project.getConfigVersion())
					.set(PROJECT.CONFIG_DATE, project.getConfigDate())
					.set(PROJECT.CONFIG_USER, project.getConfigUser())
					.set(PROJECT.SHORTNAME, toJson(project.getShortname()))
					.set(PROJECT.LONGNAME, toJson(project.getLongname()))
					.set(PROJECT.DESCRIPTION, toJson(project.getDescription()))
					.set(PROJECT.STATUS, ProjectStatus.ACTIVE)
					.onDuplicateKeyUpdate()
					.set(PROJECT.URL, project.getUrl())
					.set(PROJECT.EMAIL, project.getEmail())
					.set(PROJECT.COLOR, project.getColor())
					.set(PROJECT.INTRODUCTION_TEXT, project.getIntroductionText())
					.set(PROJECT.SMTP_TLS, project.isSmtpTLS())
					.set(PROJECT.PASSWORD_STRONG, project.isPasswordStrong())
					.set(PROJECT.PASSWORD_LENGTH, project.getPasswordLength())
					.set(PROJECT.PASSWORD_VALIDITY_DURATION, project.getPasswordValidityDuration())
					.set(PROJECT.PASSWORD_UNIQUE, project.isPasswordUniqueness())
					.set(PROJECT.EPRO_ENABLED, project.isEproEnabled())
					.set(PROJECT.CLIENT_NAME, project.getClient())
					.set(PROJECT.CLIENT_EMAIL, project.getClientEmail())
					.set(PROJECT.PROTOCOL_NO, project.getProtocolNo())
					.set(PROJECT.VERSION_NUMBER, project.getVersionNumber())
					.set(PROJECT.VERSION_DATE, versionDate)
					.set(PROJECT.CONFIG_VERSION, project.getConfigVersion())
					.set(PROJECT.CONFIG_DATE, project.getConfigDate())
					.set(PROJECT.CONFIG_USER, project.getConfigUser())
					.set(PROJECT.SHORTNAME, toJson(project.getShortname()))
					.set(PROJECT.LONGNAME, toJson(project.getLongname()))
					.set(PROJECT.DESCRIPTION, toJson(project.getDescription()))
					.set(PROJECT.STATUS, ProjectStatus.ACTIVE)
					.execute();

				if(project.getLanguageIds() != null) {
					for(String lang : project.getLanguageIds()) {
						if(lang == null || lang.isBlank()) {
							continue;
						}
						final boolean isDefault = lang.equalsIgnoreCase(project.getDefaultLanguageId());
						tx.insertInto(PROJECT_LANGUAGE)
							.set(PROJECT_LANGUAGE.PROJECT_ID, projectId)
							.set(PROJECT_LANGUAGE.LANGUAGE, lang)
							.set(PROJECT_LANGUAGE.IS_DEFAULT, isDefault)
							.onDuplicateKeyUpdate()
							.set(PROJECT_LANGUAGE.IS_DEFAULT, isDefault)
							.execute();
					}
				}

				if(project.getRuleTags() != null) {
					for(String ruleTag : project.getRuleTags()) {
						if(ruleTag == null || ruleTag.isBlank()) {
							continue;
						}
						tx.insertInto(PROJECT_RULE_TAG)
							.set(PROJECT_RULE_TAG.PROJECT_ID, projectId)
							.set(PROJECT_RULE_TAG.TAG, ruleTag)
							.onDuplicateKeyIgnore()
							.execute();
					}
				}
			}
		});
	}
}
