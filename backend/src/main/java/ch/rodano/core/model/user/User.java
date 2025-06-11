package ch.rodano.core.model.user;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.actor.ActorType;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.TimestampableObject;

public final class User extends UserRecord implements DeletableObject, TimestampableObject, AuditableObject, Actor, Comparable<User> {

	public static final Comparator<User> DEFAULT_COMPARATOR = Comparator
		.comparing(User::getEmail)
		.thenComparing(User::getCreationTime)
		.thenComparing(User::getPk);

	private Long pk;
	private ZonedDateTime creationTime;
	private ZonedDateTime lastUpdateTime;

	// TODO 2FA reintegrate this property with 2FA
	//@IgnoreDatabaseProperty
	//private UserData userData;

	public User() {
		super();
		//userData = new UserData();
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

	/**
	 * Update both login date and previous login date
	 *
	 * @param date The new login date
	 */
	public void updateLoginDates(final ZonedDateTime date) {
		setPreviousLoginDate(getLoginDate());
		this.loginDate = date;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email);
	}

	@Override
	public boolean equals(final Object o) {
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		final var user = (User) o;
		if(email == null && user.email == null) {
			return this == o;
		}
		return Objects.equals(email, user.email);
	}

	@Override
	public int compareTo(final User otherUser) {
		//preserve consistency between equals and comparator
		if(equals(otherUser)) {
			return 0;
		}
		return DEFAULT_COMPARATOR.compare(this, otherUser);
	}

	// TODO 2FA
	/*
	@Override
	public void onPostLoad(final Study study) {
		if(getData() != null && getData().length > 0) {
			try {
				setUserData(MAPPER.readValue(ArrayUtils.toPrimitive(getData()), UserData.class));
			}
			catch(final IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		else {
			setUserData(new UserData());
		}
	}
	
	@Override
	public void onPreUpdate() {
		/*
		try {
			setData(ArrayUtils.toObject(MAPPER.writeValueAsBytes(getUserData())));
		}
		catch(final IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			setData(null);
		}
	}
	
	@Override
	public void onPostUpdate(final DatabaseAction action) {
		onPostLoad();
	}
	*/

	@Override
	public ActorType getType() {
		return ActorType.USER;
	}

	@Override
	public String toString() {
		return "User{" + super.toString() + ", email='" + email + ", name='" + name + "'}";
	}
}
