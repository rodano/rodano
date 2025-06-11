package ch.rodano.configuration.model.validator;

import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.utils.DisplayableUtils;

public class InvalidOtherPossibleValue implements ValueError {

	public static final Map<String, String> SPECIFY_VALUE_NOT_BLANK = Map.of(
		LanguageStatic.en.getId(), "%s is required.",
		LanguageStatic.fr.getId(), "%s est requis.");


	private final Map<String, String> messages;

	protected InvalidOtherPossibleValue(final Map<String, String> messages, final Object... args) {
		this.messages = new HashMap<>();
		if(messages != null) {
			messages.forEach((key, value) -> this.messages.put(key, String.format(value, args)));
		}
	}

	// ValueCheck factory methods
	public static ValueCheck specifyCanNotBeBlank(final String value) {
		return new ValueCheck(new InvalidOtherPossibleValue(SPECIFY_VALUE_NOT_BLANK, value));
	}

	@Override
	public String getLocalizedMessage(final String... languages) {
		return DisplayableUtils.getLocalizedMap(messages, languages);
	}

	@Override
	public String getDefaultLocalizedMessage() {
		return DisplayableUtils.getLocalizedMap(messages, LanguageStatic.en.getId());
	}
}
