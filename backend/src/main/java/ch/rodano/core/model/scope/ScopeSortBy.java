package ch.rodano.core.model.scope;

import org.jooq.TableField;

import ch.rodano.core.model.jooq.tables.records.ScopeRecord;

import static ch.rodano.core.model.jooq.Tables.SCOPE;

public enum ScopeSortBy {
	creationTime(SCOPE.CREATION_TIME),
	code(SCOPE.CODE),
	shortname(SCOPE.SHORTNAME),
	longname(SCOPE.LONGNAME); 

	private final TableField<ScopeRecord, ?> field;

	ScopeSortBy(final TableField<ScopeRecord, ?> field) {
		this.field = field;
	}

	public TableField<ScopeRecord, ?> getField() {
		return field;
	}
}
