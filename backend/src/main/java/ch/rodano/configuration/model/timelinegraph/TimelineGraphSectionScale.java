package ch.rodano.configuration.model.timelinegraph;

import java.util.Collection;
import java.util.Collections;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

public class TimelineGraphSectionScale implements Node {
	private static final long serialVersionUID = -6392180609573822912L;

	private Integer min;
	private Integer max;
	private int decimal;
	private double markInterval;
	private double labelInterval;
	private TimelineGraphSectionAxisPosition position;

	public final Integer getMin() {
		return min;
	}

	public final void setMin(final Integer min) {
		this.min = min;
	}

	public final Integer getMax() {
		return max;
	}

	public final void setMax(final Integer max) {
		this.max = max;
	}

	public final int getDecimal() {
		return decimal;
	}

	public final void setDecimal(final int decimal) {
		this.decimal = decimal;
	}

	public final double getMarkInterval() {
		return markInterval;
	}

	public final void setMarkInterval(final double markInterval) {
		this.markInterval = markInterval;
	}

	public final double getLabelInterval() {
		return labelInterval;
	}

	public final void setLabelInterval(final double labelInterval) {
		this.labelInterval = labelInterval;
	}

	public final TimelineGraphSectionAxisPosition getPosition() {
		return position;
	}

	public final void setPosition(final TimelineGraphSectionAxisPosition position) {
		this.position = position;
	}

	@Override
	public Entity getEntity() {
		return Entity.TIMELINE_GRAPH_SECTION_SCALE;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
