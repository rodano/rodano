package ch.rodano.api.scope;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.config.ScopeModelDTO;
import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.api.workflow.WorkflowStatusDTO;
import ch.rodano.core.model.enrollment.EnrollmentModel;
import ch.rodano.core.model.enrollment.EnrollmentTarget;
import ch.rodano.core.model.enrollment.SubscriptionRestriction;

//TODO split this DTO into a DTO that the server receives and a DTO that the server sends
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScopeDTO extends ScopeMiniDTO {
	@Schema(description = "Scope model")
	@NotNull
	ScopeModelDTO model;
	@NotNull
	boolean root;

	@NotNull
	ZonedDateTime creationTime;
	@NotNull
	ZonedDateTime lastUpdateTime;

	@NotNull
	boolean removed;
	@NotNull
	boolean locked;

	@Schema(description = "Can the user update the scope?")
	@NotNull
	boolean canWrite;
	@Schema(description = "Can the user remove the scope?")
	@NotNull
	boolean canBeRemoved;

	SortedMap<String, String> description;

	Integer expectedNumber;
	Integer maxNumber;
	EnrollmentModel enrollmentModel;
	@NotNull
	List<EnrollmentTarget> enrollmentTargets = new ArrayList<>();

	@NotNull
	Integer leaves;

	Long mainUserPk;
	String mainUserName;

	@NotNull
	List<SubscriptionRestriction> subscriptionRestrictions = new ArrayList<>();
	String defaultProfileId;

	@NotNull
	List<WorkflowStatusDTO> workflowStatuses;
	@NotNull
	List<WorkflowDTO> possibleWorkflows;

	ScopeTinyDTO parentScope;

	Map<String, Map<String, String>> searchableFields = new HashMap<>();

	public ScopeModelDTO getModel() {
		return model;
	}

	public void setModel(final ScopeModelDTO model) {
		this.model = model;
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(final boolean root) {
		this.root = root;
	}

	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(final boolean locked) {
		this.locked = locked;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(final boolean removed) {
		this.removed = removed;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(final boolean canWrite) {
		this.canWrite = canWrite;
	}

	public boolean isCanBeRemoved() {
		return canBeRemoved;
	}

	public void setCanBeRemoved(final boolean canBeRemoved) {
		this.canBeRemoved = canBeRemoved;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
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

	public EnrollmentModel getEnrollmentModel() {
		return enrollmentModel;
	}

	public void setEnrollmentModel(final EnrollmentModel enrollmentModel) {
		this.enrollmentModel = enrollmentModel;
	}

	public List<EnrollmentTarget> getEnrollmentTargets() {
		return enrollmentTargets;
	}

	public void setEnrollmentTargets(final List<EnrollmentTarget> enrollmentTargets) {
		this.enrollmentTargets = enrollmentTargets;
	}

	public List<WorkflowStatusDTO> getWorkflowStatuses() {
		return workflowStatuses;
	}

	public void setWorkflowStatuses(final List<WorkflowStatusDTO> workflowStatus) {
		this.workflowStatuses = workflowStatus;
	}

	public List<WorkflowDTO> getPossibleWorkflows() {
		return possibleWorkflows;
	}

	public void setPossibleWorkflows(final List<WorkflowDTO> possibleWorkflows) {
		this.possibleWorkflows = possibleWorkflows;
	}

	public Integer getLeaves() {
		return leaves;
	}

	public void setLeaves(final Integer leaves) {
		this.leaves = leaves;
	}

	public List<SubscriptionRestriction> getSubscriptionRestrictions() {
		return subscriptionRestrictions;
	}

	public void setSubscriptionRestrictions(final List<SubscriptionRestriction> subscriptionRestrictions) {
		this.subscriptionRestrictions = subscriptionRestrictions;
	}

	public String getDefaultProfileId() {
		return defaultProfileId;
	}

	public void setDefaultProfileId(final String defaultProfileId) {
		this.defaultProfileId = defaultProfileId;
	}

	public Long getMainUserPk() {
		return mainUserPk;
	}

	public void setMainUserPk(final Long mainUserPk) {
		this.mainUserPk = mainUserPk;
	}

	public String getMainUserName() {
		return mainUserName;
	}

	public void setMainUserName(final String mainUserName) {
		this.mainUserName = mainUserName;
	}

	public ScopeTinyDTO getParentScope() {
		return parentScope;
	}

	public void setParentScope(final ScopeTinyDTO parentScope) {
		this.parentScope = parentScope;
	}

	public Map<String, Map<String, String>> getSearchableFields() {
		return searchableFields;
	}

	public void setSearchableFields(final Map<String, Map<String, String>> searchableFields) {
		this.searchableFields = searchableFields;
	}
}
