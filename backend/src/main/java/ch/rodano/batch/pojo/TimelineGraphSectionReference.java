package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineGraphSectionReference {

	private Map<String, String> label;
	private Map<String, String> tooltip;

	private String color;
	private Boolean dashed;
	private String referenceSectionId;

	private List<TimelineGraphSectionReferenceEntry> entries;

	public Map<String, String> getLabel() {
		return label;
	}

	public void setLabel(final Map<String, String> label) {
		this.label = label;
	}

	public Map<String, String> getTooltip() {
		return tooltip;
	}

	public void setTooltip(final Map<String, String> tooltip) {
		this.tooltip = tooltip;
	}

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public Boolean getDashed() {
		return dashed;
	}

	public void setDashed(final Boolean dashed) {
		this.dashed = dashed;
	}

	public String getReferenceSectionId() {
		return referenceSectionId;
	}

	public void setReferenceSectionId(final String referenceSectionId) {
		this.referenceSectionId = referenceSectionId;
	}

	public List<TimelineGraphSectionReferenceEntry> getEntries() {
		return entries;
	}

	public void setEntries(final List<TimelineGraphSectionReferenceEntry> entries) {
		this.entries = entries;
	}

	@Override
	public String toString() {
		return "TimelineGraphSectionReference{" +
			"label=" + label +
			", tooltip=" + tooltip +
			", color='" + color + '\'' +
			", dashed=" + dashed +
			", referenceSectionId='" + referenceSectionId + '\'' +
			", entries=" + entries +
			'}';
	}
}
