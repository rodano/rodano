package ch.rodano.api.field;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.config.FieldModelDTO;
import ch.rodano.api.config.PossibleValueDTO;
import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.api.workflow.WorkflowStatusDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldDTO {
	@Schema(description = "Scope reference")
	@NotNull
	Long scopePk;
	@NotNull
	String scopeId;
	@NotBlank
	String scopeCodeAndShortname;

	@Schema(description = "Event reference")
	Long eventPk;
	String eventId;
	String eventShortname;

	@Schema(description = "Dataset reference")
	@NotNull
	Long datasetPk;
	@NotBlank
	String datasetId;
	@NotBlank
	String datasetModelId;

	@Schema(description = "Field pk")
	@NotNull
	Long pk;

	@NotNull
	ZonedDateTime creationTime;
	@NotNull
	ZonedDateTime lastUpdateTime;

	@Schema(description = "Field model")
	@NotNull
	FieldModelDTO model;
	@Schema(description = "Field model id")
	@NotBlank
	String modelId;

	@Schema(description = "Is the field attached to a removed entity?")
	@NotNull
	boolean inRemoved;
	@NotNull
	@Schema(description = "Is the field attached to a locked entity?")
	boolean inLocked;

	@Schema(description = "Possible values of the field, if the field type is a multiple")
	@NotNull
	List<PossibleValueDTO> possibleValues;

	String value;
	String valueLabel;
	boolean newContent;

	Long filePk;
	String fileName;

	@NotNull
	List<WorkflowStatusDTO> workflowStatuses;
	@NotNull
	List<WorkflowDTO> possibleWorkflows;

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

	public String getScopeCodeAndShortname() {
		return scopeCodeAndShortname;
	}

	public void setScopeCodeAndShortname(final String scopeCodeAndShortname) {
		this.scopeCodeAndShortname = scopeCodeAndShortname;
	}

	public Long getEventPk() {
		return eventPk;
	}

	public void setEventPk(final Long eventPk) {
		this.eventPk = eventPk;
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

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public String getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public Long getDatasetPk() {
		return datasetPk;
	}

	public void setDatasetPk(final Long datasetPk) {
		this.datasetPk = datasetPk;
	}

	public String getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(final String datasetId) {
		this.datasetId = datasetId;
	}

	public final FieldModelDTO getModel() {
		return model;
	}

	public final void setModel(final FieldModelDTO model) {
		this.model = model;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(final String modelId) {
		this.modelId = modelId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public String getValueLabel() {
		return valueLabel;
	}

	public void setValueLabel(final String valueLabel) {
		this.valueLabel = valueLabel;
	}

	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public final ZonedDateTime getCreationTime() {
		return creationTime;
	}

	public final void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public boolean isNewContent() {
		return newContent;
	}

	public void setNewContent(final boolean newContent) {
		this.newContent = newContent;
	}

	public final List<WorkflowStatusDTO> getWorkflowStatuses() {
		return workflowStatuses;
	}

	public final void setWorkflowStatuses(final List<WorkflowStatusDTO> workflowStatus) {
		this.workflowStatuses = workflowStatus;
	}

	public List<WorkflowDTO> getPossibleWorkflows() {
		return possibleWorkflows;
	}

	public void setPossibleWorkflows(final List<WorkflowDTO> possibleWorkflows) {
		this.possibleWorkflows = possibleWorkflows;
	}

	public List<PossibleValueDTO> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(final List<PossibleValueDTO> possibleValues) {
		this.possibleValues = possibleValues;
	}

	public Long getFilePk() {
		return filePk;
	}

	public void setFilePk(final Long filePk) {
		this.filePk = filePk;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
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
}
