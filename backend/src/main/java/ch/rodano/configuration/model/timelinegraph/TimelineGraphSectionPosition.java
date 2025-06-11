package ch.rodano.configuration.model.timelinegraph;

import java.util.Collection;
import java.util.Collections;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

public class TimelineGraphSectionPosition implements Node {
	private static final long serialVersionUID = 3773184542129742539L;

	private Integer start;
	private Integer stop;

	public final Integer getStart() {
		return start;
	}

	public final void setStart(final Integer start) {
		this.start = start;
	}

	public final Integer getStop() {
		return stop;
	}

	public final void setStop(final Integer stop) {
		this.stop = stop;
	}

	@Override
	public Entity getEntity() {
		return Entity.TIMELINE_GRAPH_SECTION_POSITION;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
