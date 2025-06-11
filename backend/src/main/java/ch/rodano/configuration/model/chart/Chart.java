package ch.rodano.configuration.model.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.request.Request;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.utils.DisplayableUtils;

@JsonPropertyOrder(alphabetic = true)
public class Chart implements Node, Comparable<Chart> {
	private static final long serialVersionUID = 3353079861292862954L;

	private Study study;
	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private Map<String, String> title;
	private ChartType type;

	private Legend legendX, legendY;

	private List<String> colors;
	private String backgroundColor;

	private Integer marginLeft;
	private Integer marginRight;
	private Integer marginTop;
	private Integer marginBottom;

	//statistic chart
	private Request request;
	private String eventModelId;
	private String datasetModelId;
	private String fieldModelId;

	private List<ChartRange> ranges;
	private Float valuesMin;
	private Float valuesMax;

	private boolean fillDataGaps;
	private boolean verticalLines;
	private boolean withStatistics;
	private boolean usePercentile;

	//workflow chart
	private String workflowId;
	private Set<String> includedStateIds;
	private Set<String> excludedStateIds;

	//all enrollment charts
	private String leafScopeModelId;
	private boolean overrideUserRights;

	//enrollment by scope
	private String scopeModelId;

	//enrollment by date
	private boolean displayExpected;
	private String enrollmentWorkflowId;
	private Set<String> enrollmentStateIds;

	public Chart() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		title = new TreeMap<>();
		colors = new ArrayList<>();
		ranges = new ArrayList<>();
	}

	@JsonBackReference
	public Study getStudy() {
		return study;
	}

	@JsonBackReference
	public void setStudy(final Study study) {
		this.study = study;
		if(request != null) {
			request.setStudy(this.study);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public final Map<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public final Map<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public final Map<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public ChartType getType() {
		return type;
	}

	public void setType(final ChartType type) {
		this.type = type;
	}

	public Map<String, String> getTitle() {
		return title;
	}

	public void setTitle(final Map<String, String> title) {
		this.title = title;
	}

	public String getLocalizedTitle(final String... languages) {
		return DisplayableUtils.getLocalizedMap(title, languages);
	}

	public Legend getLegendX() {
		return legendX;
	}

	public void setLegendX(final Legend legendX) {
		this.legendX = legendX;
	}

	public Legend getLegendY() {
		return legendY;
	}

	public void setLegendY(final Legend legendY) {
		this.legendY = legendY;
	}

	public List<String> getColors() {
		return colors;
	}

	public void setColors(final List<String> colors) {
		this.colors = colors;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Integer getMarginBottom() {
		return marginBottom;
	}

	public void setMarginBottom(final Integer marginBottom) {
		this.marginBottom = marginBottom;
	}

	public Integer getMarginLeft() {
		return marginLeft;
	}

	public void setMarginLeft(final Integer marginLeft) {
		this.marginLeft = marginLeft;
	}

	public Integer getMarginRight() {
		return marginRight;
	}

	public void setMarginRight(final Integer marginRight) {
		this.marginRight = marginRight;
	}

	public Integer getMarginTop() {
		return marginTop;
	}

	public void setMarginTop(final Integer marginTop) {
		this.marginTop = marginTop;
	}

	@Override
	public final int compareTo(final Chart chart) {
		return id.compareTo(chart.id);
	}

	@Override
	public Entity getEntity() {
		return Entity.CHART;
	}

	@Override
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case CHART_RANGE:
				return Collections.unmodifiableList(ranges);
			default:
				return Collections.emptyList();
		}
	}

	//statistics
	public Request getRequest() {
		return request;
	}

	public void setRequest(final Request request) {
		this.request = request;
		this.request.setStudy(study);
	}

	public final String getEventModelId() {
		return eventModelId;
	}

	public final void setEventModelId(final String eventId) {
		this.eventModelId = eventId;
	}

	public final String getDatasetModelId() {
		return datasetModelId;
	}

	public final void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public final String getFieldModelId() {
		return fieldModelId;
	}

	public final void setFieldModelId(final String fieldModelId) {
		this.fieldModelId = fieldModelId;
	}

	public List<ChartRange> getRanges() {
		return ranges;
	}

	public void setRanges(final List<ChartRange> ranges) {
		this.ranges = ranges;
	}

	public boolean isWithStatistics() {
		return withStatistics;
	}

	public void setWithStatistics(final boolean withStatistics) {
		this.withStatistics = withStatistics;
	}

	public boolean isUsePercentile() {
		return usePercentile;
	}

	public void setUsePercentile(final boolean statisticsUsePercentile) {
		usePercentile = statisticsUsePercentile;
	}

	public Float getValuesMin() {
		return valuesMin;
	}

	public void setValuesMin(final Float valuesMin) {
		this.valuesMin = valuesMin;
	}

	public Float getValuesMax() {
		return valuesMax;
	}

	public void setValuesMax(final Float valuesMax) {
		this.valuesMax = valuesMax;
	}

	public boolean getFillDataGaps() {
		return fillDataGaps;
	}

	public void setFillDataGaps(final boolean fillDataGaps) {
		this.fillDataGaps = fillDataGaps;
	}

	public boolean getVerticalLines() {
		return verticalLines;
	}

	public void setVerticalLines(final boolean verticalLines) {
		this.verticalLines = verticalLines;
	}

	@JsonIgnore
	public boolean hasRange() {
		return !ranges.isEmpty();
	}

	@JsonIgnore
	public List<String> getRangesIds() {
		return getRanges().stream().map(ChartRange::getId).collect(Collectors.toCollection(LinkedList::new));
	}

	public ChartRange getRangeFromId(final String rangeId) {
		return ranges.stream()
			.filter(r -> r.getId().equalsIgnoreCase(rangeId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.CHART_RANGE, rangeId));
	}

	@JsonIgnore
	public ChartRange getOtherRange() {
		return ranges.stream().filter(r -> r.getOther()).findAny().orElseThrow();
	}

	@JsonIgnore
	public List<ChartRange> getDisplayableRanges() {
		return getRanges().stream().filter(ChartRange::getShow).toList();
	}

	//workflow chart
	public final String getWorkflowId() {
		return workflowId;
	}

	public final void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public final Set<String> getIncludedStateIds() {
		return includedStateIds;
	}

	public final void setIncludedStateIds(final Set<String> includedStateIds) {
		this.includedStateIds = includedStateIds;
	}

	public final Set<String> getExcludedStateIds() {
		return excludedStateIds;
	}

	public final void setExcludedStateIds(final Set<String> excludedStateIds) {
		this.excludedStateIds = excludedStateIds;
	}

	//enrollment
	public final String getLeafScopeModelId() {
		return leafScopeModelId;
	}

	public final void setLeafScopeModelId(final String leafScopeModelId) {
		this.leafScopeModelId = leafScopeModelId;
	}

	@JsonIgnore
	public final ScopeModel getLeafScopeModel() {
		return study.getScopeModel(leafScopeModelId);
	}

	public final boolean getOverrideUserRights() {
		return overrideUserRights;
	}

	public final void setOverrideUserRights(final boolean overrideUserRights) {
		this.overrideUserRights = overrideUserRights;
	}

	//enrollment by scope
	public final String getScopeModelId() {
		return scopeModelId;
	}

	public final void setScopeModelId(final String scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	@JsonIgnore
	public final ScopeModel getScopeModel() {
		return study.getScopeModel(scopeModelId);
	}

	//enrollment by date
	public final boolean isDisplayExpected() {
		return displayExpected;
	}

	public final void setDisplayExpected(final boolean displayExpected) {
		this.displayExpected = displayExpected;
	}

	public final String getEnrollmentWorkflowId() {
		return enrollmentWorkflowId;
	}

	public final void setEnrollmentWorkflowId(final String enrollmentWorkflowId) {
		this.enrollmentWorkflowId = enrollmentWorkflowId;
	}

	public final Set<String> getEnrollmentStateIds() {
		return enrollmentStateIds;
	}

	public final void setEnrollmentStateIds(final Set<String> enrollmentStateIds) {
		this.enrollmentStateIds = enrollmentStateIds;
	}
}
