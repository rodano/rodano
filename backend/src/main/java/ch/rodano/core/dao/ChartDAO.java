package ch.rodano.core.dao;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.chart.ChartType;
import ch.rodano.core.model.jooq.enums.ChartStateFilterKind;
import ch.rodano.core.model.jooq.tables.records.ChartRecord;

import static ch.rodano.core.model.jooq.tables.Chart.CHART;
import static ch.rodano.core.model.jooq.tables.ChartColor.CHART_COLOR;
import static ch.rodano.core.model.jooq.tables.ChartStateFilter.CHART_STATE_FILTER;
import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;

@Repository
public class ChartDAO implements BaseProjectDAO<Chart> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final ChartRangeDAO chartRangeDAO;

	public ChartDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final ChartRangeDAO chartRangeDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.chartRangeDAO = chartRangeDAO;
	}

	@Override
	public List<Chart> findByProject(final UUID projectId) {
		return dslContext.selectFrom(CHART)
			.where(CHART.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public Chart findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(CHART)
			.where(CHART.PROJECT_ID.eq(projectId))
			.and(CHART.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Chart findById(final UUID id) {
		return dslContext.selectFrom(CHART)
			.where(CHART.CHART_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Chart save(final Chart entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private Chart mapToModel(final ChartRecord record) {
		if(record == null) {
			return null;
		}

		final Chart model = new Chart();

		model.setId(record.getCode());
		model.setChartId(record.getChartId());

		model.setType(mappingHelper.parseEnum(ChartType.class, record.getType(), "type"));
		model.setOverrideUserRights(record.getOverrideUserRights() != null ? record.getOverrideUserRights() : false);
		model.setWithStatistics(record.getWithStatistics() != null ? record.getWithStatistics() : false);
		model.setDisplayExpected(record.getDisplayExpected() != null ? record.getDisplayExpected() : false);

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));
		model.setTitle(mappingHelper.parseJsonToMap(record.getTitle()));
		model.setLegendX(mappingHelper.parseJsonToMap(record.getLegendX()));
		model.setLegendY(mappingHelper.parseJsonToMap(record.getLegendY()));

		if(record.getWorkflowId() != null) {
			model.setWorkflowId(getWorkflowCode(record.getWorkflowId()));
		}

		if(record.getScopeModelId() != null) {
			model.setScopeModelId(getScopeModelCode(record.getScopeModelId()));
		}

		if(record.getLeafScopeModelId() != null) {
			model.setLeafScopeModelId(getScopeModelCode(record.getLeafScopeModelId()));
		}

		// Statistics chart fields
		if(record.getDatasetModelId() != null) {
			model.setDatasetModelId(getDatasetModelCode(record.getDatasetModelId()));
		}

		if(record.getFieldModelId() != null) {
			model.setFieldModelId(getFieldModelCode(record.getFieldModelId()));
		}

		model.setRanges(chartRangeDAO.findByChart(record.getChartId()));
		model.setColors(loadColors(record.getChartId()));

		loadStateFilters(record.getChartId(), model);
		loadEnrollmentStates(record.getChartId(), model);

		return model;
	}

	private List<String> loadColors(final UUID chartId) {
		return dslContext.select(CHART_COLOR.COLOR)
			.from(CHART_COLOR)
			.where(CHART_COLOR.CHART_ID.eq(chartId))
			.orderBy(CHART_COLOR.SORT_ORDER)
			.fetch(CHART_COLOR.COLOR);
	}

	private void loadStateFilters(final UUID chartId, final Chart model) {
		final var stateFilters = dslContext.selectFrom(CHART_STATE_FILTER)
			.where(CHART_STATE_FILTER.CHART_ID.eq(chartId))
			.fetch();

		final Set<String> includedStates = new TreeSet<>();
		final Set<String> excludedStates = new TreeSet<>();

		for(var filter : stateFilters) {
			final String stateCode = getWorkflowStateCode(filter.getWorkflowStateId());

			if(filter.getKind() == ChartStateFilterKind.INCLUDED) {
				includedStates.add(stateCode);
			}
			else if(filter.getKind() == ChartStateFilterKind.EXCLUDED) {
				excludedStates.add(stateCode);
			}
		}

		if(!includedStates.isEmpty()) {
			model.setIncludedStateIds(includedStates);
		}
		if(!excludedStates.isEmpty()) {
			model.setExcludedStateIds(excludedStates);
		}
	}

	private void loadEnrollmentStates(final UUID chartId, final Chart model) {
		final var enrollmentFilters = dslContext.selectFrom(CHART_STATE_FILTER)
			.where(CHART_STATE_FILTER.CHART_ID.eq(chartId))
			.and(CHART_STATE_FILTER.KIND.eq(ChartStateFilterKind.ENROLLMENT))
			.fetch();

		if(!enrollmentFilters.isEmpty()) {
			final Set<String> enrollmentStates = new TreeSet<>();

			final UUID firstStateId = enrollmentFilters.getFirst().getWorkflowStateId();
			final UUID enrollmentWorkflowId = getWorkflowIdFromState(firstStateId);

			if(enrollmentWorkflowId != null) {
				final String enrollmentWorkflowCode = getWorkflowCode(enrollmentWorkflowId);
				model.setEnrollmentWorkflowId(enrollmentWorkflowCode);
			}

			for(var filter : enrollmentFilters) {
				final String stateCode = getWorkflowStateCode(filter.getWorkflowStateId());
				enrollmentStates.add(stateCode);
			}

			model.setEnrollmentStateIds(enrollmentStates);
		}
	}

	private String getWorkflowCode(final UUID workflowId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(WORKFLOW)
			.where(WORKFLOW.WORKFLOW_ID.eq(workflowId))
			.fetchOne(WORKFLOW.CODE);
	}

	private String getScopeModelCode(final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL.CODE)
			.from(SCOPE_MODEL)
			.where(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetchOne(SCOPE_MODEL.CODE);
	}

	private String getDatasetModelCode(final UUID datasetModelId) {
		return dslContext.select(DATASET_MODEL.CODE)
			.from(DATASET_MODEL)
			.where(DATASET_MODEL.DATASET_MODEL_ID.eq(datasetModelId))
			.fetchOne(DATASET_MODEL.CODE);
	}

	private String getFieldModelCode(final UUID fieldModelId) {
		return dslContext.select(FIELD_MODEL.CODE)
			.from(FIELD_MODEL)
			.where(FIELD_MODEL.FIELD_MODEL_ID.eq(fieldModelId))
			.fetchOne(FIELD_MODEL.CODE);
	}

	private String getWorkflowStateCode(final UUID stateId) {
		return dslContext.select(WORKFLOW_STATE.CODE)
			.from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(stateId))
			.fetchOne(WORKFLOW_STATE.CODE);
	}

	private UUID getWorkflowIdFromState(final UUID stateId) {
		return dslContext.select(WORKFLOW_STATE.WORKFLOW_ID)
			.from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(stateId))
			.fetchOne(WORKFLOW_STATE.WORKFLOW_ID);
	}
}
