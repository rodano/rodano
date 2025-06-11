package ch.rodano.configuration.model.validator;

import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.utils.DisplayableUtils;

public class InvalidDecimal implements ValueError {

	public static final Map<String, String> NOT_A_DECIMAL = Map.of(
		LanguageStatic.en.getId(), "Invalid decimal number format. Decimal number must use dot separator.",
		LanguageStatic.fr.getId(), "Format de nombre invalide. Le nombre doit utiliser le point comme séparateur.");

	public static final Map<String, String> TOO_MANY_FRACTION_DIGITS = Map.of(
		LanguageStatic.en.getId(), "Invalid number format. Number must have at most %d fraction digit(s).",
		LanguageStatic.fr.getId(), "Format de nombre invalide. Le nombre doit avoir au plus %d décimale(s).");

	public static final Map<String, String> TOO_MANY_INTEGER_DIGITS = Map.of(
		LanguageStatic.en.getId(), "Invalid number format. Number must have at most %d integer digit(s).",
		LanguageStatic.fr.getId(), "Format de nombre invalide. Le nombre doit avoir au plus %d chiffre(s) dans sa patie entière.");

	private final Map<String, String> messages;

	/**
	 * Constructor
	 *
	 * @param messages
	 * @param args
	 */
	protected InvalidDecimal(final Map<String, String> messages, final Object... args) {
		this.messages = new HashMap<>();
		if(messages != null) {
			messages.forEach((key, value) -> this.messages.put(key, String.format(value, args)));
		}
	}

	public static ValueCheck wrongType() {
		return new ValueCheck(new InvalidDecimal(NOT_A_DECIMAL));
	}

	public static ValueCheck tooManyIntegerDigits(final String value, final int maxIntegerDigitsExpected) {
		return new ValueCheck(new InvalidDecimal(TOO_MANY_INTEGER_DIGITS, maxIntegerDigitsExpected), value);
	}

	public static ValueCheck tooManyFractionDigits(final String value, final int maxFractionDigitsExpected) {
		return new ValueCheck(new InvalidDecimal(TOO_MANY_FRACTION_DIGITS, maxFractionDigitsExpected), value);
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
