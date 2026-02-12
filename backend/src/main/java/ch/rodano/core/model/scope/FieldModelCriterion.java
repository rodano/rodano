package ch.rodano.core.model.scope;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.configuration.model.study.Study;

public record FieldModelCriterion(
	String datasetModelId,
	String fieldModelId,
	Operator operator,
	String value
) {

	public FieldModelCriterion {
		Objects.requireNonNull(datasetModelId);
		Objects.requireNonNull(fieldModelId);
		Objects.requireNonNull(operator);
	}

	public FieldModelCriterion(final FieldModel fieldModel, final Operator operator, final String value) {
		this(fieldModel.getDatasetModel().getId(), fieldModel.getId(), operator, value);
	}

	@JsonIgnore
	public final boolean hasFieldModel() {
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
}
