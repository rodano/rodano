package ch.rodano.configuration.model.timelinegraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

@JsonInclude(Include.NON_NULL)
public class TimelineGraphSectionReference implements Node {
	private static final long serialVersionUID = 6494810301408433597L;

	private Map<String, String> label;
	private Map<String, String> tooltip;
	private String color;
	private boolean dashed;
	private String referenceSectionId;
	private List<TimelineGraphSectionReferenceEntry> entries;

	public TimelineGraphSectionReference() {
		label = new HashMap<>();
		tooltip = new HashMap<>();
	}

	public Map<String, String> getLabel() {
		return label;
	}

	public void setLabel(final Map<String, String> label) {
		this.label = label;
	}

	public final Map<String, String> getTooltip() {
		return tooltip;
	}

	public final void setTooltip(final Map<String, String> tooltip) {
		this.tooltip = tooltip;
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
	public final Entity getEntity() {
		return Entity.TIMELINE_GRAPH_SECTION_REFERENCE;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
