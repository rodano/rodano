package ch.rodano.core.services.dao.chart;

import java.util.Collections;
import java.util.List;

public class ChartDTO {

	private String chartId;
	private ChartType chartType;
	private String title;
	private String xLabel;
	private String yLabel;
	private ChartConfig chartConfig;
	private RequestParams requestParams;
	private Data data;

	public static class ChartConfig {
		private String graphType;
		private String unitFormat;
		private boolean ignoreNA;
		private boolean showYAxisLabel;
		private boolean showXAxisLabel;
		private boolean showDataLabels;
		private String dataLabelPos;
		private String dataLabelFormat;
		private boolean showLegend;
		private boolean showGridlines;
		private String backgroundColor;
		private String headerColor;
		private List<String> colors;

		public String getGraphType() {
			return graphType;
		}
		public void setGraphType(final String graphType) {
			this.graphType = graphType;
		}
		public String getUnitFormat() {
			return unitFormat;
		}
		public void setUnitFormat(final String unitFormat) {
			this.unitFormat = unitFormat;
		}
		public boolean isIgnoreNA() {
			return ignoreNA;
		}
		public void setIgnoreNA(final boolean ignoreNA) {
			this.ignoreNA = ignoreNA;
		}
		public boolean isShowYAxisLabel() {
			return showYAxisLabel;
		}
		public void setShowYAxisLabel(final boolean showYAxisLabel) {
			this.showYAxisLabel = showYAxisLabel;
		}
		public boolean isShowXAxisLabel() {
			return showXAxisLabel;
		}
		public void setShowXAxisLabel(final boolean showXAxisLabel) {
			this.showXAxisLabel = showXAxisLabel;
		}
		public boolean isShowDataLabels() {
			return showDataLabels;
		}
		public void setShowDataLabels(final boolean showDataLabels) {
			this.showDataLabels = showDataLabels;
		}
		public String getDataLabelPos() {
			return dataLabelPos;
		}
		public void setDataLabelPos(final String dataLabelPos) {
			this.dataLabelPos = dataLabelPos;
		}
		public String getDataLabelFormat() {
			return dataLabelFormat;
		}
		public void setDataLabelFormat(final String dataLabelFormat) {
			this.dataLabelFormat = dataLabelFormat;
		}
		public boolean isShowLegend() {
			return showLegend;
		}
		public void setShowLegend(final boolean showLegend) {
			this.showLegend = showLegend;
		}
		public boolean isShowGridlines() {
			return showGridlines;
		}
		public void setShowGridlines(final boolean showGridlines) {
			this.showGridlines = showGridlines;
		}
		public String getBackgroundColor() {
			return backgroundColor;
		}
		public void setBackgroundColor(final String backgroundColor) {
			this.backgroundColor = backgroundColor;
		}
		public String getHeaderColor() {
			return headerColor;
		}
		public void setHeaderColor(final String headerColor) {
			this.headerColor = headerColor;
		}
		public List<String> getColors() {
			return colors;
		}
		public void setColors(final List<String> colors) {
			this.colors = colors;
		}
	}

	public static class RequestParams {
		private String scopeModelId;
		private String leafScopeModelId;
		private String datasetModelId;
		private String fieldModelId;
		private String eventModelId;
		private String workflowId;
		private boolean showOtherCategory;
		private boolean ignoreUserRights;
		private List<String> stateIds;
		private List<Category> categories;

		public String getScopeModelId() {
			return scopeModelId;
		}
		public void setScopeModelId(final String scopeModelId) {
			this.scopeModelId = scopeModelId;
		}
		public String getLeafScopeModelId() {
			return leafScopeModelId;
		}
		public void setLeafScopeModelId(final String leafScopeModelId) {
			this.leafScopeModelId = leafScopeModelId;
		}
		public String getDatasetModelId() {
			return datasetModelId;
		}
		public void setDatasetModelId(final String datasetModelId) {
			this.datasetModelId = datasetModelId;
		}
		public String getFieldModelId() {
			return fieldModelId;
		}
		public void setFieldModelId(final String fieldModelId) {
			this.fieldModelId = fieldModelId;
		}
		public String getEventModelId() {
			return eventModelId;
		}
		public void setEventModelId(final String eventModelId) {
			this.eventModelId = eventModelId;
		}
		public String getWorkflowId() {
			return workflowId;
		}
		public void setWorkflowId(final String workflowId) {
			this.workflowId = workflowId;
		}
		public List<String> getStateIds() {
			return stateIds == null ? Collections.emptyList() : stateIds;
		}
		public void setStateIds(final List<String> stateIds) {
			this.stateIds = stateIds;
		}
		public boolean getShowOtherCategory() {
			return showOtherCategory;
		}
		public void setShowOtherCategory(final Boolean showOtherCategory) {
			this.showOtherCategory = showOtherCategory;
		}
		public boolean getIgnoreUserRights() {
			return ignoreUserRights;
		}
		public void setIgnoreUserRights(final Boolean ignoreUserRights) {
			this.ignoreUserRights = ignoreUserRights;
		}
		public List<Category> getCategories() {
			return categories == null ? Collections.emptyList() : categories;
		}
		public void setCategories(final List<Category> categories) {
			this.categories = categories;
		}
	}

	public static class Category {
		private String label;
		private String min;
		private String max;
		private Boolean show;

		public Category() {}

		public Category(final String s, final String number, final String number1, final boolean b) {
			this.label = s;
			this.min = number;
			this.max = number1;
			this.show = b;
			if (this.min == null) {
				this.min = "-Infinity";
			}
		}

		public String toPrettyString(final String indent) {
			final var ni = indent + "  ";
			return indent + "{\n" +
				ni + "label: " + label + "\n" +
				ni + "min: " + min + "\n" +
				ni + "max: " + max + "\n" +
				ni + "show: " + show + "\n" +
				indent + "}";
        }

		public String getLabel() {
			return label;
		}
		public void setLabel(final String label) {
			this.label = label;
		}
		public String getMin() {
			return min;
		}
		public void setMin(final String min) {
			this.min = min;
		}
		public String getMax() {
			return max;
		}
		public void setMax(final String max) {
			this.max = max;
		}
		public Boolean getShow() {
			return show;
		}
		public void setShow(final Boolean show) {
			this.show = show;
		}
	}

	public static class ChartDataSeries {
		private String label;
		private List<List<Object>> values;

		public ChartDataSeries() {}

		public ChartDataSeries(final String label, final List<List<Object>> values) {
			this.label = label;
			this.values = values;
		}

		public String toPrettyString(final String indent) {
			final var ni = indent + "  ";
			return indent + "{\n" +
				ni + "label: " + label + "\n" +
				ni + "values: " + values + "\n" +
				indent + "}";
        }

		public String getLabel() {
			return label;
		}
		public void setLabel(final String label) {
			this.label = label;
		}
		public List<List<Object>> getValues() {
			return values;
		}
		public void setValues(final List<List<Object>> values) {
			this.values = values;
		}
	}

	public static class Data {
		private List<ChartDataSeries> series;

        public String toPrettyString(final String indent) {
	        final var sb = new StringBuilder();
	        final var ni = indent + "  ";
            sb.append("{\n");
            sb.append(ni).append("series: ");
            if (series == null) {
                sb.append("null\n");
            }
			else if (series.isEmpty()) {
                sb.append("[]\n");
            }
			else {
                sb.append("[\n");
                for (var s : series) {
                    sb.append(s.toPrettyString(ni + "  ")).append("\n");
                }
                sb.append(ni).append("]\n");
            }
            sb.append(indent).append("}");
            return sb.toString();
        }

		public List<ChartDataSeries> getSeries() {
			return series;
		}
		public void setSeries(final List<ChartDataSeries> series) {
			this.series = series;
		}
	}

	public String getChartId() {
		return chartId;
	}

	public void setChartId(final String chartId) {
		this.chartId = chartId;
	}

	public ChartType getChartType() {
		return chartType;
	}

	public void setChartType(final ChartType chartType) {
		this.chartType = chartType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getxLabel() {
		return xLabel;
	}

	public void setxLabel(final String xLabel) {
		this.xLabel = xLabel;
	}

	public String getyLabel() {
		return yLabel;
	}

	public void setyLabel(final String yLabel) {
		this.yLabel = yLabel;
	}

	public ChartConfig getChartConfig() {
		return chartConfig;
	}

	public void setChartConfig(final ChartConfig chartConfig) {
		this.chartConfig = chartConfig;
	}

	public RequestParams getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(final RequestParams requestParams) {
		this.requestParams = requestParams;
	}

	public Data getData() {
		return data;
	}

	public void setData(final Data data) {
		this.data = data;
	}
}
