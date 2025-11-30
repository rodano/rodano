package ch.rodano.api.config;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.event.DateAggregationFunction;
import ch.rodano.configuration.model.event.EventModel;

public class EventModelDTO {
	@NotNull
	private UUID eventModelId;
	@NotBlank
	private String id;
	@NotNull
	private SortedMap<String, String> shortname;

	private String eventGroupId;

	@NotNull
	private List<UUID> datasetModelIds;
	@NotNull
	private List<UUID> formModelIds;
	@NotNull
	private List<UUID> workflowIds;

	private Integer deadline;
	private ChronoUnit deadlineUnit;
	private List<UUID> deadlineReferenceEventModelIds;
	private DateAggregationFunction deadlineAggregationFunction;

	private Integer interval;
	private ChronoUnit intervalUnit;

	private String icon;
	// TODO number doesn't mean anything, this property should be renamed
	private int number;

	/**
	 * Default constructor, needed by some serializer
	 */
	EventModelDTO() {

	}

	public EventModelDTO(final EventModel eventModel) {
		eventModelId = eventModel.getEventModelId();
		id = eventModel.getId();
		shortname = eventModel.getShortname();

		eventGroupId = eventModel.getEventGroupId();

		datasetModelIds = eventModel.getDatasetModelUuids();
		formModelIds = eventModel.getFormModelUuids();
		workflowIds = eventModel.getWorkflowUuids();

		deadline = eventModel.getDeadline();
		deadlineUnit = eventModel.getDeadlineUnit();
		deadlineReferenceEventModelIds = eventModel.getDeadlineReferenceEventModelUuids();
		deadlineAggregationFunction = eventModel.getDeadlineAggregationFunctionOrDefault();

		interval = eventModel.getInterval();
		intervalUnit = eventModel.getIntervalUnit();

		icon = eventModel.getIcon();
		number = eventModel.getNumber();
	}

	public UUID getEventModelId() {
		return eventModelId;
	}

	public void setEventModelId(final UUID eventModelId) {
		this.eventModelId = eventModelId;
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

	public String getEventGroupId() {
		return eventGroupId;
	}

	public void setEventGroupId(final String eventGroupId) {
		this.eventGroupId = eventGroupId;
	}

	public List<UUID> getDatasetModelIds() {
		return datasetModelIds;
	}

	public void setDatasetModelIds(final List<UUID> datasetModelIds) {
		this.datasetModelIds = datasetModelIds;
	}

	public List<UUID> getFormModelIds() {
		return formModelIds;
	}

	public void setFormModelIds(final List<UUID> formModelIds) {
		this.formModelIds = formModelIds;
	}

	public List<UUID> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<UUID> workflowIds) {
		this.workflowIds = workflowIds;
	}

	public Integer getDeadline() {
		return deadline;
	}

	public void setDeadline(final Integer deadline) {
		this.deadline = deadline;
	}

	public ChronoUnit getDeadlineUnit() {
		return deadlineUnit;
	}

	public void setDeadlineUnit(final ChronoUnit deadlineUnit) {
		this.deadlineUnit = deadlineUnit;
	}

	public List<UUID> getDeadlineReferenceEventModelIds() {
		return deadlineReferenceEventModelIds;
	}

	public void setDeadlineReferenceEventModelIds(final List<UUID> deadlineReferenceEventModelIds) {
		this.deadlineReferenceEventModelIds = deadlineReferenceEventModelIds;
	}

	public DateAggregationFunction getDeadlineAggregationFunction() {
		return deadlineAggregationFunction;
	}

	public void setDeadlineAggregationFunction(final DateAggregationFunction deadlineAggregationFunction) {
		this.deadlineAggregationFunction = deadlineAggregationFunction;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(final Integer interval) {
		this.interval = interval;
	}

	public ChronoUnit getIntervalUnit() {
		return intervalUnit;
	}

	public void setIntervalUnit(final ChronoUnit intervalUnit) {
		this.intervalUnit = intervalUnit;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(final int number) {
		this.number = number;
	}
}
