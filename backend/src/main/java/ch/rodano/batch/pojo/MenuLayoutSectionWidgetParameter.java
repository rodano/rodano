package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuLayoutSectionWidgetParameter {

	private String type;
	private String title;
	private Integer width;
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
			", workflow='" + workflow + '\'' +
			", summary='" + summary + '\'' +
			", chart='" + chart + '\'' +
			", scopeModelId='" + scopeModelId + '\'' +
			", category='" + category + '\'' +
			'}';
	}
}
