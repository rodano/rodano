package ch.rodano.api.form;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.config.FormModelDTO;
import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.api.workflow.WorkflowStatusDTO;

@Schema(description = "A form is a visual grouping of fields.")
public class FormDTO {
	@Schema(description = "Scope reference")
	@NotNull
	Long scopePk;
	@NotEmpty
	String scopeId;
	@NotEmpty
	String scopeCodeAndShortname;

	@Schema(description = "Event reference")
	Long eventPk;
	String eventId;
	String eventShortname;

	@Schema(description = "Form pk")
	@NotNull
	Long pk;

	@NotNull
	ZonedDateTime creationTime;
	@NotNull
	ZonedDateTime lastUpdateTime;

	@Schema(description = "Form model")
	@NotNull
	FormModelDTO model;
	@Schema(description = "ID of the form model")
	@NotBlank
	String modelId;

	@NotNull
	boolean removed;
	@Schema(description = "Is the form attached to a removed entity?")
	@NotNull
	boolean inRemoved;
	@NotNull
	@Schema(description = "Is the form attached to a locked entity?")
	boolean inLocked;

	@Schema(description = "Can the user update the form?")
	@NotNull
	boolean canWrite;
	@Schema(description = "Can the user remove the form?")
	@NotNull
	boolean canBeRemoved;

	@NotNull
	boolean printable;
	Map<String, String> printButtonLabel;

	@NotNull
	List<WorkflowStatusDTO> workflowStatuses;
	@NotNull
	List<WorkflowDTO> possibleWorkflows;

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
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

	public Long getScopePk() {
		return scopePk;
	}

	public void setScopePk(final Long scopePk) {
		this.scopePk = scopePk;
	}

	public Long getEventPk() {
		return eventPk;
	}

	public void setEventPk(final Long eventPk) {
		this.eventPk = eventPk;
	}

	public String getScopeId() {
		return scopeId;
	}

	public void setScopeId(final String scopeId) {
		this.scopeId = scopeId;
	}

	public String getScopeCodeAndShortname() {
		return scopeCodeAndShortname;
	}

	public void setScopeCodeAndShortname(final String scopeCodeAndShortname) {
		this.scopeCodeAndShortname = scopeCodeAndShortname;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(final String eventId) {
		this.eventId = eventId;
	}

	public String getEventShortname() {
		return eventShortname;
	}

	public void setEventShortname(final String eventShortname) {
		this.eventShortname = eventShortname;
	}

	public FormModelDTO getModel() {
		return model;
	}

	public void setModel(final FormModelDTO model) {
		this.model = model;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(final String modelId) {
		this.modelId = modelId;
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

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(final boolean removed) {
		this.removed = removed;
	}

	public boolean isInRemoved() {
		return inRemoved;
	}

	public void setInRemoved(final boolean inRemoved) {
		this.inRemoved = inRemoved;
	}

	public boolean isInLocked() {
		return inLocked;
	}

	public void setInLocked(final boolean inLocked) {
		this.inLocked = inLocked;
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

	public boolean isPrintable() {
		return printable;
	}

	public void setPrintable(final boolean printable) {
		this.printable = printable;
	}

	public Map<String, String> getPrintButtonLabel() {
		return printButtonLabel;
	}

	public void setPrintButtonLabel(final Map<String, String> printButtonLabel) {
		this.printButtonLabel = printButtonLabel;
	}

}
