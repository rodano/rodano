package ch.rodano.configuration.model.event;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BinaryOperator;

public enum DateAggregationFunction {

	MAX {
		@Override
		public Optional<ZonedDateTime> aggregate(final Collection<ZonedDateTime> dates) {
			return dates.stream().max(ZonedDateTime::compareTo);
		}


		@Override
		public BinaryOperator<ZonedDateTime> getAccumulator() {
			return BinaryOperator.maxBy(ZonedDateTime::compareTo);
		}
	},
	MIN {
		@Override
		public Optional<ZonedDateTime> aggregate(final Collection<ZonedDateTime> dates) {
			return dates.stream().min(ZonedDateTime::compareTo);
		}

		@Override
		public BinaryOperator<ZonedDateTime> getAccumulator() {
			return BinaryOperator.minBy(ZonedDateTime::compareTo);
		}
	};

	public abstract Optional<ZonedDateTime> aggregate(Collection<ZonedDateTime> dates);

	public abstract BinaryOperator<ZonedDateTime> getAccumulator();
}
