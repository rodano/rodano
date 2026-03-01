package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.PrivacyPolicyDTO;
import ch.rodano.core.model.jooq.tables.records.PrivacyPolicyRecord;

import static ch.rodano.core.model.jooq.tables.PrivacyPolicy.PRIVACY_POLICY;
import static ch.rodano.core.model.jooq.tables.PrivacyPolicyProfile.PRIVACY_POLICY_PROFILE;

@Repository
public class PrivacyPolicyDAOServiceImpl implements PrivacyPolicyDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public PrivacyPolicyDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "privacy-policies", key = "#projectId.toString()")
	public List<PrivacyPolicyDTO> getPrivacyPolicies(final UUID projectId) {
		final var records = dslContext.selectFrom(PRIVACY_POLICY)
			.where(PRIVACY_POLICY.PROJECT_ID.eq(projectId))
			.orderBy(PRIVACY_POLICY.CODE)
			.fetch();

		return records.stream()
			.map(record -> mapToDTO(record, getProfileIds(record.getPolicyId())))
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "privacy-policy", key = "#projectId.toString() + ':' + #privacyPolicyId.toString()")
	public PrivacyPolicyDTO getPrivacyPolicy(final UUID projectId, final UUID privacyPolicyId) {
		final var record = dslContext.selectFrom(PRIVACY_POLICY)
			.where(PRIVACY_POLICY.PROJECT_ID.eq(projectId))
			.and(PRIVACY_POLICY.POLICY_ID.eq(privacyPolicyId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record, getProfileIds(privacyPolicyId));
	}

	@Override
	@Transactional
	@CacheEvict(value = "privacy-policies", key = "#projectId.toString()")
	public PrivacyPolicyDTO createPrivacyPolicy(final UUID projectId, final PrivacyPolicyDTO dto) {
		final var policyId = dto.policyId() != null ? dto.policyId() : UUID.randomUUID();

		dslContext.insertInto(PRIVACY_POLICY)
			.set(PRIVACY_POLICY.POLICY_ID, policyId)
			.set(PRIVACY_POLICY.PROJECT_ID, projectId)
			.set(PRIVACY_POLICY.CODE, dto.id())
			.set(PRIVACY_POLICY.SHORTNAME, jsonMapperService.toJson(dto.shortname()))
			.set(PRIVACY_POLICY.LONGNAME, jsonMapperService.toJson(dto.longname()))
			.set(PRIVACY_POLICY.DESCRIPTION, jsonMapperService.toJson(dto.description()))
			.set(PRIVACY_POLICY.CONTENT, jsonMapperService.toJson(dto.content()))
			.execute();

		saveProfileIds(projectId, policyId, dto.profileIds());

		return getPrivacyPolicy(projectId, policyId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "privacy-policies", key = "#projectId.toString()"),
		@CacheEvict(value = "privacy-policy", key = "#projectId.toString() + ':' + #privacyPolicyId.toString()")
	})
	public PrivacyPolicyDTO updatePrivacyPolicy(final UUID projectId, final UUID privacyPolicyId, final PrivacyPolicyDTO dto) {
		dslContext.update(PRIVACY_POLICY)
			.set(PRIVACY_POLICY.CODE, dto.id())
			.set(PRIVACY_POLICY.SHORTNAME, jsonMapperService.toJson(dto.shortname()))
			.set(PRIVACY_POLICY.LONGNAME, jsonMapperService.toJson(dto.longname()))
			.set(PRIVACY_POLICY.DESCRIPTION, jsonMapperService.toJson(dto.description()))
			.set(PRIVACY_POLICY.CONTENT, jsonMapperService.toJson(dto.content()))
			.where(PRIVACY_POLICY.PROJECT_ID.eq(projectId))
			.and(PRIVACY_POLICY.POLICY_ID.eq(privacyPolicyId))
			.execute();

		saveProfileIds(projectId, privacyPolicyId, dto.profileIds());

		return getPrivacyPolicy(projectId, privacyPolicyId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "privacy-policies", key = "#projectId.toString()"),
		@CacheEvict(value = "privacy-policy", key = "#projectId.toString() + ':' + #privacyPolicyId.toString()")
	})
	public void deletePrivacyPolicy(final UUID projectId, final UUID privacyPolicyId) {
		dslContext.deleteFrom(PRIVACY_POLICY_PROFILE)
			.where(PRIVACY_POLICY_PROFILE.PROJECT_ID.eq(projectId))
			.and(PRIVACY_POLICY_PROFILE.POLICY_ID.eq(privacyPolicyId))
			.execute();

		dslContext.deleteFrom(PRIVACY_POLICY)
			.where(PRIVACY_POLICY.PROJECT_ID.eq(projectId))
			.and(PRIVACY_POLICY.POLICY_ID.eq(privacyPolicyId))
			.execute();
	}

	private List<UUID> getProfileIds(final UUID policyId) {
		return dslContext.select(PRIVACY_POLICY_PROFILE.PROFILE_ID)
			.from(PRIVACY_POLICY_PROFILE)
			.where(PRIVACY_POLICY_PROFILE.POLICY_ID.eq(policyId))
			.fetch(PRIVACY_POLICY_PROFILE.PROFILE_ID);
	}

	private void saveProfileIds(final UUID projectId, final UUID policyId, final List<UUID> profileIds) {
		dslContext.deleteFrom(PRIVACY_POLICY_PROFILE)
			.where(PRIVACY_POLICY_PROFILE.PROJECT_ID.eq(projectId))
			.and(PRIVACY_POLICY_PROFILE.POLICY_ID.eq(policyId))
			.execute();

		if(profileIds == null || profileIds.isEmpty()) {
			return;
		}

		final var insert = dslContext.insertInto(PRIVACY_POLICY_PROFILE,
			PRIVACY_POLICY_PROFILE.PROJECT_ID,
			PRIVACY_POLICY_PROFILE.POLICY_ID,
			PRIVACY_POLICY_PROFILE.PROFILE_ID);

		profileIds.forEach(profileId -> insert.values(projectId, policyId, profileId));
		insert.execute();
	}

	private PrivacyPolicyDTO mapToDTO(final PrivacyPolicyRecord record, final List<UUID> profileIds) {
		return new PrivacyPolicyDTO(
			record.getPolicyId(),
			record.getCode(),
			jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
			}),
			jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
			}),
			jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
			}),
			jsonMapperService.fromJson(record.getContent(), new TypeReference<TreeMap<String, String>>() {
			}),
			profileIds
		);
	}
}
