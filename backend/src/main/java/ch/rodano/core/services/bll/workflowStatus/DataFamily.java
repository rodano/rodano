package ch.rodano.core.services.bll.workflowStatus;

import java.util.Optional;

import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.DeletedObjectException;
import ch.rodano.core.model.exception.LockedObjectException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.Workflowable;

public record DataFamily(
	Scope scope,
	Optional<Event> event,
	Optional<Dataset> dataset,
	Optional<Field> field,
	Optional<Form> form
) {
	//scope
	public DataFamily(final Scope scope) {
		this(scope, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}

	//event
	public DataFamily(final Scope scope, final Event event) {
		this(scope, Optional.of(event), Optional.empty(), Optional.empty(), Optional.empty());
	}

	//field
	public DataFamily(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
		this(scope, event, Optional.of(dataset), Optional.of(field), Optional.empty());
	}

	public DataFamily(final Scope scope, final Event event, final Dataset dataset, final Field field) {
		this(scope, Optional.of(event), Optional.of(dataset), Optional.of(field), Optional.empty());
	}

	public DataFamily(final Scope scope, final Dataset dataset, final Field field) {
		this(scope, Optional.empty(), Optional.of(dataset), Optional.of(field), Optional.empty());
	}

	//form
	public DataFamily(final Scope scope, final Optional<Event> event, final Form form) {
		this(scope, event, Optional.empty(), Optional.empty(), Optional.of(form));
	}

	public DataFamily(final Scope scope, final Event event, final Form form) {
		this(scope, Optional.of(event), Optional.empty(), Optional.empty(), Optional.of(form));
	}

	public DataFamily(final Scope scope, final Form form) {
		this(scope, Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(form));
	}

	/**
	 *
	 * @return the "deepest" entity of this family, that means the most "advanced" entity in the chain scope/event/form or scope/event/dataset/field
	 */
	public Workflowable getDeepestEntity() {
		if(form.isPresent()) {
			return form.get();
		}
		if(dataset.isPresent() && field.isPresent()) {
			return field.get();
		}
		if(event.isPresent()) {
			return event.get();
		}
		return scope;
	}

	public void checkNotLocked() {
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}
		if(event.isPresent() && event.get().getLocked()) {
			throw new LockedObjectException(event.get());
		}
	}

	public void checkNotDeleted() {
		if(scope.getDeleted()) {
			throw new DeletedObjectException(scope);
		}
		if(event.isPresent() && event.get().getDeleted()) {
			throw new DeletedObjectException(event.get());
		}
		if(form.isPresent() && form.get().getDeleted()) {
			throw new DeletedObjectException(form.get());
		}
		if(dataset.isPresent() && dataset.get().getDeleted()) {
			throw new DeletedObjectException(dataset.get());
		}
	}

	public boolean isDeleted() {
		if(scope.getDeleted()) {
			return true;
		}
		if(event.isPresent() && event.get().getDeleted()) {
			return true;
		}
		if(form.isPresent() && form.get().getDeleted()) {
			return true;
		}
		if(dataset.isPresent() && dataset.get().getDeleted()) {
			return true;
		}
		return false;
	}

}
