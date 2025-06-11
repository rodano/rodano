package ch.rodano.configuration.model.common;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface Displayable {
	String getId();

	String getLocalizedShortname(final String... languages);

	String getLocalizedLongname(final String... languages);

	String getLocalizedDescription(final String... languages);

	static List<String> getIdsFromDisplayables(final Collection<? extends Displayable> displayables) {
		return displayables.stream()
			.filter(Objects::nonNull)
			.map(Displayable::getId)
			.toList();
	}
}
