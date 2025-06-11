package ch.rodano.core.services.dao.field;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.function.Function;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.FieldAuditTrail;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.field.Field;

public interface FieldDAOService {

	Field getFieldByPk(Long pk);

	/**
	 * Get the fields associated with a given dataset
	 *
	 * @param datasetPk The dataset pk
	 * @param fieldModelIds The list of field model ids
	 * @return The list of fields associated with the dataset
	 */
	List<Field> getFieldsByDatasetPkHavingFieldModelIds(Long datasetPk, Collection<String> fieldModelIds);

	List<Field> getFieldsFromDatasetWithAValue(Long datasetPk);

	/**
	 * Creates/updates a field in the database
	 *
	 * @param field   The field to create/update
	 * @param context The context of the action
	 * @param rationale The rationale for the operation
	 */
	void saveField(Field field, DatabaseActionContext context, String rationale);

	List<Field> getFieldsByScopePk(Long scopePk);

	List<Field> getFieldsFromScopeWithAValue(Long scopePk);

	boolean doesScopeHaveFieldsWithAValue(Long scopePk);

	List<Field> getFieldsByEventPk(Long eventPk);

	List<Field> getFieldsFromEventWithAValue(Long eventPk);

	boolean doesEventHaveFieldsWithAValue(Long eventPk);

	List<Field> getFieldsRelatedToEvent(Long scopePk, Optional<Long> eventPk);

	NavigableSet<FieldAuditTrail> getAuditTrails(Field field, Optional<Timeframe> timeframe, Optional<Long> actorPk);

	NavigableSet<FieldAuditTrail> getAuditTrailsForProperty(Field field, Optional<Timeframe> timeframe, Function<FieldAuditTrail, Object> property);

	List<Field> getSearchableFields(Collection<Long> scopePks, Collection<String> searchableFieldModel);
}
