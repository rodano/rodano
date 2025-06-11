package ch.rodano.api.authentication;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.Hidden;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationDTO {
	@NotNull private String token;
	@Hidden private String tsKey;

	/**
	 * Default constructor, needed by some serializer
	 */
	AuthenticationDTO() {

	}

	/**
	 * Constructor
	 *
	 * @param token The token
	 */
	public AuthenticationDTO(final String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public String getTsKey() {
		return tsKey;
	}

	public void setTsKey(final String tsKey) {
		this.tsKey = tsKey;
	}
}
