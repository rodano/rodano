package ch.rodano.core.model.jooqutils;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Param;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.model.scope.FieldModelCriterion;

public class JOOQTranslator {
	//TODO update this as soon as dates are stored in the ISO format
	public static Param<String> SQL_FIELD_DATE_FORMAT = DSL.inline("%d.%m.%Y");

	public static Condition translate(final Study study, final FieldModelCriterion criterion, final Field<String> sqlField) {
		final var fieldModel = criterion.getFieldModel(study);
		return translate(criterion.operator(), fieldModel, sqlField, criterion.value());
	}

	public static Condition translate(final Operator operator, final FieldModel fieldModel, final Field<String> sqlField, final String criterionValue) {
		final var sqlValue = DSL.value(criterionValue);
		switch(operator) {
			case EQUALS:
				return sqlField.eq(criterionValue);
			case NOT_EQUALS:
				return sqlField.ne(criterionValue);
			case CONTAINS:
				return sqlField.contains(criterionValue);
			case NOT_CONTAINS:
				return sqlField.notContains(criterionValue);
			case LOWER:
				return castSQLField(fieldModel, sqlField).lessThan(castSQLField(fieldModel, sqlValue));
			case GREATER:
				return castSQLField(fieldModel, sqlField).greaterThan(castSQLField(fieldModel, sqlValue));
			case LOWER_EQUALS:
				return castSQLField(fieldModel, sqlField).lessOrEqual(castSQLField(fieldModel, sqlValue));
			case GREATER_EQUALS:
				return castSQLField(fieldModel, sqlField).greaterOrEqual(castSQLField(fieldModel, sqlValue));
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

	private static <T> Field<T> castSQLField(final FieldModel fieldModel, final Field<String> field) {
		switch(fieldModel.getDataType()) {
			case DATE: {
				return (Field<T>) DSL.function("str_to_date", SQLDataType.LOCALDATETIME, field, SQL_FIELD_DATE_FORMAT);
			}
			case NUMBER:
				return (Field<T>) field.cast(Double.class);
			default:
				return (Field<T>) field;
		}
	}
}
