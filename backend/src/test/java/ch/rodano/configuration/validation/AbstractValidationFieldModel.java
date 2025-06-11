package ch.rodano.configuration.validation;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.validator.ValueCheck;

public abstract class AbstractValidationFieldModel {
	public static final String LANGUAGE = LanguageStatic.en.name();

	protected ValueCheck value;
	protected FieldModel fieldModel;

}
