package ch.rodano.core.model.scope;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.configuration.model.study.Study;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FieldModelCriterion(
	UUID datasetModelId,
	UUID fieldModelId,
	Operator operator,
	String value
) {

	public FieldModelCriterion {
		Objects.requireNonNull(datasetModelId);
		Objects.requireNonNull(fieldModelId);
		Objects.requireNonNull(operator);
	}

	public FieldModelCriterion(final FieldModel fieldModel, final Operator operator, final String value) {
		this(fieldModel.getDatasetModel().getDatasetModelId(), fieldModel.getFieldModelId(), operator, value);
	}

	@JsonIgnore
	public final boolean hasFieldModel() {
		return datasetModelId != null && fieldModelId != null;
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
