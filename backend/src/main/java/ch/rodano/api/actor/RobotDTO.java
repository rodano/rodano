package ch.rodano.api.actor;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RobotDTO extends ActorDTO {
	@NotBlank String key;

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}
}
