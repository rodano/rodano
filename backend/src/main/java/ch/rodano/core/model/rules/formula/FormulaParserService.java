package ch.rodano.core.model.rules.formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.rules.formula.exception.FormulaBadSyntaxException;
import ch.rodano.core.model.rules.formula.exception.FormulaNullConditionException;
import ch.rodano.core.model.rules.formula.exception.FormulaWrongFunctionParametersException;
import ch.rodano.core.model.rules.formula.exception.UnableToCalculateFormulaException;
import ch.rodano.core.services.rule.RulableEntityBinderService;

@Service
public class FormulaParserService {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final static Predicate<Character> FUNCTION_ALLOWED_CHARACTERS = c -> Character.isAlphabetic(c) || c == '_';

	private record Token(
		String value,
		int index
	) {
		//nothing to do here
	}

	private record Result(
		Object value,
		int index
	) {
		//nothing to do here
	}

	private final RulableEntityBinderService rulableEntityBinderService;

	public FormulaParserService(final RulableEntityBinderService rulableEntityBinderService) {
		this.rulableEntityBinderService = rulableEntityBinderService;
	}

	public <T> T parse(final String formula) throws UnableToCalculateFormulaException {
		return parse(formula, Collections.emptyMap());
	}

	@SuppressWarnings("unchecked")
	public <T> T parse(final String formula, final Map<String, DataState> evaluations) throws UnableToCalculateFormulaException {
		try {
			logger.info("Parsing formula {}", formula);
			//starts index at 1, after the "=" character
			final Result result = parseFormula(formula.substring(1), evaluations);
			if(result.index < formula.length() - 1) {
				throw new FormulaBadSyntaxException("A suboperation has not been terminated");
			}
			logger.info("Result of formula {} is {}", formula, result.value);
			return (T) result.value;
		}
		catch(final Exception e) {
			throw new UnableToCalculateFormulaException(e);
		}
	}

	private Object evaluateOperand(final String operand, final Map<String, DataState> evaluations) {
		//condition
		if(operand.contains(":")) {
			//retrieve condition id and property id in operand
			final int dotIndex = operand.indexOf(":");
			final String conditionId = operand.substring(0, dotIndex);
			final String propertyId = operand.substring(dotIndex + 1);

			//retrieve condition and property
			final DataState formulaState = evaluations.get(conditionId);

			if(formulaState == null) {
				throw new FormulaNullConditionException(String.format("No condition with id %s", conditionId));
			}
			if(formulaState.getReferenceEvaluables().isEmpty()) {
				throw new FormulaNullConditionException(String.format("Condition with id %s is empty", conditionId));
			}

			//take first evaluable
			try {
				final var evaluable = formulaState.getReferenceEvaluables().iterator().next();
				final var formulaProperty = rulableEntityBinderService.getAttribute(formulaState.reference(), propertyId);
				final var value = formulaProperty.getValue(evaluable);
				//hack adapt value to type that can be used by the formula parser
				if(value instanceof Number) {
					return ((Number) value).doubleValue();
				}
				return value;
			}
			catch(final Exception e) {
				throw new FormulaNullConditionException(String.format("Unable to get a value for condition %s", conditionId), e);
			}
		}
		//number
		if(NumberUtils.isCreatable(operand)) {
			return Double.parseDouble(operand);
		}
		//string
		if(operand.startsWith("\"") && operand.endsWith("\"")) {
			return StringUtils.substring(operand, 1, -1);
		}
		throw new FormulaBadSyntaxException(String.format("%s is not a valid operand: only conditions, string or number are supported", operand));
	}

	//when encountering a stop (either the end of a subformula or the end of the main formula or a new operator), prepare next operation
	private Operation addOperand(final Operation operation, final Double operand) {
		//end "running" operand if any
		/*if(!operand.isEmpty()) {
			operation.addOperand(evaluateOperand(operand));
		}*/
		operation.addOperand(operand);
		//calculate result if the current operation is complete (it has two operands and an operator)
		//it may not necessary if the formula does not contain any operation, for example "=3"
		if(operation.isComplete()) {
			final var result = operation.calculate();
			final var newOperation = new Operation();
			newOperation.addOperand(result);
			return newOperation;
		}
		return operation;
	}

	private Object endOperation(final Operation operation) {
		if(operation.isComplete()) {
			return operation.calculate();
		}
		return operation.getOperand1();
	}

	private Token eat(final String formula, final int index, final Predicate<Character> validator) {
		int i = index;
		final var result = new StringBuilder();
		while(i < formula.length() && validator.test(formula.charAt(i))) {
			result.append(formula.charAt(i));
			i++;
		}
		return new Token(result.toString(), i);
	}

	private Result parseFormula(final String formula, final Map<String, DataState> evaluations) throws FormulaBadSyntaxException, FormulaNullConditionException,
		FormulaWrongFunctionParametersException {
		//current operation and operand
		final StringBuilder operand = new StringBuilder();

		//current position in formula
		int index = 0;

		while(index < formula.length()) {
			final char character = formula.charAt(index);

			//signs that stop parsing
			//a comma ends a function parameter
			//a parenthesis ends a function call or a sub-operation
			if(character == ',' || character == ')') {
				return new Result(evaluateOperand(operand.toString().trim(), evaluations), index);
			}

			//detect a potential operator
			/*final var operator = Operator.getOperatorFromChar(character);
			if(operator.isPresent()) {
				//operation = addOperand(operation, operand.toString());
				operand = new StringBuilder();
				//check that there is not already an operator (it may happen if there is two consequent operators)
				if(operation.getOperator() != null) {
					throw new FormulaBadSyntaxException("An operator must be between two operands");
				}
				operation.setOperator(operator.get());
				index++;
				continue;
			}*/
			//if it's not an operator, it's a function or an operand
			//beginning of a function or a condition
			if(FUNCTION_ALLOWED_CHARACTERS.test(character)) {
				final var result = eat(formula, index, FUNCTION_ALLOWED_CHARACTERS);
				index = result.index;
				//function
				if(index < formula.length() && formula.charAt(index) == '(') {
					//retrieve function
					final FormulaFunction function = FormulaFunction.valueOf(result.value);
					//advance after the opening parenthesis
					index++;
					//retrieve parameters
					final List<Object> parameters = new ArrayList<>();
					while(index < formula.length() && formula.charAt(index) != ')') {
						final var subresult = parseFormula(formula.substring(index), evaluations);
						parameters.add(subresult.value);
						index += subresult.index;
						//advance after the comma if any
						//the syntax of formula may not be correct, so always check if index is valid
						if(index < formula.length() && formula.charAt(index) == ',') {
							index++;
						}
					}
					if(index >= formula.length() || formula.charAt(index) != ')') {
						throw new FormulaBadSyntaxException(String.format("Missing closing parenthesis for function %s", function.name()));
					}
					//advance after the closing parenthesis
					index++;
					return new Result(function.getValue(parameters), index);
				}
				operand.append(result.value);
				continue;
			}
			//beginning of an operand
			operand.append(character);
			index++;
		}
		return new Result(evaluateOperand(operand.toString(), evaluations), index);
	}
}
