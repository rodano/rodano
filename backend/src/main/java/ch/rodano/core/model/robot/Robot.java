package ch.rodano.core.model.robot;

import java.time.ZonedDateTime;
import java.util.Objects;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.actor.ActorType;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.TimestampableObject;

public class Robot extends RobotRecord implements DeletableObject, TimestampableObject, AuditableObject, Actor {

	private Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	public Robot() {
		super();
	}

	@Override
	public Long getPk() {
		return pk;
	}

	@Override
	public void setPk(final Long pk) {
		this.pk = pk;
	}

	@Override
	public String getLanguageId() {
		return null;
	}

	@Override
	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	@Override
	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	@Override
	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(final Object o) {
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		final var robot = (Robot) o;
		if(name == null && robot.name == null) {
			return this == o;
		}
		return Objects.equals(name, robot.name);
	}

	@Override
	public ActorType getType() {
		return ActorType.ROBOT;
	}

	@Override
	public boolean isActivated() {
		return true;
	}
}
