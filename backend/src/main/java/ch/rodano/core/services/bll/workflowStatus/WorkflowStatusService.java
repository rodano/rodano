package ch.rodano.core.services.bll.workflowStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.workflow.WorkflowStatusSearch;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.configuration.model.workflow.Action;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.model.workflow.Workflowable;

public interface WorkflowStatusService {

	//// Workflow status functions

	void save(
		DataFamily family,
		WorkflowStatus workflowStatus,
		DatabaseActionContext context,
		String rationale
	);

	void updateState(
		DataFamily family,
		WorkflowStatus workflowStatus,
		WorkflowState state,
		Map<String, Object> data,
		DatabaseActionContext context,
		String rationale
	);

	void delete(
		DataFamily family,
		WorkflowStatus workflowStatus,
		DatabaseActionContext context,
		String rationale
	);

	PagedResult<WorkflowStatus> search(WorkflowStatusSearch search);

	/**
	 * Retrieve an optional profile of the user who created the workflow status
	 * If the workflow status has been created by the system, the optional will be empty
	 * this is wrong because we retrieve the current profiles of the creator
	 * these profiles may have been different at the time the workflow has been created
	 * even worst, we only return one profile but we don't know with "which" profile the workflow has been created
	 * 	TODO remove this when advanced workflow matrix has been deleted
	 * @return an optional list of profiles of the user who created the workflow status
	 */
	Optional<Profile> getCreatorProfile(WorkflowStatus workflowStatus);

	//// Workflowable functions

	/**
	 * Initialize a workflow on a workflowable.
	 *
	 * @return The workflow status created
	 */
	WorkflowStatus create(
		DataFamily family,
		Workflowable workflowable,
		Workflow workflow,
		Optional<WorkflowState> state,
		Optional<Action> action,
		Optional<Validator> validator,
		Optional<Profile> profile,
		Map<String, Object> data,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Initialize a workflow on a workflowable with default state and no validator and no data
	 * Mainly used to create a workflow initialized by a user.
	 *
	 * @return The workflow status created
	 */
	WorkflowStatus create(
		final DataFamily family,
		Workflowable workflowable,
		Workflow workflow,
		Action action,
		Profile profile,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Initialize a workflow on a workflowable with default state, no action and no validator and no profile.
	 * Mainly used to create a system workflow.
	 *
	 * @return The workflow status created
	 */
	WorkflowStatus create(
		DataFamily family,
		Workflowable workflowable,
		Workflow workflow,
		Map<String, Object> data,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Initialize a workflow on a workflowable with no action, no validator and no profile.
	 * Mainly used to create a system workflow and set it to a specific state.
	 *
	 * @return The workflow status created
	 */
	WorkflowStatus create(
		final DataFamily family,
		Workflowable workflowable,
		Workflow workflow,
		WorkflowState state,
		Map<String, Object> data,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Initialize all workflows configured on a workflowable.
	 * @param workflowable  The workflowable
	 * @return              All the workflow statuses created
	 */
	List<WorkflowStatus> createAll(
		DataFamily family,
		Workflowable workflowable,
		Map<String, Object> data,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Reset all mandatory workflows on a workflowable and delete the rest of the workflows.
	 */
	void resetMandatoryAndDeleteTheRest(
		DataFamily family,
		Workflowable workflowable,
		DatabaseActionContext context,
		String rationale
	);

	Workflowable getWorkflowable(WorkflowStatus workflowStatus);

	/**
	 * Get all workflow statuses on a workflowable.
	 * @return  All workflow statuses attached to the workflowable
	 */
	List<WorkflowStatus> getAll(Workflowable workflowable);

	/**
	 * Get all workflow statuses with the provided workflow on a workflowable.
	 * @return  All workflow statuses attached to the workflowable
	 */
	List<WorkflowStatus> getAll(Workflowable workflowable, Workflow workflow);

	/**
	 * Get all workflow statuses with the provided workflow state on a workflowable.
	 * @return  All workflow statuses attached to the workflowable
	 */
	List<WorkflowStatus> getAll(Workflowable workflowable, WorkflowState workflowState);

	/**
	 * Get the current workflow status with the provided workflow, if any.
	 * @return  The most recently modified workflow status
	 */
	Optional<WorkflowStatus> getMostRecent(Workflowable workflowable, Workflow workflow);

	/**
	 * Get the current workflow status with the provided workflow state, if any.
	 * @return  The most recently modified workflow status
	 */
	Optional<WorkflowStatus> getMostRecent(Workflowable workflowable, WorkflowState workflowState);

	/**
	 * Get workflow statuses linked to a given form (workflow statuses on the form itself or on its descendant entities).
	 * @param scope The scope containing the form
	 * @param event The optional event containing the form (empty if the form is directly attached to a scope)
	 * @param form The form
	 * @return A list of workflow statuses
	 */
	List<WorkflowStatus> getContained(
		Scope scope,
		Optional<Event> event,
		Form form
	);

	/**
	 * Get workflow statuses linked to the provided forms (workflow statuses on the forms themselves or on their descendant entities).
	 * @param scope The scope containing the forms
	 * @param event The optional event containing the form (empty if the form is directly attached to a scope)
	 * @param forms The forms
	 * @return A list of workflow statuses
	 */
	Map<Form, List<WorkflowStatus>> getContained(
		Scope scope,
		Optional<Event> event,
		List<Form> forms
	);

	DataFamily createDataFamily(WorkflowStatus workflowStatus);

}
