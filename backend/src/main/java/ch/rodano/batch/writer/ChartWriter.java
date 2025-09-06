package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Chart;
import ch.rodano.batch.pojo.ChartRange;
import ch.rodano.core.model.jooq.enums.ChartStateFilterKind;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveDatasetModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFieldModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveScopeModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowStateId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Chart.CHART;
import static ch.rodano.core.model.jooq.tables.ChartColor.CHART_COLOR;
import static ch.rodano.core.model.jooq.tables.ChartRange.CHART_RANGE;
import static ch.rodano.core.model.jooq.tables.ChartStateFilter.CHART_STATE_FILTER;

public class ChartWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Chart> wrapped = (ProjectScoped<Chart>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Chart chart = wrapped.getPayload();

				final UUID workflowId = resolveWorkflowId(tx, projectId, chart.getWorkflowId());
				final UUID scopeModelId = resolveScopeModelId(tx, projectId, chart.getScopeModelId());
				final UUID leafScopeModelId = resolveScopeModelId(tx, projectId, chart.getLeafScopeModelId());
				final UUID datasetModelId = resolveDatasetModelId(tx, projectId, chart.getDatasetModelId());
				final UUID fieldModelId = resolveFieldModelId(tx, projectId, datasetModelId, chart.getFieldModelId());

				final String chartCode = chart.getId();
				final UUID chartId = deterministic(projectId, "CHART", chartCode);
				tx.insertInto(CHART)
					.set(CHART.PROJECT_ID, projectId)
					.set(CHART.CHART_ID, chartId)
					.set(CHART.CODE, chartCode)
					.set(CHART.TYPE, chart.getType())
					.set(CHART.OVERRIDE_USER_RIGHTS, chart.getOverrideUserRights())
					.set(CHART.WITH_STATISTICS, chart.getWithStatistics())
					.set(CHART.DISPLAY_EXPECTED, chart.getDisplayExpected())
					.set(CHART.WORKFLOW_ID, workflowId)
					.set(CHART.SCOPE_MODEL_ID, scopeModelId)
					.set(CHART.LEAF_SCOPE_MODEL_ID, leafScopeModelId)
					.set(CHART.DATASET_MODEL_ID, datasetModelId)
					.set(CHART.FIELD_MODEL_ID, fieldModelId)
					.set(CHART.SHORTNAME, toJson(chart.getShortname()))
					.set(CHART.LONGNAME, toJson(chart.getLongname()))
					.set(CHART.DESCRIPTION, toJson(chart.getDescription()))
					.set(CHART.TITLE, toJson(chart.getTitle()))
					.set(CHART.LEGEND_X, toJson(chart.getLegendX()))
					.set(CHART.LEGEND_Y, toJson(chart.getLegendY()))
					.onDuplicateKeyUpdate()
					.set(CHART.TYPE, chart.getType())
					.set(CHART.OVERRIDE_USER_RIGHTS, chart.getOverrideUserRights())
					.set(CHART.WITH_STATISTICS, chart.getWithStatistics())
					.set(CHART.DISPLAY_EXPECTED, chart.getDisplayExpected())
					.set(CHART.WORKFLOW_ID, workflowId)
					.set(CHART.SCOPE_MODEL_ID, scopeModelId)
					.set(CHART.LEAF_SCOPE_MODEL_ID, leafScopeModelId)
					.set(CHART.DATASET_MODEL_ID, datasetModelId)
					.set(CHART.FIELD_MODEL_ID, fieldModelId)
					.set(CHART.SHORTNAME, toJson(chart.getShortname()))
					.set(CHART.LONGNAME, toJson(chart.getLongname()))
					.set(CHART.DESCRIPTION, toJson(chart.getDescription()))
					.set(CHART.TITLE, toJson(chart.getTitle()))
					.set(CHART.LEGEND_X, toJson(chart.getLegendX()))
					.set(CHART.LEGEND_Y, toJson(chart.getLegendY()))
					.execute();

				if(chart.getColors() != null && !chart.getColors().isEmpty()) {
					int colOrder = 0;
					for(String color : chart.getColors()) {
						tx.insertInto(CHART_COLOR)
							.set(CHART_COLOR.PROJECT_ID, projectId)
							.set(CHART_COLOR.CHART_ID, chartId)
							.set(CHART_COLOR.SORT_ORDER, colOrder++)
							.set(CHART_COLOR.COLOR, color)
							.onDuplicateKeyUpdate()
							.set(CHART_COLOR.COLOR, color)
							.execute();
					}
				}

				if(chart.getRanges() != null && !chart.getRanges().isEmpty()) {
					int rangeOrder = 0;
					for(ChartRange range : chart.getRanges()) {
						final String rangeCode = range.getId();
						tx.insertInto(CHART_RANGE)
							.set(CHART_RANGE.PROJECT_ID, projectId)
							.set(CHART_RANGE.CHART_ID, chartId)
							.set(CHART_RANGE.CODE, rangeCode)
							.set(CHART_RANGE.VALUE, range.getValue())
							.set(CHART_RANGE.MIN, range.getMin())
							.set(CHART_RANGE.MAX, range.getMax())
							.set(CHART_RANGE.IS_OTHER, range.getOther())
							.set(CHART_RANGE.SORT_ORDER, rangeOrder++)
							.set(CHART_RANGE.LABEL, toJson(range.getLabels()))
							.onDuplicateKeyUpdate()
							.set(CHART_RANGE.VALUE, range.getValue())
							.set(CHART_RANGE.MIN, range.getMin())
							.set(CHART_RANGE.MAX, range.getMax())
							.set(CHART_RANGE.IS_OTHER, range.getOther())
							.set(CHART_RANGE.SORT_ORDER, rangeOrder - 1)
							.set(CHART_RANGE.LABEL, toJson(range.getLabels()))
							.execute();
					}
				}

				insertStateFilters(tx, projectId, chart, chartId, chart.getIncludedStateIds(), ChartStateFilterKind.INCLUDED);
				insertStateFilters(tx, projectId, chart, chartId, chart.getExcludedStateIds(), ChartStateFilterKind.EXCLUDED);
				insertStateFilters(tx, projectId, chart, chartId, chart.getEnrollmentStateIds(), ChartStateFilterKind.ENROLLMENT);
			}
		});
	}

	private static void insertStateFilters(final DSLContext tx,
										   final UUID projectId,
										   final Chart chart,
										   final UUID chartId,
										   final List<String> stateCodes,
										   final ChartStateFilterKind kind) {

		if(stateCodes == null || stateCodes.isEmpty()) {
			return;
		}
		final UUID workflowId = resolveWorkflowId(tx, projectId, chart.getWorkflowId());
		for(String code : stateCodes) {
			final UUID stateId = resolveWorkflowStateId(tx, projectId, workflowId, code);
			tx.insertInto(CHART_STATE_FILTER)
				.set(CHART_STATE_FILTER.PROJECT_ID, projectId)
				.set(CHART_STATE_FILTER.CHART_ID, chartId)
				.set(CHART_STATE_FILTER.WORKFLOW_STATE_ID, stateId)
				.set(CHART_STATE_FILTER.KIND, kind)
				.onDuplicateKeyIgnore()
				.execute();
		}
	}
}
