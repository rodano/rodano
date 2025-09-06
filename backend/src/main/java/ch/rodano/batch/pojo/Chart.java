package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Chart {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private Map<String, String> title;
	private Map<String, String> legendX;
	private Map<String, String> legendY;

	private String type;
	private Boolean overrideUserRights;
	private Boolean withStatistics;
	private Boolean displayExpected;

	private String scopeModelId;
	private String leafScopeModelId;
	private String datasetModelId;
	private String fieldModelId;
	private String workflowId;

	private List<String> colors;
	private List<ChartRange> ranges;
	private List<String> includedStateIds;
	private List<String> excludedStateIds;
	private List<String> enrollmentStateIds;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public Map<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final Map<String, String> longname) {
		this.longname = longname;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public Map<String, String> getTitle() {
		return title;
	}

	public void setTitle(final Map<String, String> title) {
		this.title = title;
	}

	public Map<String, String> getLegendX() {
		return legendX;
	}

	public void setLegendX(final Map<String, String> legendX) {
		this.legendX = legendX;
	}

	public Map<String, String> getLegendY() {
		return legendY;
	}

	public void setLegendY(final Map<String, String> legendY) {
		this.legendY = legendY;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Boolean getOverrideUserRights() {
		return overrideUserRights;
	}

	public void setOverrideUserRights(final Boolean overrideUserRights) {
		this.overrideUserRights = overrideUserRights;
	}

	public Boolean getWithStatistics() {
		return withStatistics;
	}

	public void setWithStatistics(final Boolean withStatistics) {
		this.withStatistics = withStatistics;
	}

	public Boolean getDisplayExpected() {
		return displayExpected;
	}

	public void setDisplayExpected(final Boolean displayExpected) {
		this.displayExpected = displayExpected;
	}

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

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public List<String> getColors() {
		return colors;
	}

	public void setColors(final List<String> colors) {
		this.colors = colors;
	}

	public List<ChartRange> getRanges() {
		return ranges;
	}

	public void setRanges(final List<ChartRange> ranges) {
		this.ranges = ranges;
	}

	public List<String> getIncludedStateIds() {
		return includedStateIds;
	}

	public void setIncludedStateIds(final List<String> includedStateIds) {
		this.includedStateIds = includedStateIds;
	}

	public List<String> getExcludedStateIds() {
		return excludedStateIds;
	}

	public void setExcludedStateIds(final List<String> excludedStateIds) {
		this.excludedStateIds = excludedStateIds;
	}

	public List<String> getEnrollmentStateIds() {
		return enrollmentStateIds;
	}

	public void setEnrollmentStateIds(final List<String> enrollmentStateIds) {
		this.enrollmentStateIds = enrollmentStateIds;
	}

	@Override
	public String toString() {
		return "Chart{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", title=" + title +
			", legendX=" + legendX +
			", legendY=" + legendY +
			", type='" + type + '\'' +
			", overrideUserRights=" + overrideUserRights +
			", withStatistics=" + withStatistics +
			", displayExpected=" + displayExpected +
			", scopeModelId='" + scopeModelId + '\'' +
			", leafScopeModelId='" + leafScopeModelId + '\'' +
			", datasetModelId='" + datasetModelId + '\'' +
			", fieldModelId='" + fieldModelId + '\'' +
			", workflowId='" + workflowId + '\'' +
			", colors=" + colors +
			", ranges=" + ranges +
			", includedStateIds=" + includedStateIds +
			", excludedStateIds=" + excludedStateIds +
			", enrollmentStateIds=" + enrollmentStateIds +
			'}';
	}
}
