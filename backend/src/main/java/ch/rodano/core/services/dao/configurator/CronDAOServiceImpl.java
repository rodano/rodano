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

import ch.rodano.api.config.CronDTO;
import ch.rodano.core.model.jooq.tables.records.CronRecord;

import static ch.rodano.core.model.jooq.tables.Cron.CRON;

@Repository
public class CronDAOServiceImpl implements CronDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public CronDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "crons", key = "#projectId.toString()")
	public List<CronDTO> getCrons(final UUID projectId) {
		final var records = dslContext
			.selectFrom(CRON)
			.where(CRON.PROJECT_ID.eq(projectId))
			.orderBy(CRON.CODE)
			.fetch();

		return records.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "cron", key = "#projectId.toString() + ':' + #cronId.toString()")
	public CronDTO getCron(final UUID projectId, final UUID cronId) {
		final var record = dslContext
			.selectFrom(CRON)
			.where(CRON.PROJECT_ID.eq(projectId))
			.and(CRON.CRON_ID.eq(cronId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record);
	}

	@Override
	@Transactional
	@CacheEvict(value = "crons", key = "#projectId.toString()")
	public CronDTO createCron(final UUID projectId, final CronDTO dto) {
		final var cronId = dto.getCronId() != null ? dto.getCronId() : UUID.randomUUID();

		dslContext.insertInto(CRON)
			.set(CRON.CRON_ID, cronId)
			.set(CRON.PROJECT_ID, projectId)
			.set(CRON.CODE, dto.getId())
			.set(CRON.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(CRON.INTERVAL_VALUE, dto.getIntervalValue())
			.set(CRON.INTERVAL_UNIT, dto.getIntervalUnit())
			.execute();

		return getCron(projectId, cronId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "crons", key = "#projectId.toString()"),
		@CacheEvict(value = "cron", key = "#projectId.toString() + ':' + #cronId.toString()")
	})
	public CronDTO updateCron(final UUID projectId, final UUID cronId, final CronDTO dto) {
		dslContext.update(CRON)
			.set(CRON.CODE, dto.getId())
			.set(CRON.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(CRON.INTERVAL_VALUE, dto.getIntervalValue())
			.set(CRON.INTERVAL_UNIT, dto.getIntervalUnit())
			.where(CRON.PROJECT_ID.eq(projectId))
			.and(CRON.CRON_ID.eq(cronId))
			.execute();

		return getCron(projectId, cronId);
	}

	@Override
	@Caching(evict = {
		@CacheEvict(value = "crons", key = "#projectId.toString()"),
		@CacheEvict(value = "cron", key = "#projectId.toString() + ':' + #cronId.toString()")
	})
	public void deleteCron(final UUID projectId, final UUID cronId) {
		dslContext.deleteFrom(CRON)
			.where(CRON.PROJECT_ID.eq(projectId))
			.and(CRON.CRON_ID.eq(cronId))
			.execute();
	}

	private CronDTO mapToDTO(final CronRecord record) {
		final var dto = new CronDTO();
		dto.setCronId(record.getCronId());
		dto.setId(record.getCode());
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setIntervalValue(record.getIntervalValue());
		dto.setIntervalUnit(record.getIntervalUnit());
		return dto;
	}
}
