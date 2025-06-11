package ch.rodano.core.services.dao.form;

import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.function.Function;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.FormAuditTrail;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.form.Form;

public interface FormDAOService {

	Form getFormByPk(Long pk);

	List<Form> getFormsByScopePkIncludingRemoved(Long scopePk);

	List<Form> getFormsByScopePk(Long scopePk);

	List<Form> getFormsByEventPkIncludingRemoved(Long eventPk);

	List<Form> getFormsByEventPk(Long eventPk);

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

	Form getFormByScopePkAndFormModelId(Long scopePk, String formId);

	Form getFormByEventPkAndFormModelId(Long eventPk, String formId);

	NavigableSet<FormAuditTrail> getAuditTrails(Form form, Optional<Timeframe> timeframe, Optional<Long> actorPk);

	NavigableSet<FormAuditTrail> getAuditTrailsForProperty(Form form, Optional<Timeframe> timeframe, Function<FormAuditTrail, Object> property);

	NavigableSet<FormAuditTrail> getAuditTrailsForProperties(Form form, Optional<Timeframe> timeframe, List<Function<FormAuditTrail, Object>> properties);
}
