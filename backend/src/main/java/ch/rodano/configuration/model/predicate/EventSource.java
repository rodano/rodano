package ch.rodano.configuration.model.predicate;

public enum EventSource {
	INCEPTIVE,
	FIRST,
	LAST,
	POSITION,
	BEFORE,
	BEFORE_ITER {
		@Override
		public String toString() {
			return "Before (Iterative)";
		}
	},
	AFTER,
	CURRENT
}
