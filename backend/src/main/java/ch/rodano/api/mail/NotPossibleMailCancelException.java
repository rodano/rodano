package ch.rodano.api.mail;

import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.core.model.mail.MailStatus;

public class NotPossibleMailCancelException extends MailException implements ManagedException {
	private static final long serialVersionUID = 7223692675898572163L;

	public NotPossibleMailCancelException(final Long mailPk, final MailStatus status) {
		super("Cannot cancel mail with pk [" + mailPk + "] because its status is " + status.getStatus());
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.CONFLICT;
	}
}
