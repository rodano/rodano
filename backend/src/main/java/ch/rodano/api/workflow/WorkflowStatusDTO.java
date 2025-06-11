package ch.rodano.api.workflow;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class WorkflowStatusDTO {
	@NotNull
	Long pk;
	@Schema(description = "The workflow ID")
	@NotBlank
	String workflowId;
	@Schema(description = "The workflow status ID")
	@NotBlank
	String statusId;
	@Schema(description = "User message that was attached to the latest status change")
	@NotBlank
	String triggerMessage;
	@Schema(description = "The workflow model of the WorkflowStatus")
	@NotNull
	WorkflowDTO workflow;
	@Schema(description = "The current state of the WorkflowStatus")
	@NotNull
	WorkflowStateDTO state;
	@Schema(description = "Date of the WorkflowStatus")
	@NotNull
	ZonedDateTime date;
	Integer orderBy;
	@Schema(description = "Does this WorkflowStatus have a creation action?")
	@NotNull
	boolean hasCreationAction;

	// Scope info
	@Schema(description = "Scope reference of the WorkflowStatus")
	@NotNull
	Long scopeFk;
	@Schema(description = "Code of the scope to which WorkflowStatus is attached to")
	@NotBlank
	String scopeCode;
	@Schema(description = "Shortname of the scope to which WorkflowStatus is attached to")
	@NotBlank
	String scopeShortname;

	// Event info
	@Schema(description = "Event reference of the WorkflowStatus")
	Long eventFk;
	@Schema(description = "Shortname of the event to which WorkflowStatus is attached to")
	String eventShortname;
	@Schema(description = "Date of the event to which WorkflowStatus is attached to")
	ZonedDateTime eventDate;

	// Field info
	@Schema(description = "Field reference of the WorkflowStatus")
	Long fieldFk;
	@Schema(description = "Shortname of the field to which WorkflowStatus is attached to")
	String fieldShortname;

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(final String statusId) {
		this.statusId = statusId;
	}

	public String getTriggerMessage() {
		return triggerMessage;
	}

	public void setTriggerMessage(final String triggerMessage) {
		this.triggerMessage = triggerMessage;
	}

	public WorkflowDTO getWorkflow() {
		return workflow;
	}

	public void setWorkflow(final WorkflowDTO workflow) {
		this.workflow = workflow;
	}

	public WorkflowStateDTO getState() {
		return state;
	}

	public void setState(final WorkflowStateDTO state) {
		this.state = state;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(final ZonedDateTime date) {
		this.date = date;
	}

	public Integer getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(final Integer orderBy) {
		this.orderBy = orderBy;
	}

	public boolean isHasCreationAction() {
		return hasCreationAction;
	}

	public void setHasCreationAction(final boolean hasCreationAction) {
		this.hasCreationAction = hasCreationAction;
	}

	public Long getScopeFk() {
		return scopeFk;
	}

	public void setScopeFk(final Long scopeFk) {
		this.scopeFk = scopeFk;
	}

	public String getScopeCode() {
		return scopeCode;
	}

	public void setScopeCode(final String scopeCode) {
		this.scopeCode = scopeCode;
	}

	public String getScopeShortname() {
		return scopeShortname;
	}

	public void setScopeShortname(final String scopeShortname) {
		this.scopeShortname = scopeShortname;
	}

	public Long getEventFk() {
		return eventFk;
	}

	public void setEventFk(final Long eventFk) {
		this.eventFk = eventFk;
	}

	public String getEventShortname() {
		return eventShortname;
	}

	public void setEventShortname(final String eventShortname) {
		this.eventShortname = eventShortname;
	}

	public ZonedDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(final ZonedDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public Long getFieldFk() {
		return fieldFk;
	}

	public void setFieldFk(final Long fieldFk) {
		this.fieldFk = fieldFk;
	}

	public String getFieldShortname() {
		return fieldShortname;
	}

	public void setFieldShortname(final String fieldShortname) {
		this.fieldShortname = fieldShortname;
	}
}
