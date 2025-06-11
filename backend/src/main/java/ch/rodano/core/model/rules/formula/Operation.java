package ch.rodano.core.model.rules.formula;

import ch.rodano.core.model.rules.formula.exception.FormulaBadSyntaxException;

public class Operation {
	private Double operand1;
	private Double operand2;
	private Operator operator;

	public void addOperand(final Double operand) throws FormulaBadSyntaxException {
		if(operand1 == null) {
			operand1 = operand;
		}
		else if(operand2 == null) {
			operand2 = operand;
		}
		else {
			throw new FormulaBadSyntaxException("You must use an operator to separate two operands");
		}
	}

	public boolean isComplete() {
		return operand1 != null && operand2 != null && operator != null;
	}

	public Double calculate() {
		if(!isComplete()) {
			throw new UnsupportedOperationException("Unable to perform calculation is operation is not complete");
		}
		if(Operator.PLUS.equals(operator)) {
			return operand1 + operand2;
		}
		if(Operator.MINUS.equals(operator)) {
			return operand1 - operand2;
		}
		else if(Operator.MULTIPLY.equals(operator)) {
			return operand1 * operand2;
		}
		else if(Operator.DIVIDE.equals(operator)) {
			return operand1 / operand2;
		}
		throw new UnsupportedOperationException("Only operators [PLUS,MINUS,MULTIPLY,DIVIDE] are supported");
	}

	public Double getOperand1() {
		return operand1;
	}

	public Double getOperand2() {
		return operand2;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(final Operator operator) {
		this.operator = operator;
	}
}
