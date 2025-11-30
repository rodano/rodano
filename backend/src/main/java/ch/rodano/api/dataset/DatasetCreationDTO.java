package ch.rodano.api.dataset;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.field.FieldUpdateDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Dataset creation")
public class DatasetCreationDTO {

	@Schema(description = "Dataset ID")
	@NotBlank
	String id;

	@Schema(description = "Dataset model ID")
	@NotNull
	UUID modelId;

	@Schema(description = "Fields associated with the dataset")
	@NotNull
	List<FieldUpdateDTO> fields;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public UUID getModelId() {
		return modelId;
	}

	public void setModelId(final UUID modelId) {
		this.modelId = modelId;
	}

	public final List<FieldUpdateDTO> getFields() {
		return fields;
	}

	public final void setFields(final List<FieldUpdateDTO> fields) {
		this.fields = fields;
	}

}
