package ch.rodano.api.config;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.event.DateAggregationFunction;
import ch.rodano.configuration.model.event.EventModel;

public class EventModelDTO {
	@NotBlank
	private String id;
	@NotNull
	private SortedMap<String, String> shortname;

	private String eventGroupId;

	@NotNull
	private List<String> datasetModelIds;
	@NotNull
	private List<String> formModelIds;
	@NotNull
	private List<String> workflowIds;

	private Integer deadline;
	private ChronoUnit deadlineUnit;
	private List<String> deadlineReferenceEventModelIds;
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
		id = eventModel.getId();
		shortname = eventModel.getShortname();

		eventGroupId = eventModel.getEventGroupId();

		datasetModelIds = eventModel.getDatasetModelIds();
		formModelIds = eventModel.getFormModelIds();
		workflowIds = eventModel.getWorkflowIds();

		deadline = eventModel.getDeadline();
		deadlineUnit = eventModel.getDeadlineUnit();
		deadlineReferenceEventModelIds = eventModel.getDeadlineReferenceEventModelIds();
		deadlineAggregationFunction = eventModel.getDeadlineAggregationFunctionOrDefault();

		interval = eventModel.getInterval();
		intervalUnit = eventModel.getIntervalUnit();

		icon = eventModel.getIcon();
		number = eventModel.getNumber();
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

	public List<String> getDatasetModelIds() {
		return datasetModelIds;
	}

	public void setDatasetModelIds(final List<String> datasetModelIds) {
		this.datasetModelIds = datasetModelIds;
	}

	public List<String> getFormModelIds() {
		return formModelIds;
	}

	public void setFormModelIds(final List<String> formModelIds) {
		this.formModelIds = formModelIds;
	}

	public List<String> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<String> workflowIds) {
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

	public List<String> getDeadlineReferenceEventModelIds() {
		return deadlineReferenceEventModelIds;
	}

	public void setDeadlineReferenceEventModelIds(final List<String> deadlineReferenceEventModelIds) {
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
