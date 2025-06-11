package ch.rodano.configuration.model.timelinegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

@JsonInclude(Include.NON_NULL)
public class TimelineGraphSection implements Node {
	private static final long serialVersionUID = 6494810301408433597L;

	private String id;

	//data
	private boolean useScopePaths;
	private Set<String> eventModelIds;
	private boolean hideExpectedEvent;
	private boolean hideDoneEvent;
	private String datasetModelId;
	private String dateFieldModelId;
	private String endDateFieldModelId;
	private String valueFieldModelId;
	private String labelFieldModelId;
	private List<String> metaFieldModelIds;

	//representation
	private TimelineGraphSectionType type;
	private Map<String, String> label;
	private Map<String, String> tooltip;
	private String unit;
	private String color;
	private String strokeColor;
	private float opacity;
	private boolean dashed;
	private TimelineGraphSectionMark mark;
	private TimelineGraphSectionScale scale;
	private TimelineGraphSectionPosition position;
	private boolean hiddenLegend;
	private boolean hidden;

	private List<TimelineGraphSectionReference> references;

	public TimelineGraphSection() {
		useScopePaths = false;
		eventModelIds = new TreeSet<>();
		metaFieldModelIds = new ArrayList<>();
		label = new HashMap<>();
		tooltip = new HashMap<>();
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final boolean isUseScopePaths() {
		return useScopePaths;
	}

	public final void setUseScopePaths(final boolean useScopePathDates) {
		useScopePaths = useScopePathDates;
	}

	public Set<String> getEventModelIds() {
		return eventModelIds;
	}

	public void setEventModelIds(final Set<String> eventIds) {
		this.eventModelIds = eventIds;
	}

	public final boolean isHideExpectedEvent() {
		return hideExpectedEvent;
	}

	public final void setHideExpectedEvent(final boolean hideExpectedEvent) {
		this.hideExpectedEvent = hideExpectedEvent;
	}

	public final boolean isHideDoneEvent() {
		return hideDoneEvent;
	}

	public final void setHideDoneEvent(final boolean hideDoneEvent) {
		this.hideDoneEvent = hideDoneEvent;
	}

	public final String getDatasetModelId() {
		return datasetModelId;
	}

	public final void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public final String getDateFieldModelId() {
		return dateFieldModelId;
	}

	public final void setDateFieldModelId(final String dateFieldModelId) {
		this.dateFieldModelId = dateFieldModelId;
	}

	public final String getEndDateFieldModelId() {
		return endDateFieldModelId;
	}

	public final void setEndDateFieldModelId(final String endDateFieldModelId) {
		this.endDateFieldModelId = endDateFieldModelId;
	}

	public final String getValueFieldModelId() {
		return valueFieldModelId;
	}

	public final void setValueFieldModelId(final String valueFieldModelId) {
		this.valueFieldModelId = valueFieldModelId;
	}

	public final String getLabelFieldModelId() {
		return labelFieldModelId;
	}

	public final void setLabelFieldModelId(final String labelFieldModelId) {
		this.labelFieldModelId = labelFieldModelId;
	}

	public List<String> getMetaFieldModelIds() {
		return metaFieldModelIds;
	}

	public void setMetaFieldModelIds(final List<String> metaFieldModelIds) {
		this.metaFieldModelIds = metaFieldModelIds;
	}

	public final TimelineGraphSectionType getType() {
		return type;
	}

	public final void setType(final TimelineGraphSectionType type) {
		this.type = type;
	}

	public final Map<String, String> getLabel() {
		return label;
	}

	public final void setLabel(final Map<String, String> label) {
		this.label = label;
	}

	public final Map<String, String> getTooltip() {
		return tooltip;
	}

	public final void setTooltip(final Map<String, String> tooltip) {
		this.tooltip = tooltip;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

	public final String getColor() {
		return color;
	}

	public final void setColor(final String color) {
		this.color = color;
	}

	public final String getStrokeColor() {
		return strokeColor;
	}

	public final void setStrokeColor(final String strokeColor) {
		this.strokeColor = strokeColor;
	}

	public final float getOpacity() {
		return opacity;
	}

	public final void setOpacity(final float opacity) {
		this.opacity = opacity;
	}

	public final boolean isDashed() {
		return dashed;
	}

	public final void setDashed(final boolean dashed) {
		this.dashed = dashed;
	}

	public final TimelineGraphSectionMark getMark() {
		return mark;
	}

	public final void setMark(final TimelineGraphSectionMark mark) {
		this.mark = mark;
	}

	public final TimelineGraphSectionScale getScale() {
		return scale;
	}

	public final void setScale(final TimelineGraphSectionScale scale) {
		this.scale = scale;
	}

	public final TimelineGraphSectionPosition getPosition() {
		return position;
	}

	public final void setPosition(final TimelineGraphSectionPosition position) {
		this.position = position;
	}

	public final boolean isHidden() {
		return hidden;
	}

	public final void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	public final boolean isHiddenLegend() {
		return hiddenLegend;
	}

	public final void setHiddenLegend(final boolean hiddenLegend) {
		this.hiddenLegend = hiddenLegend;
	}

	public List<TimelineGraphSectionReference> getReferences() {
		return references;
	}

	public void setReferences(final List<TimelineGraphSectionReference> references) {
		this.references = references;
	}

	@Override
	public final Entity getEntity() {
		return Entity.TIMELINE_GRAPH_SECTION;
	}


	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
