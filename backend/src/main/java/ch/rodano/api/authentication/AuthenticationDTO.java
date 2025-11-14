package ch.rodano.api.authentication;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.Hidden;

public class AuthenticationDTO {
	@NotNull
	private String token;
	@Hidden
	private String tsKey;

	/**
	 * Constructor
	 *
	 * @param token The token
	 */
	public AuthenticationDTO(@JsonProperty("token") final String token) {
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
