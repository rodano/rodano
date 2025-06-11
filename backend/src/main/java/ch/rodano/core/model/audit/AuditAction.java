package ch.rodano.core.model.audit;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.actor.ActorType;

public class AuditAction implements Comparable<AuditAction> {

	private Long pk;

	private ZonedDateTime date;
	private Long userFk;
	private Long robotFk;
	private String context;

	public AuditAction(final Optional<Actor> possibleActor, final String context, final ZonedDateTime date) {
		//date used for the operation
		//truncate the date to the milliseconds because it is the smallest unit stored in the database
		//if we want to compare object from the database and object in Java memory, we need the same precision
		this.date = date.truncatedTo(ChronoUnit.MILLIS);
		this.context = context;
		possibleActor.ifPresent(actor -> {
			if(ActorType.USER.equals(actor.getType())) {
				this.userFk = actor.getPk();
			}
			else {
				this.robotFk = actor.getPk();
			}
		});
	}

	public AuditAction(final Optional<Actor> possibleActor, final String context) {
		this(possibleActor, context, ZonedDateTime.now());
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(final ZonedDateTime date) {
		this.date = date;
	}

	public Long getUserFk() {
		return userFk;
	}

	public Long getRobotFk() {
		return robotFk;
	}

	public void setRobotFk(final Long robotFk) {
		this.robotFk = robotFk;
	}

	public void setUserFk(final Long userFk) {
		this.userFk = userFk;
	}

	public String getContext() {
		return context;
	}

	public void setContext(final String context) {
		this.context = context;
	}

	@Override
	public int compareTo(final AuditAction o) {
		return date.compareTo(o.getDate());
	}
}
