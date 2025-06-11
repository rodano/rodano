package ch.rodano.core.model.mail;

import org.jooq.TableField;

import ch.rodano.core.model.jooq.tables.records.MailRecord;

import static ch.rodano.core.model.jooq.Tables.MAIL;

public enum MailSortBy {
	sender(MAIL.SENDER), sentTime(MAIL.SENT_TIME), creationTime(MAIL.CREATION_TIME), subject(MAIL.SUBJECT), intent(MAIL.INTENT), origin(MAIL.ORIGIN), status(MAIL.STATUS);

	private final TableField<MailRecord, ?> field;

	MailSortBy(final TableField<MailRecord, ?> field) {
		this.field = field;
	}

	public TableField<MailRecord, ?> getField() {
		return field;
	}

}
