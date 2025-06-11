package ch.rodano.core.model.jooqutils;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.model.rules.Operator;

public class JOOQTranslator {

	public static Condition translate(final Operator operator, final FieldModel fieldModel, final Field sqlField, final String criterionValue) {
		switch(operator) {
			case EQUALS:
				return sqlField.eq(parseValue(fieldModel, criterionValue));
			case NOT_EQUALS:
				return sqlField.ne(parseValue(fieldModel, criterionValue));
			case CONTAINS:
				return sqlField.contains(criterionValue);
			case NOT_CONTAINS:
				return sqlField.notContains(criterionValue);
			case LOWER:
				return sqlField.lessThan(parseValue(fieldModel, criterionValue));
			case GREATER:
				return sqlField.greaterThan(parseValue(fieldModel, criterionValue));
			case LOWER_EQUALS:
				return sqlField.lessOrEqual(parseValue(fieldModel, criterionValue));
			case GREATER_EQUALS:
				return sqlField.greaterOrEqual(parseValue(fieldModel, criterionValue));
			case NULL:
				return sqlField.isNull();
			case NOT_NULL:
				return sqlField.isNotNull();
			case BLANK:
				return sqlField.isNull().or(sqlField.like(""));
			case NOT_BLANK:
				return sqlField.isNotNull().and(sqlField.notLike(""));
			default:
				final var errorMessage = String.format("%s operator is not supported", operator.name());
				throw new UnsupportedOperationException(errorMessage);
		}
	}

	private static Field parseValue(final FieldModel fieldModel, final String value) {
		if(OperandType.DATE.equals(fieldModel.getDataType())) {
			final PartialDate date = (PartialDate) fieldModel.stringToObject(value);
			return DSL.val(date.toZonedDateTime().get());
		}
		if(OperandType.STRING.equals(fieldModel.getDataType())) {
			return DSL.val(value);
		}
		return DSL.val(value);
	}
}
