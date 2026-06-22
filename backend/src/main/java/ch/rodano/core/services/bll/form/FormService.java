package ch.rodano.core.services.bll.form;

import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.utils.ACL;

public interface FormService {

	/**
	 * Create all forms for a scope.
	 *
	 * @param scope The scope
	 * @return All created forms
	 */
	List<Form> createAll(
		Scope scope,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Create all forms for an event.
	 *
	 * @param scope The scope
	 * @param event The event
	 * @param context the context
	 * @return All created forms
	 */
	List<Form> createAll(
		Scope scope,
		Event event,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Create a form for a scope.
	 *
	 * @param scope     The scope
	 * @param formModel  The form model
	 * @return The created form
	 */
	Form create(
		Scope scope,
		FormModel formModel,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Create a form for an event.
	 *
	 * @param event The event
	 * @param formModel  The form model
	 * @return The created form
	 */
	Form create(
		Scope scope,
		Event event,
		FormModel formModel,
		DatabaseActionContext context,
		String rationale
	);

	void save(
		Scope scope,
		Optional<Event> event,
		Form form,
		DatabaseActionContext context,
		String rationale
	);

	void delete(
		Scope scope,
		Optional<Event> event,
		Form form,
		DatabaseActionContext context,
		String rationale
	);

	void restore(
		Scope scope,
		Optional<Event> event,
		Form form,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Search for forms for the given scope and event and the given ACL.
	 *
	 * @param scope The scope
	 * @param event The optional event. When empty, look for forms that are directly attached to the scope.
	 * @param acl The ACL
	 * @return Forms associated with the scope or one of its events, filtered by the ACL
	 */
	List<Form> search(Scope scope, Optional<Event> event, ACL acl);

	List<Form> getAllIncludingRemoved(Scope scope);

	List<Form> getAll(Scope scope);

	Form get(Scope scope, FormModel formModel);

	List<Form> getAllIncludingRemoved(Event event);

	List<Form> getAll(Event event);

	Form get(Event event, FormModel formModel);

	Optional<Form> get(WorkflowStatus workflowStatus);
}
