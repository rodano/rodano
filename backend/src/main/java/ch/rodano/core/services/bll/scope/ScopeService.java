package ch.rodano.core.services.bll.scope;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.model.scope.exceptions.ScopeCodeAlreadyUsedException;
import ch.rodano.core.model.workflow.WorkflowStatus;

public interface ScopeService {

	/**
	 * Create a candidate scope, which serves as a pre-filled shell for a new scope.
	 *
	 * @param model     The scope model
	 * @param startDate The start date of the scope
	 * @param parent    The parent of the scope
	 * @return A newly created candidate scope
	 */
	Scope createCandidate(ScopeModel model, ZonedDateTime startDate, Scope parent);

	/**
	 * Create a new scope using the data from the candidate scope. All the fields that are
	 * not empty in the candidate scope will be used in the new scope
	 * <p>
	 * Note that if the code used in the candidate scope is already used by an existing scope,
	 * this method will throw a ScopeCodeAlreadyUsedException
	 *
	 * @param candidateScope The candidate scope
	 * @param model          The model for the new scope
	 * @param parent         The parent of the new scope
	 * @param context        The context
	 * @param rationale      The rationale for the operation
	 * @return The created scope
	 */
	Scope createFromCandidate(
		Scope candidateScope,
		ScopeModel model,
		Scope parent,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Generates the next possible code for a scope based on the scope model and the parent scope.
	 *
	 * @param model  The scope model
	 * @param parent The parent scope
	 * @return A code
	 */
	String getNextCode(ScopeModel model, Scope parent);

	/**
	 * Inserts the specified scope into the database and creates a relation with the specified parent
	 *
	 * @param scope     The scope to add
	 * @param parent    The parent scope
	 * @param context   Context of the action
	 * @param rationale The rationale for the operation
	 */
	void create(
		Scope scope,
		Scope parent,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Restore a scope from its deleted state
	 *
	 * @param scope     The scope
	 * @param context   Context of the action
	 * @param rationale The rationale for the operation
	 */
	void restore(
		Scope scope,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Delete a scope
	 *
	 * @param scope   The scope to delete
	 * @param context   Context of the action
	 * @param rationale The rationale for the operation
	 */
	void delete(
		Scope scope,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Save a scope
	 *
	 * @param scope   The scope
	 * @param context   Context of the action
	 * @param rationale The rationale for the operation
	 * @throws ScopeCodeAlreadyUsedException Thrown if the scope code is already used
	 */
	void save(
		Scope scope,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Lock the scope and all its events and descendants scopes.
	 * @param scope     Scope to lock
	 * @param context   Action context
	 */
	void lock(Scope scope, DatabaseActionContext context);

	/**
	 * Unlock the scope and all its events and descendants scopes.
	 * @param scope     Scope to unlock
	 * @param context   Action context
	 */
	void unlock(Scope scope, DatabaseActionContext context);

	/**
	 * Validate all fields on scope
	 * @param scope         The scope
	 * @param context       The action context
	 * @param rationale     The rationale for the operation
	 */
	void validateFieldsOnScope(
		Scope scope,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Validate datasets on scope and all its events
	 * @param scope         The scope
	 * @param context       The action context
	 * @param rationale     The rationale for the operation
	 */
	void validateFieldsOnScopeAndEvents(
		Scope scope,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Check if a scope is a root scope
	 *
	 * @param scope The scope
	 * @return True if the scope is a root scope and false otherwise
	 */
	boolean isRootScope(Scope scope);

	/**
	 * Get the root scope
	 *
	 * @return The root scope
	 */
	Scope getRootScope();

	/**
	 * Get scopes with the specified scope model.
	 * @param scopeModel    Scope model
	 * @return              A list of scopes
	 */
	List<Scope> getAll(ScopeModel scopeModel);

	/**
	 * Get scopes with the specified scope models and ancestors
	 * @param scopeModels   Scope models
	 * @param ancestors     Scope ancestors
	 * @return              A list of scopes
	 */
	List<Scope> getAll(Collection<ScopeModel> scopeModels, Collection<Scope> ancestors);

	Scope get(Event event);

	Scope get(Form form);

	Scope get(Dataset dataset);

	Scope get(Field field);

	Scope get(WorkflowStatus workflowStatus);

	Scope get(Resource resource);

	Scope get(Role role);

	/**
	 * Get all scopes (including removed ones).
	 *
	 * @return All scopes
	 */
	List<Scope> getAllIncludingRemoved();

	/**
	 * Get all scopes (including removed ones) for a scope model.
	 *
	 * @param scopeModel The scope model
	 * @return The scopes matching the scope model
	 */
	List<Scope> getAllIncludingRemoved(ScopeModel scopeModel);

	/**
	 * Search for scopes
	 * @param search The search predicate
	 * @return          Found scopes
	 */
	PagedResult<Scope> search(ScopeSearch search);

	/**
	 * Checks if at least one of the default ancestors of a scope is deleted
	 *
	 * @param scope The scope
	 * @return True if at least one of the default ancestors of a scope is deleted
	 */
	boolean isDeletedOrInDeletedScope(Scope scope);

	/**
	 * Get number of enabled leaves
	 *
	 * @param scope The scope
	 * @return number of leaves
	 */
	Integer getLeafCount(Scope scope);

	/**
	 * Get number of enabled leaves for a list of scopes
	 *
	 * @param scopes The scopes
	 * @return number of leaves
	 */
	Map<Scope, Integer> getLeafCount(Collection<Scope> scopes);

	/**
	 * Get the relation timeframe for two different scopes
	 *
	 * @param ancestor   The ancestor
	 * @param descendant The descendant
	 * @return An optional relation timeframe between the two scopes, empty if the two scopes are not linked
	 */
	Optional<Timeframe> getRelationTimeframe(Scope ancestor, Scope descendant);

	/**
	 * Move back the start date of the oldest physical relation of the scope to the specified date if needed
	 *
	 * @param scope The scope
	 * @param date  The date
	 * @param context   Context of the action
	 * @param rationale The rationale for the operation
	 */
	void moveBackPhysicalDate(Scope scope, ZonedDateTime date, DatabaseActionContext context, String rationale);

	/**
	 * Get the default start date
	 *
	 * @param scope The scope
	 * @return A date
	 */
	ZonedDateTime getDefaultStartDate(Scope scope);

	/**
	 * Get the default stop date
	 *
	 * @param scope The scope
	 * @return A date
	 */
	ZonedDateTime getDefaultStopDate(Scope scope);

	/**
	 * Get the depth of the scope in the scope tree
	 *
	 * @param scope The scope
	 * @return The depth
	 */
	int getTreeDepth(Scope scope);
}
