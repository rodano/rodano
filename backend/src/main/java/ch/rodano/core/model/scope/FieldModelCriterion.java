package ch.rodano.core.model.scope;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.predicate.EventSource;
import ch.rodano.configuration.model.predicate.ValueSource;
import ch.rodano.configuration.model.predicate.ValueSourceCriteria;
import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.configuration.model.study.Study;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FieldModelCriterion(String datasetModelId, String fieldModelId, Operator operator, String value) {

	public FieldModelCriterion {
		Objects.requireNonNull(datasetModelId);
		Objects.requireNonNull(fieldModelId);
	}

	public FieldModelCriterion(final FieldModel fieldModel, final Operator operator, final String value) {
		this(fieldModel.getDatasetModel().getId(), fieldModel.getId(), operator, value);
	}

	@JsonIgnore
	public boolean hasFieldModel() {
		return StringUtils.isNotBlank(datasetModelId) && StringUtils.isNotBlank(fieldModelId);
	}

	@JsonIgnore
	public boolean isValid() {
		return hasFieldModel() && operator != null && (!operator.hasValue() || value != null);
	}

	@JsonIgnore
	public FieldModel getFieldModel(final Study study) {
		return study.getDatasetModel(datasetModelId).getFieldModel(fieldModelId);
	}

	@JsonIgnore
	public boolean hasValidValue(final Study study) {
		if(value == null) {
			return true;
		}
		final var fieldModel = getFieldModel(study);
		return fieldModel.checkAndSanitizeValue(fieldModel.getPossibleValues(), value).isValid();
	}

	@Override
	public String toString() {
		if(isValid()) {
			return String.format("%s %s %s %s", datasetModelId, fieldModelId, operator.toString().toLowerCase(), value);
		}
		return String.format("Incomplete criterion: %s %s %s %s", datasetModelId, fieldModelId, operator, value);
	}

	@JsonIgnore
	public ValueSource getValueSource(final Study study) throws Exception {
		if(!isValid()) {
			throw new Exception("Incomplete criterion");
		}
		final var valueSource = new ValueSource();
		valueSource.setStudy(study);
		valueSource.setDatasetModelId(datasetModelId);
		valueSource.setFieldModelId(fieldModelId);

		final var criteria = new ValueSourceCriteria();
		criteria.setStudy(study);
		criteria.setDatasetModelId(datasetModelId);
		criteria.setFieldModelId(fieldModelId);
		criteria.setValue(value);
		criteria.setOperator(operator);
		criteria.setEventSource(EventSource.FIRST);

		valueSource.setCriteria(criteria);
		return valueSource;
	}

	/**
	 * Converts the current FieldModelCriterion into a jOOQ {@link Condition} based on the specified parameters.
	 * The method utilizes the operator and value of the FieldModelCriterion to construct a condition on the given database field.
	 *
	 * @param dsl   The {@link DSLContext} used to access jOOQ DSL features for database interaction.
	 * @param study The {@link Study} object containing the context and data for the transformation.
	 * @param alias The alias used to qualify the field name in the SQL query.
	 * @return A jOOQ {@link Condition} object representing the SQL condition corresponding to the criterion.
	 * @throws UnsupportedOperationException If the operator is not supported.
	 */
	public Condition toCondition(final DSLContext dsl, final Study study, final String alias) {
		final var col = DSL.field(DSL.name(alias, "value"));

		return switch (operator) {
			case EQUALS -> col.eq(DSL.inline(value));
			case NOT_EQUALS -> col.ne(DSL.inline(value));
			case GREATER -> col.gt(DSL.inline(value));
			case GREATER_EQUALS -> col.ge(DSL.inline(value));
			case LOWER -> col.lt(DSL.inline(value));
			case LOWER_EQUALS -> col.le(DSL.inline(value));
			case NULL -> col.isNull();
			case NOT_NULL -> col.isNotNull();
			case CONTAINS -> col.like("%" + value + "%");
			case NOT_CONTAINS -> col.notLike("%" + value + "%");
			default -> throw new UnsupportedOperationException("Unsupported operator: " + operator);
		};
	}


	public static class Builder {
		private String datasetModelId;
		private String fieldModelId;
		private Operator operator;
		private String value;

		public Builder datasetModelId(final String id) {
			this.datasetModelId = id;
			return this;
		}

		public Builder fieldModelId(final String id) {
			this.fieldModelId = id;
			return this;
		}

		public Builder operator(final Operator op) {
			this.operator = op;
			return this;
		}

		public Builder value(final String v) {
			this.value = v;
			return this;
		}

		public FieldModelCriterion build() {
			return new FieldModelCriterion(datasetModelId, fieldModelId, operator, value);
		}
	}


}
