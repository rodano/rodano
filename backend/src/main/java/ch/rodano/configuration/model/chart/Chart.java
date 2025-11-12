package ch.rodano.configuration.model.chart;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.utils.DisplayableUtils;

import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;

@JsonPropertyOrder(alphabetic = true)
public class Chart implements Node, Comparable<Chart> {
	@Serial
	private static final long serialVersionUID = 3353079861292862954L;

	private Study study;
	private UUID chartId;
	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private Map<String, String> title;
	private ChartType type;

	private Map<String, String> legendX;
	private Map<String, String> legendY;

	private List<String> colors;

	//statistic chart
	private String datasetModelId;
	private String fieldModelId;
	private List<ChartRange> ranges;
	private boolean withStatistics;

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
	}

	public UUID getChartId() {
		if(this.chartId == null && this.id != null && !this.id.isBlank() && this.study != null) {
			this.chartId = deterministic(
				this.study.getProjectId(),
				"CHART",
				this.id);
		}
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

	public final Map<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public final Map<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public final Map<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final Map<String, String> longname) {
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

	public Map<String, String> getLegendX() {
		return legendX;
	}

	public void setLegendX(final Map<String, String> legendX) {
		this.legendX = legendX;
	}

	public String getLocalizedLegendX(final String... languages) {
		return DisplayableUtils.getLocalizedMap(legendX, languages);
	}

	public Map<String, String> getLegendY() {
		return legendY;
	}

	public void setLegendY(final Map<String, String> legendY) {
		this.legendY = legendY;
	}

	public String getLocalizedLegendY(final String... languages) {
		return DisplayableUtils.getLocalizedMap(legendY, languages);
	}

	public List<String> getColors() {
		return colors;
	}

	public void setColors(final List<String> colors) {
		this.colors = colors;
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
		if(Objects.requireNonNull(entity) == Entity.CHART_RANGE) {
			return Collections.unmodifiableList(ranges);
		}
		return Collections.emptyList();
	}

	//statistics
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
		if(this.ranges != null) {
			for(ChartRange r : this.ranges) {
				r.setChart(this);
			}
		}
	}

	public boolean isWithStatistics() {
		return withStatistics;
	}

	public void setWithStatistics(final boolean withStatistics) {
		this.withStatistics = withStatistics;
	}

	@JsonIgnore
	public boolean hasRange() {
		return !ranges.isEmpty();
	}

	@JsonIgnore
	public List<String> getRangesIds() {
		return ranges.stream().map(ChartRange::getId).toList();
	}

	public ChartRange getRangeFromId(final String rangeId) {
		return ranges.stream()
			.filter(r -> r.getId().equalsIgnoreCase(rangeId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.CHART_RANGE, rangeId));
	}

	@JsonIgnore
	public List<ChartRange> getValueRanges() {
		return ranges.stream().filter(r -> StringUtils.isNotBlank(r.getValue())).toList();
	}

	@JsonIgnore
	public List<ChartRange> getNumericRanges() {
		return ranges.stream().filter(r -> Objects.nonNull(r.getMin()) && Objects.nonNull(r.getMax())).toList();
	}

	@JsonIgnore
	public Optional<ChartRange> getOtherRange() {
		return ranges.stream().filter(ChartRange::getOther).findAny();
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

	@JsonIgnore
	public DatasetModel getDatasetModel() {
		return study.getDatasetModel(datasetModelId);
	}

	@JsonIgnore
	public FieldModel getFieldModel() {
		return getDatasetModel().getFieldModel(fieldModelId);
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

	@JsonIgnore
	public UUID getDatasetModelUuid() {
		if(datasetModelId == null || datasetModelId.isBlank()) {
			return null;
		}
		return getDatasetModel().getDatasetModelId();
	}

	@JsonIgnore
	public UUID getFieldModelUuid() {
		if(fieldModelId == null || fieldModelId.isBlank()) {
			return null;
		}
		return getFieldModel().getFieldModelId();
	}

	@JsonIgnore
	public UUID getWorkflowUuid() {
		if(workflowId == null || workflowId.isBlank()) {
			return null;
		}
		return study.getWorkflow(workflowId).getWorkflowId();
	}

	@JsonIgnore
	public UUID getLeafScopeModelUuid() {
		if(leafScopeModelId == null || leafScopeModelId.isBlank()) {
			return null;
		}
		return getLeafScopeModel().getScopeModelId();
	}

	@JsonIgnore
	public UUID getScopeModelUuid() {
		if(scopeModelId == null || scopeModelId.isBlank()) {
			return null;
		}
		return getScopeModel().getScopeModelId();
	}

	@JsonIgnore
	public UUID getEnrollmentWorkflowUuid() {
		return deterministic(study.getProjectId(), "WORKFLOW", enrollmentWorkflowId);
	}

	@JsonIgnore
	public Set<UUID> getEnrollmentStateUuids() {
		if(enrollmentWorkflowId == null || enrollmentWorkflowId.isBlank()) {
			return Collections.emptySet();
		}
		return toUuidSet(study.getProjectId(), enrollmentWorkflowId, enrollmentStateIds);
	}

	@JsonIgnore
	public Set<UUID> getIncludedStateUuids() {
		if(workflowId == null || workflowId.isBlank()) {
			return Collections.emptySet();
		}
		return toUuidSet(study.getProjectId(), workflowId, includedStateIds);
	}

	@JsonIgnore
	public Set<UUID> getExcludedStateUuids() {
		if(workflowId == null || workflowId.isBlank()) {
			return Collections.emptySet();
		}
		return toUuidSet(study.getProjectId(), workflowId, excludedStateIds);
	}

	@JsonIgnore
	private static Set<UUID> toUuidSet(final UUID projectId, final String workflowCode, final Set<String> codes) {
		if(codes == null || codes.isEmpty() || projectId == null) {
			return Collections.emptySet();
		}
		return codes.stream()
			.filter(Objects::nonNull)
			.filter(s -> !s.isBlank())
			.map(s -> deterministic(projectId, "WORKFLOW_STATE", workflowCode + "|" + s))
			.collect(Collectors.toCollection(TreeSet::new));
	}
}
