package ch.rodano.batch.pojo;


import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventModel {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private String eventGroupId;
	private boolean inceptive;
	private Integer number;
	private Boolean mandatory;
	private Integer maxOccurrence;
	private Boolean preventAdd;

	private Integer deadline;
	private String deadlineUnit;
	private String deadlineAggregationFunction;

	private Integer interval;
	private String intervalUnit;

	private String labelPattern;
	private String icon;

	private List<String> datasetModelIds;
	private List<String> formModelIds;
	private List<String> workflowIds;

	private List<String> deadlineReferenceEventModelIds;
	private List<String> impliedEventModelIds;
	private List<String> blockedEventModelIds;

	private List<Rule> createRules;
	private List<Rule> removeRules;
	private List<Rule> restoreRules;

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

	public String getEventGroupId() {
		return eventGroupId;
	}

	public void setEventGroupId(final String eventGroupId) {
		this.eventGroupId = eventGroupId;
	}

	public boolean isInceptive() {
		return inceptive;
	}

	public void setInceptive(final boolean inceptive) {
		this.inceptive = inceptive;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(final Integer number) {
		this.number = number;
	}

	public Boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(final Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Integer getMaxOccurrence() {
		return maxOccurrence;
	}

	public void setMaxOccurrence(final Integer maxOccurrence) {
		this.maxOccurrence = maxOccurrence;
	}

	public Boolean getPreventAdd() {
		return preventAdd;
	}

	public void setPreventAdd(final Boolean preventAdd) {
		this.preventAdd = preventAdd;
	}

	public Integer getDeadline() {
		return deadline;
	}

	public void setDeadline(final Integer deadline) {
		this.deadline = deadline;
	}

	public String getDeadlineUnit() {
		return deadlineUnit;
	}

	public void setDeadlineUnit(final String deadlineUnit) {
		this.deadlineUnit = deadlineUnit;
	}

	public String getDeadlineAggregationFunction() {
		return deadlineAggregationFunction;
	}

	public void setDeadlineAggregationFunction(final String deadlineAggregationFunction) {
		this.deadlineAggregationFunction = deadlineAggregationFunction;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(final Integer interval) {
		this.interval = interval;
	}

	public String getIntervalUnit() {
		return intervalUnit;
	}

	public void setIntervalUnit(final String intervalUnit) {
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

	public List<String> getDeadlineReferenceEventModelIds() {
		return deadlineReferenceEventModelIds;
	}

	public void setDeadlineReferenceEventModelIds(final List<String> deadlineReferenceEventModelIds) {
		this.deadlineReferenceEventModelIds = deadlineReferenceEventModelIds;
	}

	public List<String> getImpliedEventModelIds() {
		return impliedEventModelIds;
	}

	public void setImpliedEventModelIds(final List<String> impliedEventModelIds) {
		this.impliedEventModelIds = impliedEventModelIds;
	}

	public List<String> getBlockedEventModelIds() {
		return blockedEventModelIds;
	}

	public void setBlockedEventModelIds(final List<String> blockedEventModelIds) {
		this.blockedEventModelIds = blockedEventModelIds;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public List<Rule> getCreateRules() {
		return createRules;
	}

	public void setCreateRules(final List<Rule> createRules) {
		this.createRules = createRules;
	}

	public List<Rule> getRemoveRules() {
		return removeRules;
	}

	public void setRemoveRules(final List<Rule> removeRules) {
		this.removeRules = removeRules;
	}

	public List<Rule> getRestoreRules() {
		return restoreRules;
	}

	public void setRestoreRules(final List<Rule> restoreRules) {
		this.restoreRules = restoreRules;
	}

	@Override
	public String toString() {
		return "EventModel{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", eventGroupId='" + eventGroupId + '\'' +
			", inceptive=" + inceptive +
			", number=" + number +
			", mandatory=" + mandatory +
			", maxOccurrence=" + maxOccurrence +
			", preventAdd=" + preventAdd +
			", deadline=" + deadline +
			", deadlineUnit='" + deadlineUnit + '\'' +
			", deadlineAggregationFunction='" + deadlineAggregationFunction + '\'' +
			", interval=" + interval +
			", intervalUnit='" + intervalUnit + '\'' +
			", labelPattern='" + labelPattern + '\'' +
			", icon='" + icon + '\'' +
			", datasetModelIds=" + datasetModelIds +
			", formModelIds=" + formModelIds +
			", workflowIds=" + workflowIds +
			", deadlineReferenceEventModelIds=" + deadlineReferenceEventModelIds +
			", impliedEventModelIds=" + impliedEventModelIds +
			", blockedEventModelIds=" + blockedEventModelIds +
			", createRules=" + createRules +
			", removeRules=" + removeRules +
			", restoreRules=" + restoreRules +
			'}';
	}
}
