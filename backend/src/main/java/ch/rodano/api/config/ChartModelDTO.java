package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.chart.ChartType;

public class ChartModelDTO {
	@NotNull
	private UUID chartId;
	@NotNull
	private String id;
	@NotNull
	private ChartType type;

	@NotNull
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	@NotNull
	private SortedMap<String, String> title;

	private SortedMap<String, String> legendX;
	private SortedMap<String, String> legendY;

	private UUID workflowId;
	private UUID scopeModelId;
	private UUID leafScopeModelId;
	private UUID datasetModelId;
	private UUID fieldModelId;

	@NotNull
	private List<String> colors;

	private boolean withStatistics;
	private boolean overrideUserRights;
	private boolean displayExpected;

	@NotNull
	private List<ChartRangeDTO> ranges;

	@NotNull
	private List<ChartStateFilterDTO> stateFilters;

	public ChartModelDTO() {
	}

	public ChartModelDTO(final Chart chart, final String[] languages) {
		this.chartId = chart.getChartId();
		this.id = chart.getId();
		this.type = chart.getType();
		this.title = chart.getTitle() != null ? new TreeMap<>(chart.getTitle()) : new TreeMap<>();
		this.legendX = chart.getLegendX() != null ? new TreeMap<>(chart.getLegendX()) : new TreeMap<>();
		this.legendY = chart.getLegendY() != null ? new TreeMap<>(chart.getLegendY()) : new TreeMap<>();
		this.colors = chart.getColors();
		this.withStatistics = chart.isWithStatistics();
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public void setTitle(final SortedMap<String, String> title) {
		this.title = title;
	}

	public void setLegendX(final SortedMap<String, String> legendX) {
		this.legendX = legendX;
	}

	public void setLegendY(final SortedMap<String, String> legendY) {
		this.legendY = legendY;
	}

	public UUID getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final UUID workflowId) {
		this.workflowId = workflowId;
	}

	public UUID getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final UUID scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public UUID getLeafScopeModelId() {
		return leafScopeModelId;
	}

	public void setLeafScopeModelId(final UUID leafScopeModelId) {
		this.leafScopeModelId = leafScopeModelId;
	}

	public UUID getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final UUID datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public UUID getFieldModelId() {
		return fieldModelId;
	}

	public void setFieldModelId(final UUID fieldModelId) {
		this.fieldModelId = fieldModelId;
	}

	public void setWithStatistics(final boolean withStatistics) {
		this.withStatistics = withStatistics;
	}

	public boolean isOverrideUserRights() {
		return overrideUserRights;
	}

	public void setOverrideUserRights(final boolean overrideUserRights) {
		this.overrideUserRights = overrideUserRights;
	}

	public boolean isDisplayExpected() {
		return displayExpected;
	}

	public void setDisplayExpected(final boolean displayExpected) {
		this.displayExpected = displayExpected;
	}

	public List<ChartRangeDTO> getRanges() {
		return ranges;
	}

	public void setRanges(final List<ChartRangeDTO> ranges) {
		this.ranges = ranges;
	}

	public List<ChartStateFilterDTO> getStateFilters() {
		return stateFilters;
	}

	public void setStateFilters(final List<ChartStateFilterDTO> stateFilters) {
		this.stateFilters = stateFilters;
	}

	public UUID getChartId() {
		return chartId;
	}

	public void setChartId(final UUID chartId) {
		this.chartId = chartId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public ChartType getType() {
		return type;
	}

	public void setType(final ChartType type) {
		this.type = type;
	}

	public SortedMap<String, String> getTitle() {
		return title;
	}

	public SortedMap<String, String> getLegendX() {
		return legendX;
	}

	public SortedMap<String, String> getLegendY() {
		return legendY;
	}

	public List<String> getColors() {
		return colors;
	}

	public void setColors(final List<String> colors) {
		this.colors = colors;
	}

	public boolean isWithStatistics() {
		return withStatistics;
	}
}
