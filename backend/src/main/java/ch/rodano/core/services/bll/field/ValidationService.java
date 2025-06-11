package ch.rodano.core.services.bll.field;

import java.util.Optional;

import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.plugin.validator.exception.ValidatorException;

public interface ValidationService {

	void applyNonBlockingValidators(Scope scope, Optional<Event> event, Dataset dataset, Field field) throws ValidatorException;

	void applyBlockingValidators(Scope scope, Optional<Event> event, Dataset dataset, Field field) throws ValidatorException;

	void checkValue(Scope scope, Optional<Event> event, Dataset dataset, Field field, Validator validator) throws ValidatorException;

	/**
	 * Validate a field
	 * @param scope   The scope that contains the field
	 * @param event   The optional event that contains the field
	 * @param dataset The dataset that contains the field
	 * @param field   The field to validate
	 * @param context The context of the update
	 * @param context The rationale for the validation
	 */
	void validateField(Scope scope, Optional<Event> event, Dataset dataset, Field field, DatabaseActionContext context, String rationale);
}
