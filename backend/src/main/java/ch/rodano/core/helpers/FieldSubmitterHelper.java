package ch.rodano.core.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.field.ValidationService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;

//TODO rely on methods from pages resource
public class FieldSubmitterHelper {
	private final DatabaseActionContext context;
	private final Scope scope;
	private final Optional<Event> event;
	private final FieldService fieldService;
	private final ValidationService validationService;

	private final Map<Dataset, Map<Field, String>> inputs = new HashMap<>();

	public FieldSubmitterHelper(final DatabaseActionContext context, final Scope scope, final Optional<Event> event, final FieldService fieldService, final ValidationService validationService) {
		this.context = context;
		this.scope = scope;
		this.event = event;
		this.fieldService = fieldService;
		this.validationService = validationService;
	}

	public FieldSubmitterHelper updateField(final Dataset dataset, final String fieldModelId, final String value) {
		if(!inputs.containsKey(dataset)) {
			inputs.put(dataset, new HashMap<>());
		}
		final var fieldModel = dataset.getDatasetModel().getFieldModel(fieldModelId);
		final var field = fieldService.get(dataset, fieldModel);
		inputs.get(dataset).put(field, value);

		return this;
	}

	public FieldSubmitterHelper updateFields(final Dataset dataset, final Map<String, String> values) {
		values.forEach((key, value) -> updateField(dataset, key, value));
		return this;
	}

	public void submit(final String rationale) throws InvalidValueException, BadlyFormattedValue {
		for(final var entry : inputs.entrySet()) {
			for(final Entry<Field, String> fieldEntry : entry.getValue().entrySet()) {
				fieldService.updateValue(scope, event, entry.getKey(), fieldEntry.getKey(), fieldEntry.getValue(), context, rationale);
			}
		}

		for(final var entry : inputs.entrySet()) {
			for(final Entry<Field, String> fieldEntry : entry.getValue().entrySet()) {
				validationService.validateField(scope, event, entry.getKey(), fieldEntry.getKey(), context.toSystemAction(), rationale);
			}
		}
	}
}
