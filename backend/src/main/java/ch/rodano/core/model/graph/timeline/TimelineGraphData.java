package ch.rodano.core.model.graph.timeline;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.configuration.model.timelinegraph.TimelineGraph;

@Schema(description = "Timeline graph data")
public class TimelineGraphData {
	@Serial
	private static final long serialVersionUID = -601065732031149176L;

	@NotBlank
	private String id;

	private int width;
	private int height;
	private int legendWidth;
	private int scrollerHeight;
	@NotNull
	private boolean showScroller;

	private SortedMap<String, String> footNote;

	@NotNull
	private List<TimelineGraphDataPeriod> periods = new ArrayList<>();
	@NotNull
	private List<TimelineGraphDataSection> sections = new ArrayList<>();

	public TimelineGraphData(final TimelineGraph graph) {
		id = graph.getId();
		width = graph.getWidth();
		height = graph.getHeight();
		legendWidth = graph.getLegendWidth();
		scrollerHeight = graph.getScrollerHeight();
		showScroller = graph.isShowScroller();
		footNote = graph.getFootNote();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public int getLegendWidth() {
		return legendWidth;
	}

	public void setLegendWidth(final int legendWidth) {
		this.legendWidth = legendWidth;
	}

	public int getScrollerHeight() {
		return scrollerHeight;
	}

	public void setScrollerHeight(final int scrollerHeight) {
		this.scrollerHeight = scrollerHeight;
	}

	public boolean isShowScroller() {
		return showScroller;
	}

	public void setShowScroller(final boolean showScroller) {
		this.showScroller = showScroller;
	}

	public SortedMap<String, String> getFootNote() {
		return footNote;
	}

	public void setFootNote(final SortedMap<String, String> footNote) {
		this.footNote = footNote;
	}

	public final List<TimelineGraphDataPeriod> getPeriods() {
		return periods;
	}

	public final void setPeriods(final List<TimelineGraphDataPeriod> periods) {
		this.periods = periods;
	}

	public final List<TimelineGraphDataSection> getSections() {
		return sections;
	}

	public final void setSections(final List<TimelineGraphDataSection> sections) {
		this.sections = sections;
	}
}
