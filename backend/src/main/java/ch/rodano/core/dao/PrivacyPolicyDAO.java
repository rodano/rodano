package ch.rodano.core.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.policy.PrivacyPolicy;
import ch.rodano.core.model.jooq.tables.records.PrivacyPolicyRecord;

import static ch.rodano.core.model.jooq.tables.PrivacyPolicy.PRIVACY_POLICY;
import static ch.rodano.core.model.jooq.tables.PrivacyPolicyProfile.PRIVACY_POLICY_PROFILE;
import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;

@Repository
public class PrivacyPolicyDAO implements BaseProjectDAO<PrivacyPolicy> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public PrivacyPolicyDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	@Override
	public List<PrivacyPolicy> findByProject(final UUID projectId) {
		return dslContext.selectFrom(PRIVACY_POLICY)
			.where(PRIVACY_POLICY.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public PrivacyPolicy findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(PRIVACY_POLICY)
			.where(PRIVACY_POLICY.PROJECT_ID.eq(projectId))
			.and(PRIVACY_POLICY.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public PrivacyPolicy findById(final UUID id) {
		return dslContext.selectFrom(PRIVACY_POLICY)
			.where(PRIVACY_POLICY.POLICY_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public PrivacyPolicy save(final PrivacyPolicy entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private PrivacyPolicy mapToModel(final PrivacyPolicyRecord record) {
		if(record == null) {
			return null;
		}

		final PrivacyPolicy model = new PrivacyPolicy();

		model.setPrivacyPolicyId(record.getPolicyId());
		model.setId(record.getCode());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		model.setContent(mappingHelper.parseJsonToMap(record.getContent()));

		model.setProfileIds(loadProfileIds(record.getPolicyId()));

		return model;
	}

	private Set<String> loadProfileIds(final UUID policyId) {
		final var profileCodes = dslContext.select(PROFILE.CODE)
			.from(PRIVACY_POLICY_PROFILE)
			.join(PROFILE).on(PROFILE.PROFILE_ID.eq(PRIVACY_POLICY_PROFILE.PROFILE_ID))
			.where(PRIVACY_POLICY_PROFILE.POLICY_ID.eq(policyId))
			.fetch(PROFILE.CODE);

		return new HashSet<>(profileCodes);
	}
}
