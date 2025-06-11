package ch.rodano.api.event;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.config.EventModelDTO;
import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.api.workflow.WorkflowStatusDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDTO {
	@Schema(description = "Scope reference")
	@NotNull
	Long scopePk;
	@NotBlank
	String scopeId;
	@NotBlank
	String scopeCodeAndShortname;

	@Schema(description = "Event pk")
	@NotNull
	Long pk;
	@NotBlank
	String id;
	@NotNull
	int eventGroupNumber;

	@NotNull
	ZonedDateTime creationTime;
	@NotNull
	ZonedDateTime lastUpdateTime;

	@Schema(description = "Event model")
	@NotNull
	EventModelDTO model;
	@Schema(description = "Event model id")
	@NotNull
	String modelId;

	@NotBlank
	String shortname;
	@NotBlank
	String longname;

	@NotNull
	boolean removed;
	@Schema(description = "Is the event attached to a removed entity?")
	@NotNull
	boolean inRemoved;

	@NotNull
	boolean locked;
	@NotNull
	@Schema(description = "Is the event attached to a locked entity?")
	boolean inLocked;

	@Schema(description = "Can the user update the event?")
	@NotNull
	boolean canWrite;
	@Schema(description = "Can the user remove the event?")
	@NotNull
	boolean canBeRemoved;

	@NotNull
	ZonedDateTime expectedDate;
	@NotNull
	ZonedDateTime date;
	ZonedDateTime endDate;
	@NotNull
	boolean mandatory;
	@NotNull
	boolean expected;
	@NotNull
	boolean notDone;
	@NotNull
	boolean blocking;

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

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getLongname() {
		return longname;
	}

	public void setLongname(final String longname) {
		this.longname = longname;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(final String shortname) {
		this.shortname = shortname;
	}

	public EventModelDTO getModel() {
		return model;
	}

	public void setModel(final EventModelDTO model) {
		this.model = model;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(final String modelId) {
		this.modelId = modelId;
	}

	public int getEventGroupNumber() {
		return eventGroupNumber;
	}

	public void setEventGroupNumber(final int eventGroupNumber) {
		this.eventGroupNumber = eventGroupNumber;
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

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(final boolean canWrite) {
		this.canWrite = canWrite;
	}

	public ZonedDateTime getExpectedDate() {
		return expectedDate;
	}

	public void setExpectedDate(final ZonedDateTime expectedDate) {
		this.expectedDate = expectedDate;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(final ZonedDateTime date) {
		this.date = date;
	}

	public ZonedDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(final ZonedDateTime endDate) {
		this.endDate = endDate;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isExpected() {
		return expected;
	}

	public void setExpected(final boolean expected) {
		this.expected = expected;
	}

	public boolean isNotDone() {
		return notDone;
	}

	public void setNotDone(final boolean notDone) {
		this.notDone = notDone;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(final boolean blocking) {
		this.blocking = blocking;
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

	public boolean isCanBeRemoved() {
		return canBeRemoved;
	}

	public void setCanBeRemoved(final boolean canBeRemoved) {
		this.canBeRemoved = canBeRemoved;
	}

	public Long getScopePk() {
		return scopePk;
	}

	public void setScopePk(final Long scopePk) {
		this.scopePk = scopePk;
	}

	public String getScopeId() {
		return scopeId;
	}

	public void setScopeId(final String scopeId) {
		this.scopeId = scopeId;
	}

	public final String getScopeCodeAndShortname() {
		return scopeCodeAndShortname;
	}

	public final void setScopeCodeAndShortname(final String scopeCodeAndShortname) {
		this.scopeCodeAndShortname = scopeCodeAndShortname;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(final boolean locked) {
		this.locked = locked;
	}
}
