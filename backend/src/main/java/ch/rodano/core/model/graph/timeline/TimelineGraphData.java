package ch.rodano.core.model.graph.timeline;

import java.util.ArrayList;
import java.util.List;

import ch.rodano.configuration.model.timelinegraph.TimelineGraph;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSection;

public class TimelineGraphData extends TimelineGraph {
	private static final long serialVersionUID = -601065732031149176L;

	private List<TimelineGraphDataPeriod> periods = new ArrayList<>();
	private List<TimelineGraphDataSection> sections = new ArrayList<>();

	public TimelineGraphData(final TimelineGraph graph) {
		setId(graph.getId());
		setShortname(graph.getShortname());
		setLongname(graph.getLongname());
		setDescription(graph.getDescription());

		setWidth(graph.getWidth());
		setHeight(graph.getHeight());
		setLegendWidth(graph.getLegendWidth());
		setScrollerHeight(graph.getScrollerHeight());
		setShowScroller(graph.isShowScroller());

		setFootNote(graph.getFootNote());
	}

	public final List<TimelineGraphDataPeriod> getPeriods() {
		return periods;
	}

	public final void setPeriods(final List<TimelineGraphDataPeriod> periods) {
		this.periods = periods;
	}

	@Override
	public final List<TimelineGraphSection> getSections() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setSections(final List<TimelineGraphSection> sections) {
		throw new UnsupportedOperationException();
	}

	public final List<TimelineGraphDataSection> getDataSections() {
		return sections;
	}

	public final void setDataSections(final List<TimelineGraphDataSection> sections) {
		this.sections = sections;
	}
}
