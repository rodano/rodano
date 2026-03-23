package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuLayoutSectionWidgetParameter {

	private String type;
	private String title;
	private Integer width;

	private Boolean displayAddResponse;
	private Boolean removeProfileSelector;
	private String overdueType;
	private String specificColumnName;

	private String workflow;
	private String summary;
	private String chart;
	private String scopeModelId;
	private String category;

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(final Integer width) {
		this.width = width;
	}

	public Boolean getDisplayAddResponse() {
		return displayAddResponse;
	}

	public void setDisplayAddResponse(final Boolean displayAddResponse) {
		this.displayAddResponse = displayAddResponse;
	}

	public Boolean getRemoveProfileSelector() {
		return removeProfileSelector;
	}

	public void setRemoveProfileSelector(final Boolean removeProfileSelector) {
		this.removeProfileSelector = removeProfileSelector;
	}

	public String getOverdueType() {
		return overdueType;
	}

	public void setOverdueType(final String overdueType) {
		this.overdueType = overdueType;
	}

	public String getSpecificColumnName() {
		return specificColumnName;
	}

	public void setSpecificColumnName(final String specificColumnName) {
		this.specificColumnName = specificColumnName;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(final String workflow) {
		this.workflow = workflow;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(final String summary) {
		this.summary = summary;
	}

	public String getChart() {
		return chart;
	}

	public void setChart(final String chart) {
		this.chart = chart;
	}

	public String getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final String scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "MenuLayoutSectionWidgetParameter{" +
			"type='" + type + '\'' +
			", title='" + title + '\'' +
			", width=" + width +
			", displayAddResponse=" + displayAddResponse +
			", removeProfileSelector=" + removeProfileSelector +
			", overdueType='" + overdueType + '\'' +
			", specificColumnName='" + specificColumnName + '\'' +
			", workflow='" + workflow + '\'' +
			", summary='" + summary + '\'' +
			", chart='" + chart + '\'' +
			", scopeModelId='" + scopeModelId + '\'' +
			", category='" + category + '\'' +
			'}';
	}
}
