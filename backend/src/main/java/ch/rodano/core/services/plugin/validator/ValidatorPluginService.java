package ch.rodano.core.services.plugin.validator;

import java.util.Optional;

import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.plugin.PluginService;

public interface ValidatorPluginService extends PluginService {
	/**
	 * Validate a field
	 *
	 * @param validator The validator to use
	 * @param scope The scope
	 * @param event The optional event
	 * @param dataset The dataset
	 * @param field The field to validate
	 * @return True of the field is valid and false otherwise
	 */
	Boolean validate(Validator validator, Scope scope, Optional<Event> event, Dataset dataset, Field field);
}
