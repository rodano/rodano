package ch.rodano.core.model.event;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;

public record Timeframe(Optional<ZonedDateTime> startDate, Optional<ZonedDateTime> stopDate) {

	public static final Timeframe INFINITE_TIMEFRAME = new Timeframe(Optional.empty(), Optional.empty());

	public Timeframe(final ZonedDateTime startDate, final ZonedDateTime stopDate) {
		this(Optional.of(startDate), Optional.of(stopDate));
	}

	public Timeframe {
		Objects.requireNonNull(startDate);
		Objects.requireNonNull(stopDate);
		if(startDate.isPresent() && stopDate.isPresent()) {
			if(!startDate.get().isBefore(stopDate.get())) {
				throw new IllegalArgumentException("Start date must be before stop date");
			}
		}
	}

	public boolean isInfiniteStartDate() {
		return startDate.isEmpty();
	}

	public boolean isInfiniteStopDate() {
		return stopDate.isEmpty();
	}

	public Timeframe withExtension(final Timeframe timeframe) {
		final Optional<ZonedDateTime> start = isInfiniteStartDate() || timeframe.isInfiniteStartDate() ? Optional.empty() : Optional.of(ObjectUtils.min(startDate.get(), timeframe.startDate.get()));
		final Optional<ZonedDateTime> stop = isInfiniteStopDate() || timeframe.isInfiniteStopDate() ? Optional.empty() : Optional.of(ObjectUtils.max(stopDate.get(), timeframe.stopDate.get()));
		return new Timeframe(start, stop);
	}

	public Timeframe withInfiniteStartDate() {
		return new Timeframe(Optional.empty(), stopDate);
	}

	public Timeframe withInfiniteStopDate() {
		return new Timeframe(startDate, Optional.empty());
	}

	public boolean surroundDate(final ZonedDateTime date) {
		return (isInfiniteStartDate() || startDate.get().equals(date) || startDate.get().isBefore(date)) && (isInfiniteStopDate() || stopDate.get().equals(date) || stopDate.get().isAfter(date));
	}

	public boolean surroundNow() {
		return surroundDate(ZonedDateTime.now());
	}

	@Override
	public String toString() {
		return String.format(
			"%s → %s",
			startDate.map(ZonedDateTime::toString).orElse("∞"),
			stopDate.map(ZonedDateTime::toString).orElse("∞")
		);
	}
}
