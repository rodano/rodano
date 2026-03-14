package ch.rodano.api.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class TimelineGraphSectionDTO {

	@NotNull
	private UUID graphSectionId;
	@NotBlank
	private String id;
	@NotNull
	private String type;

	@NotNull
	private UUID timelineGraphId;

	private UUID datasetModelId;
	private UUID dateFieldId;
	private UUID endDateFieldId;
	private UUID labelFieldId;
	private UUID valueFieldId;

	private boolean hideExpectedEvent;
	private boolean hideDoneEvent;
	private boolean useScopePaths;

	private String unit;
	private String color;
	private String strokeColor;
	private BigDecimal opacity;
	private boolean dashed;
	private String mark;
	private Integer positionStart;
	private Integer positionStop;

	private BigDecimal scaleMin;
	private BigDecimal scaleMax;
	private Integer scaleDecimal;
	private BigDecimal scaleMarkInterval;
	private BigDecimal scaleLabelInterval;
	private String scalePosition;

	private boolean hiddenLegend;
	private boolean hidden;

	@NotNull
	private SortedMap<String, String> label;
	private SortedMap<String, String> tooltip;

	private List<UUID> eventModelIds;
	private List<UUID> metaFieldIds;

	@Schema(description = "Section References")
	@NotNull
	private List<TimelineGraphSectionReferenceDTO> references;

	public UUID getGraphSectionId() {
		return graphSectionId;
	}

	public void setGraphSectionId(final UUID graphSectionId) {
		this.graphSectionId = graphSectionId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public UUID getTimelineGraphId() {
		return timelineGraphId;
	}

	public void setTimelineGraphId(final UUID timelineGraphId) {
		this.timelineGraphId = timelineGraphId;
	}

	public UUID getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final UUID datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public UUID getDateFieldId() {
		return dateFieldId;
	}

	public void setDateFieldId(final UUID dateFieldId) {
		this.dateFieldId = dateFieldId;
	}

	public UUID getEndDateFieldId() {
		return endDateFieldId;
	}

	public void setEndDateFieldId(final UUID endDateFieldId) {
		this.endDateFieldId = endDateFieldId;
	}

	public UUID getLabelFieldId() {
		return labelFieldId;
	}

	public void setLabelFieldId(final UUID labelFieldId) {
		this.labelFieldId = labelFieldId;
	}

	public UUID getValueFieldId() {
		return valueFieldId;
	}

	public void setValueFieldId(final UUID valueFieldId) {
		this.valueFieldId = valueFieldId;
	}

	public boolean isHideExpectedEvent() {
		return hideExpectedEvent;
	}

	public void setHideExpectedEvent(final boolean hideExpectedEvent) {
		this.hideExpectedEvent = hideExpectedEvent;
	}

	public boolean isHideDoneEvent() {
		return hideDoneEvent;
	}

	public void setHideDoneEvent(final boolean hideDoneEvent) {
		this.hideDoneEvent = hideDoneEvent;
	}

	public boolean isUseScopePaths() {
		return useScopePaths;
	}

	public void setUseScopePaths(final boolean useScopePaths) {
		this.useScopePaths = useScopePaths;
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

	public BigDecimal getOpacity() {
		return opacity;
	}

	public void setOpacity(final BigDecimal opacity) {
		this.opacity = opacity;
	}

	public boolean isDashed() {
		return dashed;
	}

	public void setDashed(final boolean dashed) {
		this.dashed = dashed;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(final String mark) {
		this.mark = mark;
	}

	public Integer getPositionStart() {
		return positionStart;
	}

	public void setPositionStart(final Integer positionStart) {
		this.positionStart = positionStart;
	}

	public Integer getPositionStop() {
		return positionStop;
	}

	public void setPositionStop(final Integer positionStop) {
		this.positionStop = positionStop;
	}

	public BigDecimal getScaleMin() {
		return scaleMin;
	}

	public void setScaleMin(final BigDecimal scaleMin) {
		this.scaleMin = scaleMin;
	}

	public BigDecimal getScaleMax() {
		return scaleMax;
	}

	public void setScaleMax(final BigDecimal scaleMax) {
		this.scaleMax = scaleMax;
	}

	public Integer getScaleDecimal() {
		return scaleDecimal;
	}

	public void setScaleDecimal(final Integer scaleDecimal) {
		this.scaleDecimal = scaleDecimal;
	}

	public BigDecimal getScaleMarkInterval() {
		return scaleMarkInterval;
	}

	public void setScaleMarkInterval(final BigDecimal scaleMarkInterval) {
		this.scaleMarkInterval = scaleMarkInterval;
	}

	public BigDecimal getScaleLabelInterval() {
		return scaleLabelInterval;
	}

	public void setScaleLabelInterval(final BigDecimal scaleLabelInterval) {
		this.scaleLabelInterval = scaleLabelInterval;
	}

	public String getScalePosition() {
		return scalePosition;
	}

	public void setScalePosition(final String scalePosition) {
		this.scalePosition = scalePosition;
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

	public SortedMap<String, String> getLabel() {
		return label;
	}

	public void setLabel(final SortedMap<String, String> label) {
		this.label = label;
	}

	public SortedMap<String, String> getTooltip() {
		return tooltip;
	}

	public void setTooltip(final SortedMap<String, String> tooltip) {
		this.tooltip = tooltip;
	}

	public List<UUID> getEventModelIds() {
		return eventModelIds;
	}

	public void setEventModelIds(final List<UUID> eventModelIds) {
		this.eventModelIds = eventModelIds;
	}

	public List<UUID> getMetaFieldIds() {
		return metaFieldIds;
	}

	public void setMetaFieldIds(final List<UUID> metaFieldIds) {
		this.metaFieldIds = metaFieldIds;
	}

	public List<TimelineGraphSectionReferenceDTO> getReferences() {
		return references;
	}

	public void setReferences(final List<TimelineGraphSectionReferenceDTO> references) {
		this.references = references;
	}
}
