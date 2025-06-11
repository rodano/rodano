package ch.rodano.api.timeline;

import java.util.List;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.graph.timeline.TimelineGraphData;
import ch.rodano.core.model.graph.timeline.TimelineGraphDataPeriod;
import ch.rodano.core.model.graph.timeline.TimelineGraphDataSection;

@Schema(description = "Timeline graph data")
public class TimelineGraphDataDTO {
	@NotBlank
	private final String id;

	@NotNull
	private final SortedMap<String, String> shortname;
	@NotNull
	private final SortedMap<String, String> longname;
	private final SortedMap<String, String> description;
	@NotBlank
	private final String scopeModelId;

	private final String studyStartEventModelId;
	private final String studyStopEventModelId;
	private final boolean studyPeriodIsDefault;

	private final int width;
	private final int height;
	private final int legendWidth;
	private final int scrollerHeight;
	@NotNull
	private final boolean showScroller;

	private final SortedMap<String, String> footNote;

	@NotNull
	private final List<TimelineGraphDataSection> sections;
	@NotNull
	private final List<TimelineGraphDataPeriod> periods;

	public TimelineGraphDataDTO(final TimelineGraphData timelineGraphData) {
		this.id = timelineGraphData.getId();
		this.shortname = timelineGraphData.getShortname();
		this.longname = timelineGraphData.getLongname();
		this.description = timelineGraphData.getDescription();
		this.scopeModelId = timelineGraphData.getScopeModelId();
		this.studyStartEventModelId = timelineGraphData.getStudyStartEventModelId();
		this.studyStopEventModelId = timelineGraphData.getStudyStopEventModelId();
		this.studyPeriodIsDefault = timelineGraphData.isStudyPeriodIsDefault();
		this.width = timelineGraphData.getWidth();
		this.height = timelineGraphData.getHeight();
		this.legendWidth = timelineGraphData.getLegendWidth();
		this.scrollerHeight = timelineGraphData.getScrollerHeight();
		this.showScroller = timelineGraphData.isShowScroller();
		this.footNote = timelineGraphData.getFootNote();
		this.sections = timelineGraphData.getDataSections();
		this.periods = timelineGraphData.getPeriods();
	}

	public String getId() {
		return id;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public String getScopeModelId() {
		return scopeModelId;
	}

	public String getStudyStartEventModelId() {
		return studyStartEventModelId;
	}

	public String getStudyStopEventModelId() {
		return studyStopEventModelId;
	}

	public boolean isStudyPeriodIsDefault() {
		return studyPeriodIsDefault;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getLegendWidth() {
		return legendWidth;
	}

	public int getScrollerHeight() {
		return scrollerHeight;
	}

	public boolean isShowScroller() {
		return showScroller;
	}

	public SortedMap<String, String> getFootNote() {
		return footNote;
	}

	public List<TimelineGraphDataSection> getSections() {
		return sections;
	}

	public List<TimelineGraphDataPeriod> getPeriods() {
		return periods;
	}
}
