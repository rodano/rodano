package ch.rodano.configuration.model.validator;

import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.utils.DisplayableUtils;

public class InvalidDate implements ValueError {

	public static final Map<String, String> IMPOSSIBLE_DATE = Map.of(
		LanguageStatic.en.getId(), "Invalid date.",
		LanguageStatic.fr.getId(), "Date invalide.");

	public static final Map<String, String> IN_THE_PAST = Map.of(
		LanguageStatic.en.getId(), "Date cannot be before %d.",
		LanguageStatic.fr.getId(), "La date ne doit pas être avant %d.");

	public static final Map<String, String> IN_THE_FUTURE = Map.of(
		LanguageStatic.en.getId(), "Date cannot be in the future.",
		LanguageStatic.fr.getId(), "La date ne doit pas être dans le futur.");

	public static final Map<String, String> MONTH_INCONSISTENT = Map.of(
		LanguageStatic.en.getId(), "You cannot set an unknown month if you know the day.",
		LanguageStatic.fr.getId(), "Le mois ne peut pas être inconnu si le jour ne l'est pas.");

	public static final Map<String, String> YEAR_INCONSISTENT = Map.of(
		LanguageStatic.en.getId(), "You cannot set an unknown year if you know the day or the month.",
		LanguageStatic.fr.getId(), "L'année ne peut pas être inconnue si le jour et le mois ne le sont pas.");

	public static final Map<String, String> INCOMPLETE_DATE = Map.of(
		LanguageStatic.en.getId(), "All date fields are required.",
		LanguageStatic.fr.getId(), "L'ensemble des champs date est requis.");

	public static final Map<String, String> INVALID_DATE_FORMAT = Map.of(
		LanguageStatic.en.getId(), "Invalid date format. Format is %s.",
		LanguageStatic.fr.getId(), "Format de date invalide. Le format est %s.");

	public static final Map<String, String> INVALID_TIME_FORMAT = Map.of(
		LanguageStatic.en.getId(), "Invalid time format. Format is %s.",
		LanguageStatic.fr.getId(), "Format de temps invalide. Le format est %s.");

	private final Map<String, String> messages;

	/**
	 * Constructor
	 *
	 * @param messages
	 * @param args
	 */
	protected InvalidDate(final Map<String, String> messages, final Object... args) {
		this.messages = new HashMap<>();
		if(messages != null) {
			messages.forEach((key, value) -> this.messages.put(key, String.format(value, args)));
		}
	}

	// ValueCheck factory methods

	public static ValueCheck impossibleDate() {
		return new ValueCheck(new InvalidDate(IMPOSSIBLE_DATE));
	}

	public static ValueCheck inThePast(final int year) {
		return new ValueCheck(new InvalidDate(IN_THE_PAST, year));
	}

	public static ValueCheck inTheFuture() {
		return new ValueCheck(new InvalidDate(IN_THE_FUTURE));
	}

	public static ValueCheck inconsitentMonth() {
		return new ValueCheck(new InvalidDate(MONTH_INCONSISTENT));
	}

	public static ValueCheck inconsitentYear() {
		return new ValueCheck(new InvalidDate(YEAR_INCONSISTENT));
	}

	public static ValueCheck incomplete() {
		return new ValueCheck(new InvalidDate(INCOMPLETE_DATE));
	}

	public static ValueCheck invalidDateFormat(final String format) {
		return new ValueCheck(new InvalidDate(INVALID_DATE_FORMAT, format));
	}

	public static ValueCheck invalidTimeFormat(final String format) {
		return new ValueCheck(new InvalidDate(INVALID_TIME_FORMAT, format));
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
