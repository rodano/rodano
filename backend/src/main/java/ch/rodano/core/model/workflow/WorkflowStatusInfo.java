package ch.rodano.core.model.workflow;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.scope.ScopeTinyDTO;
import ch.rodano.configuration.model.reports.WorkflowWidgetColumnType;

@Schema(description = "An auxiliary object that provides information relevant to a WorkflowStatus")
public class WorkflowStatusInfo {

	@NotNull
	private List<ScopeTinyDTO> ancestors;
	@NotBlank
	private String parentScopeCode;

	@NotNull
	private Long scopePk;
	@NotBlank
	private String scopeModelId;
	@NotBlank
	private String scopeCode;
	@NotBlank
	private String scopeShortname;

	//workflowable
	private Long eventPk;
	private String eventId;
	private String eventLabel;
	private ZonedDateTime eventDate;

	@NotNull
	private Long formPk;
	@NotBlank
	private String formModelId;
	@NotBlank
	private String formLabel;
	@NotBlank
	private ZonedDateTime formDate;

	@NotBlank
	private Long fieldPk;
	@NotBlank
	private String datasetModelId;
	@NotBlank
	private String fieldModelId;
	@NotBlank
	private String fieldLabel;
	@NotBlank
	private ZonedDateTime fieldDate;

	//status
	@NotNull
	private Long pk;
	@NotBlank
	private String workflow;
	@NotBlank
	private String triggerMessage;

	@NotBlank
	private String status;
	@NotBlank
	private ZonedDateTime statusDate;
	@NotBlank
	private String statusIcon;
	@NotBlank
	private String statusColor;

	@JsonIgnore
	public Object getValue(final WorkflowWidgetColumnType type) {
		return switch(type) {
			case WORKFLOW_LABEL -> workflow;
			case WORKFLOW_TRIGGER_MESSAGE -> triggerMessage;
			case STATUS_LABEL -> status;
			case STATUS_DATE -> statusDate;
			case EVENT_LABEL -> eventLabel;
			case EVENT_DATE -> eventDate;
			case FORM_LABEL -> formLabel;
			case FORM_DATE -> formDate;
			case FIELD_LABEL -> fieldLabel;
			case FIELD_DATE -> fieldDate;
			default -> throw new IllegalArgumentException("Cannot return widget value for type " + type);
		};
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public Long getScopePk() {
		return scopePk;
	}

	public void setScopePk(final Long scopePk) {
		this.scopePk = scopePk;
	}

	public String getScopeCode() {
		return scopeCode;
	}

	public void setScopeCode(final String scopeCode) {
		this.scopeCode = scopeCode;
	}

	public String getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final String scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public String getScopeShortname() {
		return scopeShortname;
	}

	public void setScopeShortname(final String scopeShortname) {
		this.scopeShortname = scopeShortname;
	}

	public List<ScopeTinyDTO> getAncestors() {
		return ancestors;
	}

	public void setAncestors(final List<ScopeTinyDTO> ancestors) {
		this.ancestors = ancestors;
	}

	public String getParentScopeCode() {
		return parentScopeCode;
	}

	public void setParentScopeCode(final String parentScopeCode) {
		this.parentScopeCode = parentScopeCode;
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

	public String getEventLabel() {
		return eventLabel;
	}

	public void setEventLabel(final String visit) {
		this.eventLabel = visit;
	}

	public ZonedDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(final ZonedDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public Long getFormPk() {
		return formPk;
	}

	public void setFormPk(final Long formPk) {
		this.formPk = formPk;
	}

	public String getFormModelId() {
		return formModelId;
	}

	public void setFormModelId(final String formModelId) {
		this.formModelId = formModelId;
	}

	public String getFormLabel() {
		return formLabel;
	}

	public void setFormLabel(final String formLabel) {
		this.formLabel = formLabel;
	}

	public ZonedDateTime getFormDate() {
		return formDate;
	}

	public void setFormDate(final ZonedDateTime formDate) {
		this.formDate = formDate;
	}

	public Long getFieldPk() {
		return fieldPk;
	}

	public void setFieldPk(final Long fieldPk) {
		this.fieldPk = fieldPk;
	}

	public String getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public String getFieldModelId() {
		return fieldModelId;
	}

	public void setFieldModelId(final String fieldModelId) {
		this.fieldModelId = fieldModelId;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(final String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public ZonedDateTime getFieldDate() {
		return fieldDate;
	}

	public void setFieldDate(final ZonedDateTime fieldDate) {
		this.fieldDate = fieldDate;
	}

	public String getTriggerMessage() {
		return triggerMessage;
	}

	public void setTriggerMessage(final String triggerMessage) {
		this.triggerMessage = triggerMessage;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(final String workflow) {
		this.workflow = workflow;
	}

	public ZonedDateTime getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(final ZonedDateTime statusDate) {
		this.statusDate = statusDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public String getStatusIcon() {
		return statusIcon;
	}

	public void setStatusIcon(final String statusIcon) {
		this.statusIcon = statusIcon;
	}

	public String getStatusColor() {
		return statusColor;
	}

	public void setStatusColor(final String statusColor) {
		this.statusColor = statusColor;
	}
}
