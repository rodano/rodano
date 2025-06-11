package ch.rodano.core.model.mail;

import java.util.Set;

import ch.rodano.core.model.common.TimestampableObject;

public interface TimestampedMail extends TimestampableObject {
	int getAttempts();

	void setAttempts(final int attempts);

	MailStatus getStatus();

	void setStatus(final MailStatus status);

	MailOrigin getOrigin();

	void setOrigin(final MailOrigin origin);

	String getIntent();

	void setIntent(final String intent);

	String getSender();

	void setSender(final String sender);

	Set<String> getRecipients();

	void setRecipients(final Set<String> recipients);

	String getReplyTo();

	void setReplyTo(final String replyTo);

	default void increaseAttempts() {
		setAttempts(getAttempts() + 1);
	}
}
