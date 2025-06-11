package ch.rodano.configuration.model.timelinegraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.rights.Assignable;
import ch.rodano.configuration.model.study.Study;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class TimelineGraph implements SuperDisplayable, Serializable, Assignable<TimelineGraph>, Node {
	private static final long serialVersionUID = 5861725017187478844L;

	private Study study;
	private String id;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;
	private String scopeModelId;

	private String studyStartEventModelId;
	private String studyStopEventModelId;
	private boolean studyPeriodIsDefault;

	private int width;
	private int height;
	private int legendWidth;
	private int scrollerHeight;
	private boolean showScroller;

	private SortedMap<String, String> footNote;

	private List<TimelineGraphSection> sections;

	public TimelineGraph() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		footNote = new TreeMap<>();
		sections = new ArrayList<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getStudy().getDefaultLanguage().getId());
	}

	@Override
	@JsonIgnore
	public final String getAssignableDescription() {
		return getDefaultLocalizedShortname();
	}

	@Override
	public final Entity getEntity() {
		return Entity.TIMELINE_GRAPH;
	}

	@Override
	@JsonIgnore
	public final int compareTo(final TimelineGraph config) {
		return getId().compareTo(config.getId());
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

	public final String getScopeModelId() {
		return scopeModelId;
	}

	public final void setScopeModelId(final String scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public String getStudyStartEventModelId() {
		return studyStartEventModelId;
	}

	public void setStudyStartEventModelId(final String studyStartEventId) {
		this.studyStartEventModelId = studyStartEventId;
	}

	public String getStudyStopEventModelId() {
		return studyStopEventModelId;
	}

	public void setStudyStopEventModelId(final String studyStopEventId) {
		this.studyStopEventModelId = studyStopEventId;
	}

	public boolean isStudyPeriodIsDefault() {
		return studyPeriodIsDefault;
	}

	public void setStudyPeriodIsDefault(final boolean studyPeriodIsDefault) {
		this.studyPeriodIsDefault = studyPeriodIsDefault;
	}

	public final int getWidth() {
		return width;
	}

	public final void setWidth(final int width) {
		this.width = width;
	}

	public final int getHeight() {
		return height;
	}

	public final void setHeight(final int height) {
		this.height = height;
	}

	public final boolean isShowScroller() {
		return showScroller;
	}

	public final void setShowScroller(final boolean showScroller) {
		this.showScroller = showScroller;
	}

	public final int getLegendWidth() {
		return legendWidth;
	}

	public final void setLegendWidth(final int legendWidth) {
		this.legendWidth = legendWidth;
	}

	public final int getScrollerHeight() {
		return scrollerHeight;
	}

	public final void setScrollerHeight(final int scrollerHeight) {
		this.scrollerHeight = scrollerHeight;
	}

	public SortedMap<String, String> getFootNote() {
		return footNote;
	}

	public void setFootNote(final SortedMap<String, String> footNote) {
		this.footNote = footNote;
	}

	public List<TimelineGraphSection> getSections() {
		return sections;
	}

	public void setSections(final List<TimelineGraphSection> sections) {
		this.sections = sections;
	}

	public TimelineGraphSection getSection(final String sectionId) {
		return getSections().stream()
			.filter(s -> s.getId().equals(sectionId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.TIMELINE_GRAPH_SECTION, sectionId));
	}
}
