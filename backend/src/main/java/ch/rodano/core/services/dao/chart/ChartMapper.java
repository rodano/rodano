package ch.rodano.core.services.dao.chart;

import java.util.Optional;

import org.jooq.Record;

import static ch.rodano.core.model.jooq.tables.Chart.CHART;

public class ChartMapper {

	private ChartMapper() {} //static utility class

	public static ChartDTO toChartDTO(final Record record) {
		final var chartDTO = new ChartDTO();
		chartDTO.setChartId(record.get(CHART.CHART_ID));
		chartDTO.setChartType(record.get(CHART.CHART_TYPE));
		chartDTO.setTitle(record.get(CHART.TITLE));
		chartDTO.setxLabel(record.get(CHART.X_LABEL));
		chartDTO.setyLabel(record.get(CHART.Y_LABEL));

		final var chartConfig = new ChartDTO.ChartConfig();
		chartConfig.setGraphType(record.get(CHART.GRAPH_TYPE));
		chartConfig.setUnitFormat(record.get(CHART.UNIT_FORMAT));
		chartConfig.setIgnoreNA(record.get(CHART.IGNORE_NA));
		chartConfig.setShowXAxisLabel(record.get(CHART.SHOW_X_AXIS_LABEL));
		chartConfig.setShowYAxisLabel(record.get(CHART.SHOW_Y_AXIS_LABEL));
		chartConfig.setShowDataLabels(record.get(CHART.SHOW_DATA_LABELS));
		chartConfig.setDataLabelPos(record.get(CHART.DATA_LABEL_POS));
		chartConfig.setDataLabelFormat(record.get(CHART.DATA_LABEL_FORMAT));
		chartConfig.setShowLegend(record.get(CHART.SHOW_LEGEND));
		chartConfig.setShowGridlines(record.get(CHART.SHOW_GRIDLINES));
		chartConfig.setBackgroundColor(record.get(CHART.BACKGROUND_COLOR));
		chartConfig.setHeaderColor(record.get(CHART.HEADER_COLOR));
		chartDTO.setChartConfig(chartConfig);

		final var requestParams = new ChartDTO.RequestParams();
		requestParams.setScopeModelId(record.get(CHART.SCOPE_MODEL_ID));
		requestParams.setLeafScopeModelId(record.get(CHART.LEAF_SCOPE_MODEL_ID));
		requestParams.setDatasetModelId(record.get(CHART.DATASET_MODEL_ID));
		requestParams.setFieldModelId(record.get(CHART.FIELD_MODEL_ID));
		requestParams.setEventModelId(record.get(CHART.EVENT_MODEL_ID));
		requestParams.setWorkflowId(record.get(CHART.WORKFLOW_ID));
		requestParams.setShowOtherCategory(
            Optional.ofNullable(record.get(CHART.SHOW_OTHER_CATEGORY)).orElse(Boolean.FALSE)
        );
		requestParams.setIgnoreUserRights(
			Optional.ofNullable(record.get(CHART.IGNORE_USER_RIGHTS)).orElse(Boolean.FALSE)
		);
		chartDTO.setRequestParams(requestParams);

		return chartDTO;
	}
}
