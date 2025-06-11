package ch.rodano.core.model.rules.formula;

import java.util.Arrays;
import java.util.Optional;

public enum Operator {
	PLUS('+'), MINUS('-'), DIVIDE('/'), MULTIPLY('*');

	public final char symbol;

	Operator(final char symbol) {
		this.symbol = symbol;
	}

	public static Optional<Operator> getOperatorFromChar(final char symbol) {
		return Arrays.stream(Operator.values()).filter(o -> o.symbol == symbol).findFirst();
	}
}
