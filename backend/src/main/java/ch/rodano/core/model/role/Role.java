package ch.rodano.core.model.role;

import java.time.ZonedDateTime;
import java.util.Comparator;

import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.common.PersistentObject;
import ch.rodano.core.model.common.TimestampableObject;

public class Role extends RoleRecord implements TimestampableObject, AuditableObject, Comparable<Role>, PersistentObject {

	public static final Comparator<Role> DEFAULT_COMPARATOR = Comparator
		.comparing(Role::getCreationTime)
		.thenComparing(Role::getPk);

	private Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	private Profile profile;

	public Role() {
		status = RoleStatus.PENDING;
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

	public void setProfile(final Profile profile) {
		this.profile = profile;
		this.profileId = profile.getId();
	}

	public Profile getProfile() {
		return profile;
	}

	@Override
	public final int compareTo(final Role otherRole) {
		//preserve consistency between equals and comparator
		if(equals(otherRole)) {
			return 0;
		}
		return DEFAULT_COMPARATOR.compare(this, otherRole);
	}

	public void enable() {
		status = RoleStatus.ENABLED;
	}

	public void disable() {
		status = RoleStatus.DISABLED;
	}

	public boolean isEnabled() {
		return RoleStatus.ENABLED.equals(status);
	}

	@Override
	public void onPostLoad(final Study study) {
		profile = study.getProfile(profileId);
	}

	@Override
	public void onPostUpdate(final Study study) {
		//nothing
	}

	@Override
	public void onPreUpdate() {
		//nothing
	}
}
