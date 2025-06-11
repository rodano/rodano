package ch.rodano.api.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User-submitted credentials")
public class CredentialsDTO {
	@Schema(description = "User e-mail")
	@NotBlank @Email private String email;

	@Schema(description = "Plain-text password")
	private String password;

	@Schema(description = "Special key used to authenticate user. If provided password, token and code must be blank.")
	private String authKey;

	@Schema(description = "Two-step token, if enabled")
	@Hidden
	private Integer tsToken;

	@Schema(description = "Two step one use code if token can not be provided by the user. If code is provided, tsToken must be blank.")
	@Hidden
	private String tsCode;

	@Schema(description = "Key that can be sent by the client. If provided and two step enabled for the user, the two step token won't be required")
	@Hidden
	private String tsKey;

	@Schema(description = "User decides if the clients is using can be trusted. If a client is trusted, two step won't be required if the 'key' is required")
	@Hidden
	private boolean tsTrust;

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(final String authKey) {
		this.authKey = authKey;
	}

	public Integer getTsToken() {
		return tsToken;
	}

	public void setTsToken(final Integer tsToken) {
		this.tsToken = tsToken;
	}

	public String getTsCode() {
		return tsCode;
	}

	public void setTsCode(final String tsCode) {
		this.tsCode = tsCode;
	}

	public String getTsKey() {
		return tsKey;
	}

	public void setTsKey(final String tsKey) {
		this.tsKey = tsKey;
	}

	public boolean isTsTrust() {
		return tsTrust;
	}

	public void setTsTrust(final boolean tsTrust) {
		this.tsTrust = tsTrust;
	}
}
