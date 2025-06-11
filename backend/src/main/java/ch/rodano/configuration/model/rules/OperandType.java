package ch.rodano.configuration.model.rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum OperandType {
	STRING {
		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.CONTAINS, Operator.NOT_CONTAINS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	DATE {
		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER, Operator.GREATER_EQUALS, Operator.LOWER, Operator.LOWER_EQUALS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	NUMBER {
		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER, Operator.GREATER_EQUALS, Operator.LOWER, Operator.LOWER_EQUALS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	BOOLEAN {
		@Override
		public List<Operator> getOperators() {
			return Collections.singletonList(Operator.EQUALS);
		}
	},
	BLOB {
		@Override
		public List<Operator> getOperators() {
			return Collections.emptyList();
		}
	};

	public abstract List<Operator> getOperators();
}
