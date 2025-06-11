package ch.rodano.core.model.event;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.event.EventModel;
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
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.Workflowable;

public class Event extends EventRecord implements DeletableObject, TimestampableObject, PersistentObject, AuditableObject, Workflowable, Comparable<Event>, Evaluable {

	// Used in study specific code
	public static final Comparator<Event> COMPARATOR_CONFIG = (v1, v2) -> {
		if(v1 == v2) {
			return 0;
		}
		//cannot compare visits that don't belong to the same scope
		if(!v1.scopeFk.equals(v2.scopeFk)) {
			throw new RuntimeException("Cannot compare two visits in different scopes");
		}

		return Comparator
			.comparing(Event::getEventModel)
			.thenComparing(Event::getEventGroupNumber)
			.thenComparing(Event::getCreationTime)
			.thenComparing(Event::getPk)
			.compare(v1, v2);
	};

	public static final Comparator<Event> DEFAULT_COMPARATOR = Comparator
		.comparing(Event::getDateOrExpectedDate)
		.thenComparing(COMPARATOR_CONFIG);

	private Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	private ScopeModel scopeModel;
	private EventModel eventModel;

	public Event() {
		super();
	}

	public void setScopeModel(final ScopeModel scopeModel) {
		this.scopeModel = scopeModel;
		this.scopeModelId = scopeModel.getId();
	}

	public void setEventModel(final EventModel eventModel) {
		this.eventModel = eventModel;
		this.eventModelId = eventModel.getId();
	}

	public ScopeModel getScopeModel() {
		return scopeModel;
	}

	public EventModel getEventModel() {
		return eventModel;
	}

	@Override
	public WorkflowableEntity getWorkflowableEntity() {
		return WorkflowableEntity.EVENT;
	}

	public ZonedDateTime getDateOrExpectedDate() {
		return date != null ? date : expectedDate;
	}

	public ZonedDateTime getScheduleBeginning() {
		final var event = getEventModel();
		return expectedDate.minus(event.getInterval(), event.getIntervalUnit());
	}

	public ZonedDateTime getScheduleEnd() {
		final var event = getEventModel();
		return expectedDate.plus(event.getInterval(), event.getIntervalUnit());
	}

	public boolean isInSchedule() {
		if(!getEventModel().hasInterval()) {
			throw new NoRespectForConfigurationException("Unable to check event schedule if its configuration event does not specify an interval");
		}
		final var scheduleBeginning = getScheduleBeginning();
		final var scheduleEnd = getScheduleEnd();

		return (date.isAfter(scheduleBeginning) || date.isEqual(scheduleBeginning)) && (date.isBefore(scheduleEnd) || date.isEqual(scheduleEnd));
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
	public final WorkflowableModel getWorkflowableModel() {
		return getEventModel();
	}

	public final void setScope(final Scope scope) {
		scopeFk = scope.getPk();
	}

	public Boolean isExpected() {
		return expectedDate != null && date == null;
	}

	@Override
	@JsonIgnore
	public RulableEntity getRulableEntity() {
		return RulableEntity.EVENT;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(final Object o) {
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		final var event = (Event) o;
		if(id == null && event.id == null) {
			return this == o;
		}
		return Objects.equals(id, event.id);
	}

	@Override
	public final int compareTo(final Event otherEvent) {
		//preserve consistency between equals and comparator
		if(equals(otherEvent)) {
			return 0;
		}
		//can not compare visits that don't belong to the same scope
		if(!this.scopeFk.equals(otherEvent.scopeFk)) {
			throw new RuntimeException("Can not compare two visits in differents scopes");
		}
		return DEFAULT_COMPARATOR.compare(this, otherEvent);
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder("Event{");
		sb.append(super.toString());
		sb.append(", eventModelId='").append(eventModelId).append('\'');
		sb.append(", eventGroupNumber=").append(eventGroupNumber);
		sb.append('}');
		return sb.toString();
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
		scopeModel = study.getScopeModel(scopeModelId);
		eventModel = scopeModel.getEventModel(eventModelId);
	}

}
