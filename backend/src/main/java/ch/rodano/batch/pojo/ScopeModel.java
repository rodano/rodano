package ch.rodano.batch.pojo;


import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ScopeModel {

	private String id;

	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private Map<String, String> pluralShortname;

	private String defaultParentId;
	private List<String> parentIds;

	private boolean virtual;
	private Integer expectedNumber;
	private Integer maxNumber;
	private String scopeFormat;

	private List<EventGroup> eventGroups;
	private List<EventModel> eventModels;

	private String defaultProfileId;
	private List<String> datasetModelIds;
	private List<String> formModelIds;
	private List<String> workflowIds;

	private List<WorkflowStatesSelector> workflowStatesSelectors;

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

	public Map<String, String> getPluralShortname() {
		return pluralShortname;
	}

	public void setPluralShortname(final Map<String, String> pluralShortname) {
		this.pluralShortname = pluralShortname;
	}

	public String getDefaultParentId() {
		return defaultParentId;
	}

	public void setDefaultParentId(final String defaultParentId) {
		this.defaultParentId = defaultParentId;
	}

	public List<String> getParentIds() {
		return parentIds;
	}

	public void setParentIds(final List<String> parentIds) {
		this.parentIds = parentIds;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(final boolean virtual) {
		this.virtual = virtual;
	}

	public Integer getExpectedNumber() {
		return expectedNumber;
	}

	public void setExpectedNumber(final Integer expectedNumber) {
		this.expectedNumber = expectedNumber;
	}

	public Integer getMaxNumber() {
		return maxNumber;
	}

	public void setMaxNumber(final Integer maxNumber) {
		this.maxNumber = maxNumber;
	}

	public String getScopeFormat() {
		return scopeFormat;
	}

	public void setScopeFormat(final String scopeFormat) {
		this.scopeFormat = scopeFormat;
	}

	public List<EventGroup> getEventGroups() {
		return eventGroups;
	}

	public void setEventGroups(final List<EventGroup> eventGroups) {
		this.eventGroups = eventGroups;
	}

	public List<EventModel> getEventModels() {
		return eventModels;
	}

	public void setEventModels(final List<EventModel> eventModels) {
		this.eventModels = eventModels;
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

	public List<WorkflowStatesSelector> getWorkflowStatesSelectors() {
		return workflowStatesSelectors;
	}

	public void setWorkflowStatesSelectors(final List<WorkflowStatesSelector> workflowStatesSelectors) {
		this.workflowStatesSelectors = workflowStatesSelectors;
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

	public String getDefaultProfileId() {
		return defaultProfileId;
	}

	public void setDefaultProfileId(final String defaultProfileId) {
		this.defaultProfileId = defaultProfileId;
	}

	@Override
	public String toString() {
		return "ScopeModel{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", pluralShortname=" + pluralShortname +
			", defaultParentId='" + defaultParentId + '\'' +
			", parentIds=" + parentIds +
			", virtual=" + virtual +
			", expectedNumber=" + expectedNumber +
			", maxNumber=" + maxNumber +
			", scopeFormat='" + scopeFormat + '\'' +
			", eventGroups=" + eventGroups +
			", eventModels=" + eventModels +
			", defaultProfileId='" + defaultProfileId + '\'' +
			", datasetModelIds=" + datasetModelIds +
			", formModelIds=" + formModelIds +
			", workflowIds=" + workflowIds +
			", workflowStatesSelectors=" + workflowStatesSelectors +
			", createRules=" + createRules +
			", removeRules=" + removeRules +
			", restoreRules=" + restoreRules +
			'}';
	}
}
