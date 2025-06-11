package ch.rodano.configuration.model.validator;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.predicate.ValueSourceCriteria;

public class DateValidator implements ValueFormatValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ValueSourceCriteria.class);

	public static final PartialDate DATE_1900 = PartialDate.of(1900);

	private final FieldModel fieldModel;

	public DateValidator(final FieldModel fieldModel) {
		this.fieldModel = fieldModel;
	}

	/**
	 * Trims and sanitizes the provided string value, based on the field model type.
	 *
	 * @param value to sanitize
	 * @return sanitized value
	 */
	// TODO move method in the right class: this can be used outside for other field model types as well
	public String sanitizeValue(final String value) {
		if(StringUtils.isBlank(value)) {
			return "";
		}
		return value.trim();
	}

	/**
	 * This method must receive a sanitized trimmed string value.
	 * It returns either an object containing the sanitized value, or a format error.
	 *
	 * @param value The sanitized value
	 * @return ValueCheck
	 */
	@Override
	public ValueCheck validate(final String value) {
		ValueCheck valueCheck = new ValueCheck(value);

		if(FieldModelType.DATE_SELECT.equals(fieldModel.getType())) {
			//date select must be well formed
			valueCheck = validateDateSelect(value);
		}
		else if(FieldModelType.DATE.equals(fieldModel.getType())) {
			//do some check for date or time
			if(fieldModel.isTime() && !fieldModel.isDate()) {
				valueCheck = validateTime(value);
			}
			else if(fieldModel.isDate()) {
				valueCheck = validateDate(value);
			}
		}

		if(valueCheck.hasError()) {
			return valueCheck;
		}

		//keep an handle on partial date to do more tests
		final var date = PartialDate.of(valueCheck.getSanitizedValue());

		if(fieldModel.isDate()) {
			//check date is not before the start date or 1900
			final var startYear = fieldModel.getMinYear();
			final var lowerDateLimit = startYear != null && startYear != 0 ? PartialDate.of(startYear) : DATE_1900;
			if(date.before(lowerDateLimit)) {
				return InvalidDate.inThePast(lowerDateLimit.getYear().get());
			}

			//check date is not in future
			final var tomorrow = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1).minusSeconds(1);
			if(!fieldModel.isAllowDateInFuture() && date.after(PartialDate.of(tomorrow))) {
				return InvalidDate.inTheFuture();
			}
		}

		return new ValueCheck(value);
	}

	/**
	 * Validate the time
	 *
	 * @param value The value
	 * @return A value check
	 */
	private ValueCheck validateTime(final String value) {
		try {
			LocalTime.parse(value, fieldModel.getDateTimeFormatter());
		}
		catch(final DateTimeParseException e) {
			return InvalidDate.invalidTimeFormat(fieldModel.getInlineHelpOrFormat());
		}
		return new ValueCheck(value);
	}

	/**
	 * Validate the date
	 *
	 * @param value The value
	 * @return A value check
	 */
	private ValueCheck validateDate(final String value) {
		final var formatter = fieldModel.getDateTimeFormatter();
		try {
			//try to parse date using formatter
			final var date = ZonedDateTime.parse(value, formatter);
			//format date the other way to see if the date has not be altered
			if(!formatter.format(date).equals(value)) {
				return InvalidDate.invalidDateFormat(fieldModel.getInlineHelpOrFormat());
			}
			return new ValueCheck(value);
		}
		catch(final DateTimeException e) {
			LOGGER.error("Unable to parse {} with formatter {} due to: {}", value, fieldModel.getInlineHelpOrFormat(), e.getLocalizedMessage());
			return InvalidDate.invalidDateFormat(fieldModel.getInlineHelpOrFormat());
		}
	}

	/**
	 * Validate a date select
	 *
	 * @param value The value
	 * @return A value check
	 */
	private ValueCheck validateDateSelect(final String value) {
		//check all parts exists
		final var parts = StringUtils.splitPreserveAllTokens(value, '.');
		for(final var part : parts) {
			if(part.trim().isEmpty()) {
				return InvalidDate.incomplete();
			}
		}
		//check consistency regarding precision
		var hasUnknownPart = false;
		for(var i = parts.length - 1; i >= 0; i--) {
			final var part = parts[i];

			if(part.equals(PartialDate.UNKNOWN_FIELD_LITERAL)) {
				hasUnknownPart = true;
			}
			else if(hasUnknownPart) {
				return i == 1 ? InvalidDate.inconsitentYear() : InvalidDate.inconsitentMonth();
			}
		}

		if(!hasUnknownPart) {
			try {
				ZonedDateTime.parse(value, fieldModel.getDateTimeFormatter());
			}
			catch(final DateTimeException e) {
				return InvalidDate.impossibleDate();
			}
		}
		return new ValueCheck(value);
	}
}
