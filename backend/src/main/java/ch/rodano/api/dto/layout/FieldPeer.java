package ch.rodano.api.dto.layout;

import ch.rodano.api.field.FieldUpdateDTO;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.field.Field;

public record FieldPeer(
	Dataset dataset, Field field, FieldUpdateDTO fieldDTO, String originalValue
) {

	public FieldPeer(final Dataset dataset, final Field field, final FieldUpdateDTO fieldDTO, final String originalValue) {
		this.dataset = dataset;
		this.field = field;
		this.fieldDTO = fieldDTO;
		this.originalValue = originalValue;
	}

	public FieldPeer(final Dataset dataset, final Field field, final FieldUpdateDTO fieldDTO) {
		this(dataset, field, fieldDTO, field.getValue());
	}

	public String getId() {
		return field.getId();
	}
}
