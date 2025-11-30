package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.chart.ChartRange;
import ch.rodano.core.model.jooq.tables.records.ChartRangeRecord;

import static ch.rodano.core.model.jooq.tables.ChartRange.CHART_RANGE;

@Repository
public class ChartRangeDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public ChartRangeDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	public List<ChartRange> findByChart(final UUID chartId) {
		return dslContext.selectFrom(CHART_RANGE)
			.where(CHART_RANGE.CHART_ID.eq(chartId))
			.orderBy(CHART_RANGE.SORT_ORDER)
			.fetch(this::mapToModel);
	}

	private ChartRange mapToModel(final ChartRangeRecord record) {
		if(record == null) {
			return null;
		}

		final ChartRange model = new ChartRange();

		model.setId(record.getCode());
		model.setChartRangeId(record.getChartRangeId());
		model.setValue(record.getValue());
		model.setOther(record.getIsOther() != null ? record.getIsOther() : false);

		if(record.getMin() != null) {
			model.setMin(record.getMin().doubleValue());
		}
		if(record.getMax() != null) {
			model.setMax(record.getMax().doubleValue());
		}

		model.setLabels(mappingHelper.parseJsonToMap(record.getLabel()));

		return model;
	}
}
