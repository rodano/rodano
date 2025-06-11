package ch.rodano.core.services.bll.field;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.PossibleValue;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;

public interface FieldService {

	/**
	 * Create a field.
	 * @param dataset       The dataset
	 * @param fieldModel    Field model
	 * @param context       Action context
	 * @param rationale     The rationale for the operation
	 * @return              New field
	 */
	Field create(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		FieldModel fieldModel,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Create all fields in the dataset.
	 * @param dataset       The dataset
	 * @param context       Action context
	 * @param rationale     The rationale for the operation
	 * @return              New fields
	 */
	List<Field> createAll(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Get the possible values of the field as defined in the field model, or use the possible value provider, if defined.
	 * @return A list of possible values for the field
	 */
	List<PossibleValue> getPossibleValues(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		Field field
	);

	/**
	 * Format the string value of a field to a readable label.
	 * @return A readable label of the field value
	 */
	String valueToLabel(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		Field field,
		String value,
		String... languages
	);

	/**
	 * Get the latest value of the field at the provided point in time. If the date is not provided,
	 * the latest value of the field is returned.
	 * @return The latest value of the field.
	 */
	String getLatestValue(Field field, Optional<ZonedDateTime> date);

	/**
	 * Create a readable label from the field's value.
	 * @return A readable label
	 */
	String getValueLabel(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		Field field,
		Optional<ZonedDateTime> date,
		String... languages
	);

	Object getValueObject(Field field);

	Object getValueObject(Field field, Optional<ZonedDateTime> date);

	String getDefaultValue(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		Field field
	);

	String getPluginValue(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		Field field
	);

	String getInterpretedValue(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		Field field,
		Optional<ZonedDateTime> date
	);

	/**
	 * Update a field value and save it
	 *
	 * @param field   The field
	 * @param value   The new value
	 * @param context The context of the update
	 */
	void updateValue(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		Field field,
		String value,
		DatabaseActionContext context,
		String rationale
	) throws InvalidValueException, BadlyFormattedValue;

	void reset(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		Field field,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Get fields on scope and event, if provided.
	 * @return Fields contained in scope and event datasets.
	 */
	List<Field> getAll(Scope scope, Optional<Event> event);

	List<Field> getAll(Dataset dataset);

	List<Field> getAll(Dataset dataset, Collection<FieldModel> fieldModels);

	Field get(Dataset dataset, FieldModel fieldModel);

	Optional<Field> get(WorkflowStatus workflowStatus);

}
