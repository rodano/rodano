package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class TimelineGraphSectionReferenceDTO {

	@NotNull
	private UUID graphReferenceId;

	private String color;
	private boolean dashed;

	private UUID referenceSectionId;

	@NotNull
	private SortedMap<String, String> label;
	private SortedMap<String, String> tooltip;

	@Schema(description = "Reference Entries")
	@NotNull
	private List<TimelineGraphSectionReferenceEntryDTO> entries;

	public UUID getGraphReferenceId() {
		return graphReferenceId;
	}

	public void setGraphReferenceId(final UUID graphReferenceId) {
		this.graphReferenceId = graphReferenceId;
	}

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public boolean isDashed() {
		return dashed;
	}

	public void setDashed(final boolean dashed) {
		this.dashed = dashed;
	}

	public UUID getReferenceSectionId() {
		return referenceSectionId;
	}

	public void setReferenceSectionId(final UUID referenceSectionId) {
		this.referenceSectionId = referenceSectionId;
	}

	public SortedMap<String, String> getLabel() {
		return label;
	}

	public void setLabel(final SortedMap<String, String> label) {
		this.label = label;
	}

	public SortedMap<String, String> getTooltip() {
		return tooltip;
	}

	public void setTooltip(final SortedMap<String, String> tooltip) {
		this.tooltip = tooltip;
	}

	public List<TimelineGraphSectionReferenceEntryDTO> getEntries() {
		return entries;
	}

	public void setEntries(final List<TimelineGraphSectionReferenceEntryDTO> entries) {
		this.entries = entries;
	}
}
