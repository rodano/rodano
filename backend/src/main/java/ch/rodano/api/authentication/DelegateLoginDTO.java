package ch.rodano.api.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public record DelegateLoginDTO(
	@NotNull @Email String email
) { }
