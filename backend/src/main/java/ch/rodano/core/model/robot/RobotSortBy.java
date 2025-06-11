package ch.rodano.core.model.robot;

import org.jooq.TableField;

import static ch.rodano.core.model.jooq.Tables.ROBOT;

public enum RobotSortBy {
	creationTime(ROBOT.CREATION_TIME),
	name(ROBOT.NAME),
	profileId(ROBOT.role().PROFILE_ID),
	scopeShortname(ROBOT.role().scope().SHORTNAME);

	private final TableField<?, ?> field;

	RobotSortBy(final TableField<?, ?> field) {
		this.field = field;
	}

	public TableField<?, ?> getField() {
		return field;
	}

}
