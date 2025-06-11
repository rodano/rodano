package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class ScopeModelDTO {
	@NotBlank
	String id;
	@NotNull
	SortedMap<String, String> shortname;
	SortedMap<String, String> longname;
	SortedMap<String, String> description;
	@NotNull
	SortedMap<String, String> pluralShortname;

	@NotNull
	List<String> parentIds;
	@NotBlank
	String defaultParentId;
	@NotNull
	List<String> childScopeModelIds;

	@NotNull
	boolean root;
	@NotNull
	boolean leaf;
	@NotNull
	boolean virtual;

	@Schema(description = "Event groups")
	@NotNull
	List<EventGroupDTO> eventGroups;
	@Schema(description = "Event models")
	@NotNull
	List<EventModelDTO> eventModels;

	@NotNull
	List<String> datasetModelIds;
	@NotNull
	List<String> formModelIds;
	@NotNull
	List<String> workflowIds;

	@NotBlank
	String defaultProfileId;

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

	public List<String> getParentIds() {
		return parentIds;
	}

	public void setParentIds(final List<String> parentIds) {
		this.parentIds = parentIds;
	}

	public String getDefaultParentId() {
		return defaultParentId;
	}

	public void setDefaultParentId(final String defaultParentId) {
		this.defaultParentId = defaultParentId;
	}

	public String getDefaultProfileId() {
		return defaultProfileId;
	}

	public void setDefaultProfileId(final String defaultProfileId) {
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

	public List<String> getChildScopeModelIds() {
		return childScopeModelIds;
	}

	public void setChildScopeModelIds(final List<String> childScopeModelIds) {
		this.childScopeModelIds = childScopeModelIds;
	}
}
