package ch.rodano.configuration.validation.text;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.model.validator.InvalidText;
import ch.rodano.configuration.validation.AbstractValidationFieldModel;

public class TextValidationTest {

	// ---------------------------------------------------------------
	//  Date Tests
	// ---------------------------------------------------------------

	@Test
	@DisplayName("Text too long")
	public void textTooLong() {
		text().maxLength(4).rejects("blabla").withErrorCause(tooLong(4));
		text().maxLength(6).accepts("blabla");
		text().maxLength(6).accepts("bla");
	}

	@Test
	@DisplayName("Text does not match")
	public void textDoesNotMatch() {
		final var matcher = "[A-Z][a-z]+";
		final var messages = Collections.singletonMap(AbstractValidationFieldModel.LANGUAGE, "Text must contain only letters and start with an uppercase letter");
		text().matcher(matcher, messages).rejects("blabla").withErrorCause(doesNotMatch(matcher, messages));
		text().matcher(matcher, messages).accepts("Blabla");
		text().matcher(matcher, null).rejects("blabla").withErrorCause(doesNotMatch(matcher, null));
		text().matcher(matcher, Collections.emptyMap()).rejects("blabla").withErrorCause(doesNotMatch(matcher, Collections.emptyMap()));
	}

	// ---------------------------------------------------------------
	// Helper methods
	// ---------------------------------------------------------------

	private TextValidationFieldModel text() {
		return new TextValidationFieldModel();
	}

	private String tooLong(final int maxLength) {
		return String.format(InvalidText.TOO_LONG.get(AbstractValidationFieldModel.LANGUAGE), maxLength);
	}

	private String doesNotMatch(final String matcher, final Map<String, String> matcherMessage) {
		final var message = matcherMessage != null && matcherMessage.containsKey(AbstractValidationFieldModel.LANGUAGE) ? matcherMessage.get(AbstractValidationFieldModel.LANGUAGE) : matcher;
		return String.format(InvalidText.DOES_NOT_MATCH.get(AbstractValidationFieldModel.LANGUAGE), message);
	}
}
