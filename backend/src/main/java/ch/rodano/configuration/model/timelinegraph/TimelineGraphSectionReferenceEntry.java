package ch.rodano.configuration.model.timelinegraph;

import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

@JsonInclude(Include.NON_NULL)
public class TimelineGraphSectionReferenceEntry implements Node {
	private static final long serialVersionUID = 6494810301408433597L;

	private String timepoint;
	private Float value;
	private String label;

	public String getTimepoint() {
		return timepoint;
	}

	public void setTimepoint(final String timepoint) {
		this.timepoint = timepoint;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(final Float value) {
		this.value = value;
	}

	public final String getLabel() {
		return label;
	}

	public final void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public final Entity getEntity() {
		return Entity.TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
