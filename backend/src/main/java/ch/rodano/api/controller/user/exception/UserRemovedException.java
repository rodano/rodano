package ch.rodano.api.controller.user.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class UserRemovedException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = 3504628712406150117L;

	public UserRemovedException() {
		super("The user has been removed");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
