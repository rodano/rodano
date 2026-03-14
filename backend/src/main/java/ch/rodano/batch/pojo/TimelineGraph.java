package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineGraph {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private Map<String, String> footnote;

	private String scopeModelId;
	private String studyStartEventModelId;
	private String studyEndEventModelId;

	private Boolean studyPeriodIsDefault;
	private Integer width;
	private Integer height;
	private Integer legendWidth;
	private Integer scrollerHeight;
	private Boolean showScroller;

	private List<TimelineGraphSection> sections;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public Map<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final Map<String, String> longname) {
		this.longname = longname;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public Map<String, String> getFootnote() {
		return footnote;
	}

	public void setFootnote(final Map<String, String> footnote) {
		this.footnote = footnote;
	}

	public String getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final String scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public String getStudyStartEventModelId() {
		return studyStartEventModelId;
	}

	public void setStudyStartEventModelId(final String studyStartEventModelId) {
		this.studyStartEventModelId = studyStartEventModelId;
	}

	public String getStudyEndEventModelId() {
		return studyEndEventModelId;
	}

	public void setStudyEndEventModelId(final String studyEndEventModelId) {
		this.studyEndEventModelId = studyEndEventModelId;
	}

	public Boolean getStudyPeriodIsDefault() {
		return studyPeriodIsDefault;
	}

	public void setStudyPeriodIsDefault(final Boolean studyPeriodIsDefault) {
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

	public Boolean getShowScroller() {
		return showScroller;
	}

	public void setShowScroller(final Boolean showScroller) {
		this.showScroller = showScroller;
	}

	public List<TimelineGraphSection> getSections() {
		return sections;
	}

	public void setSections(final List<TimelineGraphSection> sections) {
		this.sections = sections;
	}

	@Override
	public String toString() {
		return "TimelineGraph{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", footnote=" + footnote +
			", scopeModelId='" + scopeModelId + '\'' +
			", studyStartEventModelId='" + studyStartEventModelId + '\'' +
			", studyEndEventModelId='" + studyEndEventModelId + '\'' +
			", studyPeriodIsDefault=" + studyPeriodIsDefault +
			", width=" + width +
			", height=" + height +
			", legendWidth=" + legendWidth +
			", scrollerHeight=" + scrollerHeight +
			", showScroller=" + showScroller +
			", sections=" + sections +
			'}';
	}
}
