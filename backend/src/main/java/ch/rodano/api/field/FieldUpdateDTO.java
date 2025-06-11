package ch.rodano.api.field;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Field update")
public class FieldUpdateDTO {

	@Schema(description = "Field reference")
	Long pk;
	@Schema(description = "Field model ID")
	@NotBlank
	String modelId;

	@Schema(description = "Field value")
	@NotBlank
	String value;
	@Schema(description = "Field file reference")
	Long filePk;

	boolean reset;
	String rationale;

	public final Long getPk() {
		return pk;
	}

	public final void setPk(final Long pk) {
		this.pk = pk;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(final String modelId) {
		this.modelId = modelId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public Long getFilePk() {
		return filePk;
	}

	public void setFilePk(final Long filePk) {
		this.filePk = filePk;
	}

	public boolean isReset() {
		return reset;
	}

	public void setReset(final boolean reset) {
		this.reset = reset;
	}

	public String getRationale() {
		return rationale;
	}

	public void setRationale(final String rationale) {
		this.rationale = rationale;
	}
}
