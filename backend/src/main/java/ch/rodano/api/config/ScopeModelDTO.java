package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class ScopeModelDTO {
	@NotNull
	UUID scopeModelId;
	@NotBlank
	String id;
	@NotNull
	SortedMap<String, String> shortname;
	SortedMap<String, String> longname;
	SortedMap<String, String> description;
	@NotNull
	SortedMap<String, String> pluralShortname;

	@NotNull
	List<UUID> parentIds;
	@NotNull
	UUID defaultParentId;
	@NotNull
	List<UUID> childScopeModelIds;

	@NotNull
	boolean root;
	@NotNull
	boolean leaf;
	@NotNull
	boolean virtual;

	Integer expectedNumber;
	Integer maxNumber;
	String scopeFormat;
	String layout;

	@Schema(description = "Event groups")
	@NotNull
	List<EventGroupDTO> eventGroups;
	@Schema(description = "Event models")
	@NotNull
	List<EventModelDTO> eventModels;

	@NotNull
	List<UUID> datasetModelIds;
	@NotNull
	List<UUID> formModelIds;
	@NotNull
	List<UUID> workflowIds;

	@NotNull
	UUID defaultProfileId;

	public UUID getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final UUID scopeModelId) {
		this.scopeModelId = scopeModelId;
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

	public SortedMap<String, String> getPluralShortname() {
		return pluralShortname;
	}

	public void setPluralShortname(final SortedMap<String, String> pluralShortname) {
		this.pluralShortname = pluralShortname;
	}

	public List<UUID> getParentIds() {
		return parentIds;
	}

	public void setParentIds(final List<UUID> parentIds) {
		this.parentIds = parentIds;
	}

	public UUID getDefaultParentId() {
		return defaultParentId;
	}

	public void setDefaultParentId(final UUID defaultParentId) {
		this.defaultParentId = defaultParentId;
	}

	public UUID getDefaultProfileId() {
		return defaultProfileId;
	}

	public void setDefaultProfileId(final UUID defaultProfileId) {
		this.defaultProfileId = defaultProfileId;
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(final boolean root) {
		this.root = root;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(final boolean leaf) {
		this.leaf = leaf;
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

	public String getLayout() {
		return layout;
	}

	public void setLayout(final String layout) {
		this.layout = layout;
	}

	public List<EventGroupDTO> getEventGroups() {
		return eventGroups;
	}

	public void setEventGroups(final List<EventGroupDTO> eventGroups) {
		this.eventGroups = eventGroups;
	}

	public List<EventModelDTO> getEventModels() {
		return eventModels;
	}

	public void setEventModels(final List<EventModelDTO> eventModels) {
		this.eventModels = eventModels;
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

	public List<UUID> getChildScopeModelIds() {
		return childScopeModelIds;
	}

	public void setChildScopeModelIds(final List<UUID> childScopeModelIds) {
		this.childScopeModelIds = childScopeModelIds;
	}
}
