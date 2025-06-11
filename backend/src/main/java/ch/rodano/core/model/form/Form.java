package ch.rodano.core.model.form;

import java.time.ZonedDateTime;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.configuration.model.workflow.WorkflowableModel;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.PersistentObject;
import ch.rodano.core.model.common.TimestampableObject;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.workflow.Workflowable;

public class Form extends FormRecord implements DeletableObject, TimestampableObject, PersistentObject, AuditableObject, Workflowable, Comparable<Form>, Evaluable {

	private Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	private FormModel formModel;

	public Form() {
		super();
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
	public ZonedDateTime getCreationTime() {
		return creationTime;
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

	@Override
	public String getId() {
		return formModelId;
	}

	public void setFormModel(final FormModel formModel) {
		this.formModel = formModel;
		this.formModelId = formModel.getId();
	}

	public FormModel getFormModel() {
		return formModel;
	}

	@Override
	public int compareTo(final Form otherForm) {
		if(this == otherForm) {
			return 0;
		}
		if(scopeFk != null && !scopeFk.equals(otherForm.scopeFk) || eventFk != null && !eventFk.equals(otherForm.eventFk)) {
			throw new RuntimeException("Cannot compare two forms in different scopes or visits");
		}

		return Comparator
			.comparing(Form::getId)
			.thenComparing(Form::getCreationTime)
			.thenComparing(Form::getPk)
			.compare(this, otherForm);
	}

	@Override
	public WorkflowableModel getWorkflowableModel() {
		return getFormModel();
	}

	@Override
	@JsonIgnore
	public RulableEntity getRulableEntity() {
		return RulableEntity.FORM;
	}

	@Override
	public WorkflowableEntity getWorkflowableEntity() {
		return WorkflowableEntity.FORM;
	}

	public static Comparator<Form> getScopeModelComparator(final ScopeModel scopeModel) {
		return (f1, f2) -> {
			if(f1 == f2) {
				return 0;
			}

			//cannot compare forms that don't belong to the same scope
			if(f1.scopeFk == null || f2.scopeFk == null || !f1.scopeFk.equals(f2.scopeFk)) {
				throw new RuntimeException("Cannot compare two forms in different scopes");
			}

			final var formModelIds = scopeModel.getFormModelIds();
			return formModelIds.indexOf(f1.formModelId) - formModelIds.indexOf(f2.formModelId);
		};
	}

	public static Comparator<Form> getEventModelComparator(final EventModel eventModel) {
		return (f1, f2) -> {
			if(f1 == f2) {
				return 0;
			}

			//cannot compare forms that don't belong to the same event
			if(f1.eventFk == null || f2.eventFk == null || !f1.eventFk.equals(f2.eventFk)) {
				throw new RuntimeException("Cannot compare two forms in different visits");
			}

			final var formModelIds = eventModel.getFormModelIds();
			return formModelIds.indexOf(f1.formModelId) - formModelIds.indexOf(f2.formModelId);
		};
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
		formModel = study.getFormModel(formModelId);
	}
}
