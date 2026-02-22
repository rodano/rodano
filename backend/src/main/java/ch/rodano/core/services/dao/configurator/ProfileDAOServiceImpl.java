package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.Map;
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

import ch.rodano.api.config.ProfileDTO;
import ch.rodano.core.model.jooq.tables.records.ProfileRecord;

import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;

@Repository
public class ProfileDAOServiceImpl implements ProfileDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public ProfileDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "profiles", key = "#projectId.toString()")
	public List<ProfileDTO> getProfiles(final UUID projectId) {
		final var profiles = dslContext.selectFrom(PROFILE)
			.where(PROFILE.PROJECT_ID.eq(projectId))
			.orderBy(PROFILE.ORDER_BY)
			.fetch();

		return profiles.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "profile", key = "#projectId.toString() + ':' + #profileId.toString()")
	public ProfileDTO getProfile(final UUID projectId, final UUID profileId) {
		final var record = dslContext.selectFrom(PROFILE)
			.where(PROFILE.PROJECT_ID.eq(projectId))
			.and(PROFILE.PROFILE_ID.eq(profileId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record);
	}

	@Override
	@Transactional
	@CacheEvict(value = "profiles", key = "#projectId.toString()")
	public ProfileDTO createProfile(final UUID projectId, final ProfileDTO dto) {
		final var profileId = dto.profileId() != null ? dto.profileId() : UUID.randomUUID();

		dslContext.insertInto(PROFILE)
			.set(PROFILE.PROFILE_ID, profileId)
			.set(PROFILE.PROJECT_ID, projectId)
			.set(PROFILE.CODE, dto.id())
			.set(PROFILE.ORDER_BY, dto.order())
			.set(PROFILE.WORKFLOW_OF_INTEREST_ID, dto.workflowOfInterestId())
			.set(PROFILE.SHORTNAME, jsonMapperService.toJson(dto.shortname()))
			.set(PROFILE.LONGNAME, jsonMapperService.toJson(dto.longname()))
			.set(PROFILE.DESCRIPTION, jsonMapperService.toJson(dto.description()))
			.execute();

		return getProfile(projectId, profileId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "profiles", key = "#projectId.toString()"),
		@CacheEvict(value = "profile", key = "#projectId.toString() + ':' + #profileId.toString()")
	})
	public ProfileDTO updateProfile(final UUID projectId, final UUID profileId, final ProfileDTO dto) {
		dslContext.update(PROFILE)
			.set(PROFILE.CODE, dto.id())
			.set(PROFILE.ORDER_BY, dto.order())
			.set(PROFILE.WORKFLOW_OF_INTEREST_ID, dto.workflowOfInterestId())
			.set(PROFILE.SHORTNAME, jsonMapperService.toJson(dto.shortname()))
			.set(PROFILE.LONGNAME, jsonMapperService.toJson(dto.longname()))
			.set(PROFILE.DESCRIPTION, jsonMapperService.toJson(dto.description()))
			.where(PROFILE.PROJECT_ID.eq(projectId))
			.and(PROFILE.PROFILE_ID.eq(profileId))
			.execute();

		return getProfile(projectId, profileId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "profiles", key = "#projectId.toString()"),
		@CacheEvict(value = "profile", key = "#projectId.toString() + ':' + #profileId.toString()")
	})
	public void deleteProfile(final UUID projectId, final UUID profileId) {
		dslContext.deleteFrom(PROFILE)
			.where(PROFILE.PROJECT_ID.eq(projectId))
			.and(PROFILE.PROFILE_ID.eq(profileId))
			.execute();
	}

	private ProfileDTO mapToDTO(final ProfileRecord record) {
		return new ProfileDTO(
			record.getProfileId(),
			record.getCode(),
			jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
			}),
			jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
			}),
			jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
			}),
			record.getOrderBy(),
			record.getWorkflowOfInterestId(),
			List.of(),
			Map.of(),
			List.of()
		);
	}
}
