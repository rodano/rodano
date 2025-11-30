package ch.rodano.core.dao;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.cron.Cron;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.model.jooq.tables.records.CronRecord;

import static ch.rodano.core.model.jooq.tables.Cron.CRON;

@Repository
public class CronDAO implements BaseProjectDAO<Cron> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final RuleDAO ruleDAO;

	public CronDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final RuleDAO ruleDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.ruleDAO = ruleDAO;
	}

	@Override
	public List<Cron> findByProject(final UUID projectId) {
		return dslContext.selectFrom(CRON)
			.where(CRON.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public Cron findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(CRON)
			.where(CRON.PROJECT_ID.eq(projectId))
			.and(CRON.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Cron findById(final UUID id) {
		return dslContext.selectFrom(CRON)
			.where(CRON.CRON_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Cron save(final Cron entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private Cron mapToModel(final CronRecord record) {
		if(record == null) {
			return null;
		}

		final Cron model = new Cron();

		model.setId(record.getCode());
		model.setCronId(record.getCronId());
		model.setInterval(record.getIntervalValue());

		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		model.setIntervalUnit(mappingHelper.parseEnum(ChronoUnit.class, record.getIntervalUnit(), "intervalUnit"));

		model.setRules(ruleDAO.findByEntity(RuleEntityType.CRON, record.getCronId()));

		return model;
	}
}
