package ch.rodano.configuration.model.validator;

public interface ValueError {
	String getDefaultLocalizedMessage();
	String getLocalizedMessage(final String... languages);
}
