package ch.rodano.core.model.robot;

import org.jooq.TableField;

import static ch.rodano.core.model.jooq.Tables.ROBOT;
import static ch.rodano.core.model.jooq.Tables.ROLE;
import static ch.rodano.core.model.jooq.Tables.SCOPE;

public enum RobotSortBy {
	creationTime(ROBOT.CREATION_TIME),
	name(ROBOT.NAME),
	profileId(ROLE.PROFILE_ID),
	scopeShortname(SCOPE.SHORTNAME);

	private final TableField<?, ?> field;

	RobotSortBy(final TableField<?, ?> field) {
		this.field = field;
	}

	public TableField<?, ?> getField() {
		return field;
	}

}
