package ch.rodano.core.model.workflow;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.configuration.model.workflow.Action;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.PersistentObject;
import ch.rodano.core.model.common.TimestampableObject;
import ch.rodano.core.model.rules.Evaluable;

public class WorkflowStatus extends WorkflowStatusRecord implements DeletableObject, TimestampableObject, AuditableObject, PersistentObject, Evaluable, Comparable<WorkflowStatus> {

	public static final Comparator<WorkflowStatus> DEFAULT_COMPARATOR = Comparator
		.comparing(WorkflowStatus::getState, WorkflowState.COMPARATOR_IMPORTANCE)
		.thenComparing(WorkflowStatus::getPk);

	public static final Comparator<WorkflowStatus> MOST_RECENT_COMPARATOR = Comparator
		.comparing(WorkflowStatus::getLastUpdateTime)
		.thenComparing(WorkflowStatus::getPk);

	public static Comparator<WorkflowStatus> proxyComparator(final Comparator<Workflow> comparator) {
		return Comparator
			.comparing(WorkflowStatus::getWorkflow, comparator)
			.thenComparing(WorkflowStatus::getPk);
	}

	private Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;
	private Workflow workflow;
	private WorkflowState state;
	private Action action;
	private Validator validator;
	private Profile profile;

	public WorkflowStatus() {
		super();
	}

	@Override
	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	@Override
	public Long getPk() {
		return pk;
	}

	@Override
	public void setPk(final Long pk) {
		this.pk = pk;
	}

	@Override
	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	@Override
	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public void setWorkflow(final Workflow workflow) {
		this.workflow = workflow;
		this.workflowId = workflow.getId();
	}

	public void setState(final WorkflowState state) {
		this.state = state;
		this.stateId = state.getId();
	}

	public void setAction(final Action action) {
		this.action = action;
		this.actionId = action.getId();
	}

	public void setValidator(final Validator validator) {
		this.validator = validator;
		this.validatorId = validator.getId();
	}

	public void setProfile(final Profile profile) {
		this.profile = profile;
		this.profileId = profile.getId();
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public WorkflowState getState() {
		return state;
	}

	public Action getAction() {
		return action;
	}

	public Validator getValidator() {
		return validator;
	}

	public Profile getProfile() {
		return profile;
	}

	public WorkflowableEntity getWorkflowableType() {
		if(fieldFk != null) {
			return WorkflowableEntity.FIELD;
		}
		if(formFk != null) {
			return WorkflowableEntity.FORM;
		}
		if(eventFk != null) {
			return WorkflowableEntity.EVENT;
		}
		return WorkflowableEntity.SCOPE;
	}

	@Override
	public String getId() {
		return getWorkflow().getId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(pk);
	}

	@Override
	public boolean equals(final Object o) {
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		final var workflowStatus = (WorkflowStatus) o;
		if(pk == null && workflowStatus.pk == null) {
			return this == o;
		}
		return Objects.equals(pk, workflowStatus.pk);
	}

	@Override
	public int compareTo(final WorkflowStatus otherWorkflowStatus) {
		//preserve consistency between equals and comparator
		if(this == otherWorkflowStatus) {
			return 0;
		}
		return DEFAULT_COMPARATOR.compare(this, otherWorkflowStatus);
	}

	@Override
	@JsonIgnore
	public RulableEntity getRulableEntity() {
		return RulableEntity.WORKFLOW;
	}

	@Override
	public void onPreUpdate() {
		//nothing
	}

	@Override
	public void onPostUpdate(final Study study) {
		//nothing
	}

	@Override
	public void onPostLoad(final Study study) {
		workflow = study.getWorkflow(workflowId);
		state = workflow.getState(stateId);
		action = StringUtils.isNotBlank(actionId) ? workflow.getAction(actionId) : null;
		validator = StringUtils.isNotBlank(validatorId) ? study.getValidator(validatorId) : null;
		profile = StringUtils.isNotBlank(profileId) ? study.getProfile(profileId) : null;
	}
}
