package ch.rodano.core.model.workflow;

public class WorkflowStatusRecord {

	protected boolean deleted;

	protected Long scopeFk;
	protected Long fieldFk;
	protected Long eventFk;
	protected Long formFk;
	protected Long userFk;
	protected Long robotFk;
	protected String profileId;
	protected String stateId;
	protected String workflowId;
	protected String actionId;
	protected String validatorId;
	protected String triggerMessage;

	protected WorkflowStatusRecord() {
		deleted = false;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(final boolean deleted) {
		this.deleted = deleted;
	}

	public Long getScopeFk() {
		return scopeFk;
	}

	public void setScopeFk(final Long workflowStatusFk) {
		this.scopeFk = workflowStatusFk;
	}

	public Long getEventFk() {
		return eventFk;
	}

	public void setEventFk(final Long eventFk) {
		this.eventFk = eventFk;
	}

	public Long getFormFk() {
		return formFk;
	}

	public void setFormFk(final Long formFk) {
		this.formFk = formFk;
	}

	public final Long getFieldFk() {
		return fieldFk;
	}

	public final void setFieldFk(final Long fieldFk) {
		this.fieldFk = fieldFk;
	}

	public Long getUserFk() {
		return userFk;
	}

	// WARNING: only use this method if you know exactly what you are doing
	public void setUserFk(final Long userFk) {
		this.userFk = userFk;
	}

	public Long getRobotFk() {
		return robotFk;
	}

	// WARNING: only use this method if you know exactly what you are doing
	public void setRobotFk(final Long robotFk) {
		this.robotFk = robotFk;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(final String profileId) {
		this.profileId = profileId;
	}

	public String getStateId() {
		return stateId;
	}

	public void setStateId(final String stateId) {
		this.stateId = stateId;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(final String actionId) {
		this.actionId = actionId;
	}

	public final String getValidatorId() {
		return validatorId;
	}

	public void setValidatorId(final String validatorId) {
		this.validatorId = validatorId;
	}

	public String getTriggerMessage() {
		return triggerMessage;
	}

	public void setTriggerMessage(final String triggerMessage) {
		this.triggerMessage = triggerMessage;
	}

}
