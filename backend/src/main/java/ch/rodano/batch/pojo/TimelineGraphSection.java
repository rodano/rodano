package ch.rodano.batch.pojo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.rodano.core.model.jooq.enums.TimelineGraphSectionMark;
import ch.rodano.core.model.jooq.enums.TimelineGraphSectionType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineGraphSection {

	private String id;
	private Map<String, String> label;
	private Map<String, String> tooltip;

	private TimelineGraphSectionType type;
	private String color;
	private String strokeColor;
	private BigDecimal opacity;
	private TimelineGraphSectionMark mark;
	private Boolean useScopePaths;
	private Boolean hideExpectedEvent;
	private Boolean hideDoneEvent;
	private Boolean dashed;
	private Boolean hiddenLegend;
	private Boolean hidden;

	private String datasetModelId;
	private String dateFieldModelId;
	private String endDateFieldModelId;
	private String labelFieldModelId;
	private String valueFieldModelId;

	private List<String> eventModelIds;
	private List<String> metaFieldModelIds;

	private List<TimelineGraphSectionReference> references;
	private TimelineGraphSectionPosition position;
	private TimelineGraphSectionScale scale;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
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

	public TimelineGraphSectionType getType() {
		return type;
	}

	public void setType(final TimelineGraphSectionType type) {
		this.type = type;
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

	public BigDecimal getOpacity() {
		return opacity;
	}

	public void setOpacity(final BigDecimal opacity) {
		this.opacity = opacity;
	}

	public TimelineGraphSectionMark getMark() {
		return mark;
	}

	public void setMark(final TimelineGraphSectionMark mark) {
		this.mark = mark;
	}

	public Boolean getUseScopePaths() {
		return useScopePaths;
	}

	public void setUseScopePaths(final Boolean useScopePaths) {
		this.useScopePaths = useScopePaths;
	}

	public Boolean getHideExpectedEvent() {
		return hideExpectedEvent;
	}

	public void setHideExpectedEvent(final Boolean hideExpectedEvent) {
		this.hideExpectedEvent = hideExpectedEvent;
	}

	public Boolean getHideDoneEvent() {
		return hideDoneEvent;
	}

	public void setHideDoneEvent(final Boolean hideDoneEvent) {
		this.hideDoneEvent = hideDoneEvent;
	}

	public Boolean getDashed() {
		return dashed;
	}

	public void setDashed(final Boolean dashed) {
		this.dashed = dashed;
	}

	public Boolean getHiddenLegend() {
		return hiddenLegend;
	}

	public void setHiddenLegend(final Boolean hiddenLegend) {
		this.hiddenLegend = hiddenLegend;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(final Boolean hidden) {
		this.hidden = hidden;
	}

	public String getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public String getDateFieldModelId() {
		return dateFieldModelId;
	}

	public void setDateFieldModelId(final String dateFieldModelId) {
		this.dateFieldModelId = dateFieldModelId;
	}

	public String getEndDateFieldModelId() {
		return endDateFieldModelId;
	}

	public void setEndDateFieldModelId(final String endDateFieldModelId) {
		this.endDateFieldModelId = endDateFieldModelId;
	}

	public String getLabelFieldModelId() {
		return labelFieldModelId;
	}

	public void setLabelFieldModelId(final String labelFieldModelId) {
		this.labelFieldModelId = labelFieldModelId;
	}

	public String getValueFieldModelId() {
		return valueFieldModelId;
	}

	public void setValueFieldModelId(final String valueFieldModelId) {
		this.valueFieldModelId = valueFieldModelId;
	}

	public List<String> getEventModelIds() {
		return eventModelIds;
	}

	public void setEventModelIds(final List<String> eventModelIds) {
		this.eventModelIds = eventModelIds;
	}

	public List<String> getMetaFieldModelIds() {
		return metaFieldModelIds;
	}

	public void setMetaFieldModelIds(final List<String> metaFieldModelIds) {
		this.metaFieldModelIds = metaFieldModelIds;
	}

	public List<TimelineGraphSectionReference> getReferences() {
		return references;
	}

	public void setReferences(final List<TimelineGraphSectionReference> references) {
		this.references = references;
	}

	public TimelineGraphSectionPosition getPosition() {
		return position;
	}

	public void setPosition(final TimelineGraphSectionPosition position) {
		this.position = position;
	}

	public TimelineGraphSectionScale getScale() {
		return scale;
	}

	public void setScale(final TimelineGraphSectionScale scale) {
		this.scale = scale;
	}

	@Override
	public String toString() {
		return "TimelineGraphSection{" +
			"id='" + id + '\'' +
			", label=" + label +
			", tooltip=" + tooltip +
			", type='" + type + '\'' +
			", color='" + color + '\'' +
			", strokeColor='" + strokeColor + '\'' +
			", opacity=" + opacity +
			", mark='" + mark + '\'' +
			", useScopePaths=" + useScopePaths +
			", hideExpectedEvent=" + hideExpectedEvent +
			", hideDoneEvent=" + hideDoneEvent +
			", dashed=" + dashed +
			", hiddenLegend=" + hiddenLegend +
			", hidden=" + hidden +
			", datasetModelId='" + datasetModelId + '\'' +
			", dateFieldModelId='" + dateFieldModelId + '\'' +
			", endDateFieldModelId='" + endDateFieldModelId + '\'' +
			", labelFieldModelId='" + labelFieldModelId + '\'' +
			", valueFieldModelId='" + valueFieldModelId + '\'' +
			", eventModelIds=" + eventModelIds +
			", metaFieldModelIds=" + metaFieldModelIds +
			", references=" + references +
			", position=" + position +
			", scale=" + scale +
			'}';
	}
}
