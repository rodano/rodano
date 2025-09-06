package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.PrivacyPolicy;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveProfileId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.PrivacyPolicy.PRIVACY_POLICY;
import static ch.rodano.core.model.jooq.tables.PrivacyPolicyProfile.PRIVACY_POLICY_PROFILE;

public class PrivacyPolicyWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<PrivacyPolicy> wrapped = (ProjectScoped<PrivacyPolicy>) raw;
				final UUID projectId = wrapped.getProjectId();
				final PrivacyPolicy policy = wrapped.getPayload();

				final String policyCode = policy.getId();
				final UUID policyId = deterministic(projectId, "PRIVACY_POLICY", policyCode);
				tx.insertInto(PRIVACY_POLICY)
					.set(PRIVACY_POLICY.PROJECT_ID, projectId)
					.set(PRIVACY_POLICY.POLICY_ID, policyId)
					.set(PRIVACY_POLICY.CODE, policyCode)
					.set(PRIVACY_POLICY.SHORTNAME, toJson(policy.getShortname()))
					.set(PRIVACY_POLICY.LONGNAME, toJson(policy.getLongname()))
					.set(PRIVACY_POLICY.DESCRIPTION, toJson(policy.getDescription()))
					.set(PRIVACY_POLICY.CONTENT, toJson(policy.getContent()))
					.onDuplicateKeyUpdate()
					.set(PRIVACY_POLICY.CODE, policyCode)
					.set(PRIVACY_POLICY.SHORTNAME, toJson(policy.getShortname()))
					.set(PRIVACY_POLICY.LONGNAME, toJson(policy.getLongname()))
					.set(PRIVACY_POLICY.DESCRIPTION, toJson(policy.getDescription()))
					.set(PRIVACY_POLICY.CONTENT, toJson(policy.getContent()))
					.execute();

				if(policy.getProfileIds() != null && !policy.getProfileIds().isEmpty()) {
					for(String profileCode : policy.getProfileIds()) {
						final UUID profileId = resolveProfileId(tx, projectId, profileCode);
						tx.insertInto(PRIVACY_POLICY_PROFILE)
							.set(PRIVACY_POLICY_PROFILE.PROJECT_ID, projectId)
							.set(PRIVACY_POLICY_PROFILE.POLICY_ID, policyId)
							.set(PRIVACY_POLICY_PROFILE.PROFILE_ID, profileId)
							.onDuplicateKeyIgnore()
							.execute();
					}
				}

			}
		});
	}
}
