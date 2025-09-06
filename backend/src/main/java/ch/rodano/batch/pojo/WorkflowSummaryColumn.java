package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowSummaryColumn {

	private Map<String, String> label;
	private Map<String, String> description;

	private Boolean total;
	private Boolean percent;
	private String nonNullColor;
	private String nonNullBackgroundColor;

	private List<String> stateIds;

	public Map<String, String> getLabel() {
		return label;
	}

	public void setLabel(final Map<String, String> label) {
		this.label = label;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public Boolean getTotal() {
		return total;
	}

	public void setTotal(final Boolean total) {
		this.total = total;
	}

	public Boolean getPercent() {
		return percent;
	}

	public void setPercent(final Boolean percent) {
		this.percent = percent;
	}

	public String getNonNullColor() {
		return nonNullColor;
	}

	public void setNonNullColor(final String nonNullColor) {
		this.nonNullColor = nonNullColor;
	}

	public String getNonNullBackgroundColor() {
		return nonNullBackgroundColor;
	}

	public void setNonNullBackgroundColor(final String nonNullBackgroundColor) {
		this.nonNullBackgroundColor = nonNullBackgroundColor;
	}

	public List<String> getStateIds() {
		return stateIds;
	}

	public void setStateIds(final List<String> stateIds) {
		this.stateIds = stateIds;
	}

	@Override
	public String toString() {
		return "WorkflowSummaryColumn{" +
			"label=" + label +
			", description=" + description +
			", total=" + total +
			", percent=" + percent +
			", nonNullColor='" + nonNullColor + '\'' +
			", nonNullBackgroundColor='" + nonNullBackgroundColor + '\'' +
			", stateIds=" + stateIds +
			'}';
	}
}
