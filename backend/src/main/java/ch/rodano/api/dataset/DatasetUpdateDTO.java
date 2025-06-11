package ch.rodano.api.dataset;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.field.FieldUpdateDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Dataset update")
public class DatasetUpdateDTO {

	@Schema(description = "Dataset reference")
	@NotNull
	Long pk;

	@Schema(description = "Fields associated with the dataset")
	@NotNull
	List<FieldUpdateDTO> fields;

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public final List<FieldUpdateDTO> getFields() {
		return fields;
	}

	public final void setFields(final List<FieldUpdateDTO> fields) {
		this.fields = fields;
	}

}
