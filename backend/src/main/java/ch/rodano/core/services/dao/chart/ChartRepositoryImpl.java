package ch.rodano.core.services.dao.chart;

import java.util.List;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.model.jooq.tables.Chart;
import ch.rodano.core.model.jooq.tables.ChartCategory;
import ch.rodano.core.model.jooq.tables.ChartColor;
import ch.rodano.core.model.jooq.tables.ChartState;
import ch.rodano.core.model.jooq.tables.records.ChartRecord;

import static ch.rodano.core.model.jooq.Tables.CHART;
import static ch.rodano.core.model.jooq.Tables.CHART_CATEGORY;
import static ch.rodano.core.model.jooq.Tables.CHART_COLOR;
import static ch.rodano.core.model.jooq.Tables.CHART_STATE;

@Service
public class ChartRepositoryImpl implements ChartRepository {
	private final DSLContext dsl;
	private static final Logger LOGGER = LoggerFactory.getLogger(ChartRepositoryImpl.class);

	public ChartRepositoryImpl(final DSLContext dsl) {
		this.dsl = dsl;
	}

	@Override
	public ChartDTO findByChartId(final String chartId) {
		final var record = dsl.select()
			.from(CHART)
			.where(CHART.CHART_ID.eq(chartId))
			.fetchOne();

		if (record == null) {
			throw new NotFoundException(ChartDTO.class, chartId);
		}
		else {
			LOGGER.info("[ChartRepository] Received record: {}", record);
		}

		final var chartDTO = ChartMapper.toChartDTO(record);
		chartDTO.getChartConfig().setColors(loadColors(record.get(Chart.CHART.PK)));
		chartDTO.getRequestParams().setStateIds(loadStates(record.get(Chart.CHART.PK)));
		chartDTO.getRequestParams().setCategories(loadCategories(record.get(Chart.CHART.PK)));
		return chartDTO;
	}

	@Override
	public List<ChartDTO> findAll() {
		return dsl.select()
			.from(CHART)
			.fetch()
			.map(record -> {
				final var chartDTO = ChartMapper.toChartDTO(record);
				final var pk = record.get(CHART.PK);
				chartDTO.getChartConfig().setColors(loadColors(pk));
				chartDTO.getRequestParams().setStateIds(loadStates(pk));
				chartDTO.getRequestParams().setCategories(loadCategories(pk));
				return chartDTO;
			});
	}

	@Override
	public ChartDTO save(final ChartDTO dto) {
		LOGGER.debug("[ChartRepository] Attempting to save chart with id: {}", dto.getChartId());

		// 1) Populate the chart record
		final var rec = dsl.newRecord(CHART);
		applyDtoToChartRecord(rec, dto);

		// 2) Store it and get the primary key
		rec.insert();
		final var pk = rec.getPk();

		LOGGER.debug("[ChartRepository] Saved chart with id: {} and PK: {}", dto.getChartId(), pk);

		// 3) Use the PK (Primary key) to insert related color, state and category values
		insertColors(pk, dto);
		insertStates(pk, dto);
		insertCategories(pk, dto);

		return dto;
	}



	@Override
	public ChartDTO update(final ChartDTO dto) {
		LOGGER.debug("[ChartRepository] Attempting to update chart with id: {}", dto.getChartId());

		// 1) Load existing chart record (to get primary key)
		final var chartRecord = dsl.fetchOne(CHART, CHART.CHART_ID.eq(dto.getChartId()));
		if(chartRecord == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chart with ID '" + dto.getChartId() + "' not found");
		}
		final var chartPk = chartRecord.getPk();

		// 2) Update the chart record with the new values from the DTO
		applyDtoToChartRecord(chartRecord, dto);

		// 3) Persist the updated row
		chartRecord.update();
		LOGGER.debug("[ChartRepository] Updated chart with id: {}", dto.getChartId());

		// 4) Tear down and rebuild all the related tables
		dsl.deleteFrom(CHART_COLOR).where(CHART_COLOR.CHART_FK.eq(chartPk)).execute();
		insertColors(chartPk, dto);

		dsl.deleteFrom(CHART_STATE).where(CHART_STATE.CHART_FK.eq(chartPk)).execute();
		insertStates(chartPk, dto);

		dsl.deleteFrom(CHART_CATEGORY).where(CHART_CATEGORY.CHART_FK.eq(chartPk)).execute();
		insertCategories(chartPk, dto);

		// 5) Reload and return the fresh DTO
		return findByChartId(dto.getChartId());
	}



	@Override
	public void delete(final String chartId) {
		final var pk = dsl.select(CHART.PK)
			.from(CHART)
			.where(CHART.CHART_ID.eq(chartId))
			.fetchOne(CHART.PK);

		dsl.deleteFrom(CHART_CATEGORY).where(ChartCategory.CHART_CATEGORY.CHART_FK.eq(pk)).execute();
		dsl.deleteFrom(CHART_STATE).where(ChartState.CHART_STATE.CHART_FK.eq(pk)).execute();
		dsl.deleteFrom(CHART_COLOR).where(ChartColor.CHART_COLOR.CHART_FK.eq(pk)).execute();
		dsl.deleteFrom(CHART).where(Chart.CHART.PK.eq(pk)).execute();
	}

	// -- Helper functions to load related data --

	private List<String> loadColors(final Long chartPk) {
		return dsl.select(CHART_COLOR.COLOR)
			.from(CHART_COLOR)
			.where(CHART_COLOR.CHART_FK.eq(chartPk))
			.orderBy(CHART_COLOR.COLOR_ORDER)
			.fetchInto(String.class);
	}

	private List<String> loadStates(final Long chartPk) {
		return dsl.select(ChartState.CHART_STATE.STATE_ID)
			.from(ChartState.CHART_STATE)
			.where(ChartState.CHART_STATE.CHART_FK.eq(chartPk))
			.fetchInto(String.class);
	}

	private List<ChartDTO.Category> loadCategories(final Long chartPk) {
		return dsl.selectFrom(CHART_CATEGORY)
			.where(CHART_CATEGORY.CHART_FK.eq(chartPk))
			.fetchInto(ChartDTO.Category.class);
	}

	private void applyDtoToChartRecord(final ChartRecord chartRecord, final ChartDTO dto) {
		chartRecord.setChartId(dto.getChartId());
		chartRecord.setTitle(dto.getTitle());
		chartRecord.setXLabel(dto.getxLabel());
		chartRecord.setYLabel(dto.getyLabel());
		chartRecord.setChartType(dto.getChartType());

		if (dto.getChartConfig() != null) {
			final var config = dto.getChartConfig();
			chartRecord.setGraphType(config.getGraphType());
			chartRecord.setUnitFormat(config.getUnitFormat());
			chartRecord.setIgnoreNa(config.isIgnoreNA());
			chartRecord.setShowXAxisLabel(config.isShowXAxisLabel());
			chartRecord.setShowYAxisLabel(config.isShowYAxisLabel());
			chartRecord.setShowDataLabels(config.isShowDataLabels());
			chartRecord.setDataLabelPos(config.getDataLabelPos());
			chartRecord.setDataLabelFormat(config.getDataLabelFormat());
			chartRecord.setShowLegend(config.isShowLegend());
			chartRecord.setShowGridlines(config.isShowGridlines());
			chartRecord.setBackgroundColor(config.getBackgroundColor());
			chartRecord.setHeaderColor(config.getHeaderColor());
		}

		if (dto.getRequestParams() != null) {
			final var params = dto.getRequestParams();
			chartRecord.setWorkflowId(params.getWorkflowId());
			chartRecord.setScopeModelId(params.getScopeModelId());
			chartRecord.setLeafScopeModelId(params.getLeafScopeModelId());
			chartRecord.setDatasetModelId(params.getDatasetModelId());
			chartRecord.setFieldModelId(params.getFieldModelId());
			chartRecord.setEventModelId(params.getEventModelId());
			chartRecord.setShowOtherCategory(params.getShowOtherCategory());
			chartRecord.setIgnoreUserRights(params.getIgnoreUserRights());
		}
	}


	private void insertCategories(final Long chartPk, final ChartDTO dto) {
		if (dto.getRequestParams() != null && !dto.getRequestParams().getCategories().isEmpty()) {
			for (var cat : dto.getRequestParams().getCategories()) {
				dsl.insertInto(CHART_CATEGORY)
					.set(CHART_CATEGORY.CHART_FK, chartPk)
					.set(CHART_CATEGORY.LABEL, cat.getLabel())
					.set(CHART_CATEGORY.MIN, cat.getMin())
					.set(CHART_CATEGORY.MAX, cat.getMax())
					.set(CHART_CATEGORY.SHOW, cat.getShow())
					.execute();
			}
		}
	}

	private void insertColors(final Long chartPk, final ChartDTO dto) {
		if(!dto.getChartConfig().getColors().isEmpty()) {
			final var colors = dto.getChartConfig().getColors();
			for(var i = 0; i < colors.size(); i++) {
				final var colorRec = dsl.newRecord(CHART_COLOR);
				colorRec.setChartFk(chartPk);
				colorRec.setColorOrder(i);
				colorRec.setColor(colors.get(i));
				colorRec.store();
			}
		}
	}

	private void insertStates(final Long chartPk, final ChartDTO dto) {
		if(!dto.getRequestParams().getStateIds().isEmpty()) {
			final var states = dto.getRequestParams().getStateIds();
			for(var state : states) {
				//noinspection CheckStyle
				final var stateRec = dsl.newRecord(CHART_STATE);
				stateRec.setChartFk(chartPk);
				stateRec.setStateId(state);
				stateRec.store();
			}
		}
	}

}
