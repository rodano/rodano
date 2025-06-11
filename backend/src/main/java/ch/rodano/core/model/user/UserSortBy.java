package ch.rodano.core.model.user;

import org.jooq.TableField;

import ch.rodano.core.model.jooq.tables.records.UserRecord;

import static ch.rodano.core.model.jooq.Tables.USER;

public enum UserSortBy {
	creationTime(USER.CREATION_TIME),
	name(USER.NAME),
	email(USER.EMAIL),
	phone(USER.PHONE);

	private final TableField<UserRecord, ?> field;

	UserSortBy(final TableField<UserRecord, ?> field) {
		this.field = field;
	}

	public TableField<UserRecord, ?> getField() {
		return field;
	}

}
