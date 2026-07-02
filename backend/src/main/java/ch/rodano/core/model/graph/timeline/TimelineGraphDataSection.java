package ch.rodano.core.model.graph.timeline;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.timelinegraph.TimelineGraphSection;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionMark;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionPosition;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionReference;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionScale;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionType;

public class TimelineGraphDataSection {
	@Serial
	private static final long serialVersionUID = 8318434632793519458L;

	private String id;

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

	@NotNull
	private List<TimelineGraphDataValue> values = new ArrayList<>();
	@NotNull
	private List<TimelineGraphSectionReference> references = new ArrayList<>();

	public TimelineGraphDataSection(final TimelineGraphSection section) {
		id = section.getId();

		type = section.getType();
		hidden = section.isHidden();
		hiddenLegend = section.isHiddenLegend();

		label = section.getLabel();
		tooltip = section.getTooltip();

		color = section.getColor();
		strokeColor = section.getStrokeColor();
		opacity = section.getOpacity();
		dashed = section.isDashed();

		mark = section.getMark();
		scale = section.getScale();
		position = section.getPosition();

		references = section.getReferences();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public TimelineGraphSectionType getType() {
		return type;
	}

	public void setType(final TimelineGraphSectionType type) {
		this.type = type;
	}

	public Map<String, String> getLabel() {
		return label;
	}

	public void setLabel(final Map<String, String> label) {
		this.label = label;
	}

	public Map<String, String> getTooltip() {
		return tooltip;
	}

	public void setTooltip(final Map<String, String> tooltip) {
		this.tooltip = tooltip;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public String getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(final String strokeColor) {
		this.strokeColor = strokeColor;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(final float opacity) {
		this.opacity = opacity;
	}

	public boolean isDashed() {
		return dashed;
	}

	public void setDashed(final boolean dashed) {
		this.dashed = dashed;
	}

	public TimelineGraphSectionMark getMark() {
		return mark;
	}

	public void setMark(final TimelineGraphSectionMark mark) {
		this.mark = mark;
	}

	public TimelineGraphSectionScale getScale() {
		return scale;
	}

	public void setScale(final TimelineGraphSectionScale scale) {
		this.scale = scale;
	}

	public TimelineGraphSectionPosition getPosition() {
		return position;
	}

	public void setPosition(final TimelineGraphSectionPosition position) {
		this.position = position;
	}

	public boolean isHiddenLegend() {
		return hiddenLegend;
	}

	public void setHiddenLegend(final boolean hiddenLegend) {
		this.hiddenLegend = hiddenLegend;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	public final List<TimelineGraphDataValue> getValues() {
		return values;
	}

	public final void setValues(final List<TimelineGraphDataValue> values) {
		this.values = values;
	}

	public List<TimelineGraphSectionReference> getReferences() {
		return references;
	}
}
