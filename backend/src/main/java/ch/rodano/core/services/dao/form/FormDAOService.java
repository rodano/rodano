package ch.rodano.core.services.dao.form;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.function.Function;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.FormAuditTrail;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.form.Form;

public interface FormDAOService {

	/**
	 * Get the form associated with a given primary key
	 * @param pk
	 * @return The form associated with the primary key
	 */
	Form getFormByPk(Long pk);

	/**
	 * Search for forms matching the provided criteria
	 * @param scopePk The primary key of the scope
	 * @param eventPk The primary key of the event. When empty, look for forms that are directly attached to the scope.
	 * @param includeDeleted Whether to include deleted forms
	 * @param formModelIds The form model ids to filter by
	 * @return The list of forms matching the criteria
	 */
	List<Form> search(Optional<Long> scopePk, Optional<Long> eventPk, boolean includeDeleted, Optional<Collection<String>> formModelIds);

	/**
	 * Get the forms associated with a given scope pk
	 *
	 * @param scopePk The scope pk
	 * @return The forms associated with the scope pk
	 */
	List<Form> getFormsByScopePk(Long scopePk);

	/**
	 * Get the forms associated with a given scope pk and form model ids
	 *
	 * @param scopePk The scope pk
	 * @param formModelIds The form model ids
	 * @return The forms associated with the scope pk and form model ids
	 */
	List<Form> getFormsByScopePkAndFormModelIds(Long scopePk, Collection<String> formModelIds);

	/**
	 * Get all the forms (including deleted) associated with the given scope pk
	 *
	 * @param scopePk The scope pk
	 * @return The forms associated with the scope
	 */
	List<Form> getAllFormsByScopePk(Long scopePk);

	/**
	 * Get all the forms (including deleted) associated with the given scope pk and form model ids
	 * @param scopePk The scope pk
	 * @param formModelIds The form model ids
	 * @return The forms associated with the scope pk and form model ids
	 */
	List<Form> getAllFormsByScopePkAndFormModelIds(Long scopePk, Collection<String> formModelIds);

	/**
	 * Get the forms associated with a given event pk
	 *
	 * @param eventPk The event pk
	 * @return The forms associated with the event pk
	 */
	List<Form> getFormsByEventPk(Long eventPk);

	/**
	 * Get the forms associated with a given event pk and form model ids
	 *
	 * @param eventPk The event pk
	 * @param formModelIds The form model ids
	 * @return The forms associated with the event pk and form model ids
	 */
	List<Form> getFormsByEventPkAndFormModelIds(Long eventPk, Collection<String> formModelIds);

	/**
	 * Get all the forms (including deleted) associated with the given event pk
	 *
	 * @param eventPk The event pk
	 * @return The forms associated with the event pk
	 */
	List<Form> getAllFormsByEventPk(Long eventPk);

	/**
	 * Get all the forms (including deleted) associated with the given event pk and form model ids
	 * @param eventPk The pk of the event
	 * @param formModelIds The form model ids
	 * @return The forms associated with the event pk and form model ids
	 */
	List<Form> getAllFormsByEventPkAndFormModelIds(Long eventPk, Collection<String> formModelIds);

	/**
	 * Delete a form
	 *
	 * @param form    The form to delete
	 * @param context The context in which the action takes place
	 */
	void deleteForm(Form form, DatabaseActionContext context, String rationale);

	/**
	 * Restore a form
	 *
	 * @param form    The form to restore
	 * @param context The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void restoreForm(Form form, DatabaseActionContext context, String rationale);

	/**
	 * Create or update a form
	 *
	 * @param form    The form to create or update
	 * @param context The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void saveForm(Form form, DatabaseActionContext context, String rationale);

	NavigableSet<FormAuditTrail> getAuditTrails(Form form, Optional<Timeframe> timeframe, Optional<Long> actorPk);

	NavigableSet<FormAuditTrail> getAuditTrailsForProperty(Form form, Optional<Timeframe> timeframe, Function<FormAuditTrail, Object> property);

	NavigableSet<FormAuditTrail> getAuditTrailsForProperties(Form form, Optional<Timeframe> timeframe, List<Function<FormAuditTrail, Object>> properties);
}
