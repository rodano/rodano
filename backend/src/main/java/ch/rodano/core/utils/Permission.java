package ch.rodano.core.utils;

import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.model.event.Timeframe;

public record Permission(
	Profile profile,
	Timeframe timeframe
) {

	public Permission toHistoricalPermission() {
		return new Permission(profile, timeframe.withInfiniteStartDate());
	}

	@Override
	public String toString() {
		return String.format(
			"%s: %s",
			profile.getId(),
			timeframe.toString()
		);
	}
}
