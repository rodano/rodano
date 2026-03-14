package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class TimelineGraphDTO {

	@NotNull
	private UUID timelineGraphId;
	@NotBlank
	private String id;

	@NotNull
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;
	private SortedMap<String, String> footnote;

	private UUID scopeModelId;
	private UUID studyStartEventModelId;
	private UUID studyEndEventModelId;

	private boolean studyPeriodIsDefault;

	private Integer width;
	private Integer height;
	private Integer legendWidth;
	private Integer scrollerHeight;
	private boolean showScroller;

	@Schema(description = "Timeline Graph Sections")
	@NotNull
	private List<UUID> sections;

	public UUID getTimelineGraphId() {
		return timelineGraphId;
	}

	public void setTimelineGraphId(final UUID timelineGraphId) {
		this.timelineGraphId = timelineGraphId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public SortedMap<String, String> getFootnote() {
		return footnote;
	}

	public void setFootnote(final SortedMap<String, String> footnote) {
		this.footnote = footnote;
	}

	public UUID getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final UUID scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public UUID getStudyStartEventModelId() {
		return studyStartEventModelId;
	}

	public void setStudyStartEventModelId(final UUID studyStartEventModelId) {
		this.studyStartEventModelId = studyStartEventModelId;
	}

	public UUID getStudyEndEventModelId() {
		return studyEndEventModelId;
	}

	public void setStudyEndEventModelId(final UUID studyEndEventModelId) {
		this.studyEndEventModelId = studyEndEventModelId;
	}

	public boolean isStudyPeriodIsDefault() {
		return studyPeriodIsDefault;
	}

	public void setStudyPeriodIsDefault(final boolean studyPeriodIsDefault) {
		this.studyPeriodIsDefault = studyPeriodIsDefault;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(final Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(final Integer height) {
		this.height = height;
	}

	public Integer getLegendWidth() {
		return legendWidth;
	}

	public void setLegendWidth(final Integer legendWidth) {
		this.legendWidth = legendWidth;
	}

	public Integer getScrollerHeight() {
		return scrollerHeight;
	}

	public void setScrollerHeight(final Integer scrollerHeight) {
		this.scrollerHeight = scrollerHeight;
	}

	public boolean isShowScroller() {
		return showScroller;
	}

	public void setShowScroller(final boolean showScroller) {
		this.showScroller = showScroller;
	}

	public List<UUID> getSections() {
		return sections;
	}

	public void setSections(final List<UUID> sections) {
		this.sections = sections;
	}
}
