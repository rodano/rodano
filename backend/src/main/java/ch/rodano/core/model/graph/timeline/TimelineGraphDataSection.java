package ch.rodano.core.model.graph.timeline;

import java.util.ArrayList;
import java.util.List;

import ch.rodano.configuration.model.timelinegraph.TimelineGraphSection;

public class TimelineGraphDataSection extends TimelineGraphSection {
	private static final long serialVersionUID = 8318434632793519458L;

	private List<TimelineGraphDataValue> values = new ArrayList<>();

	public TimelineGraphDataSection(final TimelineGraphSection section) {
		setId(section.getId());

		setType(section.getType());
		setHidden(section.isHidden());
		setHiddenLegend(section.isHiddenLegend());

		setLabel(section.getLabel());
		setTooltip(section.getTooltip());

		setColor(section.getColor());
		setStrokeColor(section.getStrokeColor());
		setOpacity(section.getOpacity());
		setDashed(section.isDashed());

		setMark(section.getMark());
		setScale(section.getScale());
		setPosition(section.getPosition());

		setDateFieldModelId(section.getDateFieldModelId());
		setEndDateFieldModelId(section.getEndDateFieldModelId());
		setValueFieldModelId(section.getValueFieldModelId());
		setLabelFieldModelId(section.getLabelFieldModelId());
		setMetaFieldModelIds(section.getMetaFieldModelIds());

		setReferences(section.getReferences());
	}

	public final List<TimelineGraphDataValue> getValues() {
		return values;
	}

	public final void setValues(final List<TimelineGraphDataValue> values) {
		this.values = values;
	}
}
