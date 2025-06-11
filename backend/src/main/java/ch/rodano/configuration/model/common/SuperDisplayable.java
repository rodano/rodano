package ch.rodano.configuration.model.common;

import java.util.Map;

import ch.rodano.configuration.model.language.Language;
import ch.rodano.configuration.utils.DisplayableUtils;

public interface SuperDisplayable extends Displayable {
	Map<String, String> getShortname();

	Map<String, String> getLongname();

	Map<String, String> getDescription();

	//shortname
	@Override
	default String getLocalizedShortname(final String... languages) {
		return DisplayableUtils.getLocalizedMapWithDefault(getShortname(), "", languages);
	}

	default String getLocalizedShortname(final Language... languages) {
		return DisplayableUtils.getLocalizedMapWithDefault(getShortname(), "", languages);
	}

	//longname
	@Override
	default String getLocalizedLongname(final String... languages) {
		return DisplayableUtils.getLocalizedMapWithDefault(getLongname(), "", languages);
	}

	default String getLocalizedLongname(final Language... languages) {
		return DisplayableUtils.getLocalizedMapWithDefault(getLongname(), "", languages);
	}

	//description
	@Override
	default String getLocalizedDescription(final String... languages) {
		return DisplayableUtils.getLocalizedMapWithDefault(getDescription(), "", languages);
	}

	default String getLocalizedDescription(final Language... languages) {
		return DisplayableUtils.getLocalizedMapWithDefault(getDescription(), "", languages);
	}
}
