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
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private UUID eventGroupId;
	private UUID scopeModelId;

	@NotNull
	private List<UUID> datasetModelIds;
	@NotNull
	private List<UUID> formModelIds;
	@NotNull
	private List<UUID> workflowIds;

	private Integer deadline;
	private ChronoUnit deadlineUnit;
	private List<UUID> deadlineReferenceEventModelIds;
	private List<UUID> blockedEventModelIds;
	private List<UUID> impliedEventModelIds;
	private DateAggregationFunction deadlineAggregationFunction;

	private boolean inceptive;
	private boolean mandatory;
	private boolean preventAdd;

	private Integer maxOccurrence;
	private Integer interval;
	private ChronoUnit intervalUnit;

	private String labelPattern;
	private String icon;
	// TODO number doesn't mean anything, this property should be renamed
	private int number;

	/**
	 * Default constructor, needed by some serializer
	 */
	public EventModelDTO() {

	}

	public EventModelDTO(final EventModel eventModel) {
		eventModelId = eventModel.getEventModelId();
		id = eventModel.getId();
		shortname = eventModel.getShortname();

		eventGroupId = eventModel.getEventGroupUuid();

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

	public UUID getEventGroupId() {
		return eventGroupId;
	}

	public void setEventGroupId(final UUID eventGroupId) {
		this.eventGroupId = eventGroupId;
	}

	public UUID getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final UUID scopeModelId) {
		this.scopeModelId = scopeModelId;
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

	public List<UUID> getBlockedEventModelIds() {
		return blockedEventModelIds;
	}

	public void setBlockedEventModelIds(final List<UUID> blockedEventModelIds) {
		this.blockedEventModelIds = blockedEventModelIds;
	}

	public List<UUID> getImpliedEventModelIds() {
		return impliedEventModelIds;
	}

	public void setImpliedEventModelIds(final List<UUID> impliedEventModelIds) {
		this.impliedEventModelIds = impliedEventModelIds;
	}

	public DateAggregationFunction getDeadlineAggregationFunction() {
		return deadlineAggregationFunction;
	}

	public void setDeadlineAggregationFunction(final DateAggregationFunction deadlineAggregationFunction) {
		this.deadlineAggregationFunction = deadlineAggregationFunction;
	}

	public boolean isInceptive() {
		return inceptive;
	}

	public void setInceptive(final boolean inceptive) {
		this.inceptive = inceptive;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isPreventAdd() {
		return preventAdd;
	}

	public void setPreventAdd(final boolean preventAdd) {
		this.preventAdd = preventAdd;
	}

	public Integer getMaxOccurrence() {
		return maxOccurrence;
	}

	public void setMaxOccurrence(final Integer maxOccurrence) {
		this.maxOccurrence = maxOccurrence;
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

	public String getLabelPattern() {
		return labelPattern;
	}

	public void setLabelPattern(final String labelPattern) {
		this.labelPattern = labelPattern;
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
