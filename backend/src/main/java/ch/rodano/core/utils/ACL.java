package ch.rodano.core.utils;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Assignable;
import ch.rodano.configuration.model.rights.FamilyAssignableChild;
import ch.rodano.configuration.model.rights.FamilyAssignableParent;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.scope.Scope;

public record ACL(
	Actor actor,
	Optional<Scope> scope,
	List<Permission> permissions
) {

	//TODO improve this
	public static ACL ANONYMOUS = new ACL(null, Optional.empty(), Collections.emptyList());

	/**
	 * Historical permissions are permissions with their start dates extended infinitely.
	 * This is used to check if the read right is possible.
	 * Having a reading right at one specific moment often extends to the right to read before this moment.
	 *
	 * @return the historical permissions
	 */
	public List<Permission> historicalPermissions() {
		return permissions.stream()
			.map(Permission::toHistoricalPermission)
			.toList();
	}

	/**
	 * Returns a list of the historical permissions for a given date-time
	 *
	 * @param date reference date-time
	 * @return the historical permissions for a given date-time
	 */
	public List<Permission> historicalPermissions(final ZonedDateTime date) {
		return historicalPermissions().stream()
			.filter(p -> p.timeframe().surroundDate(date))
			.toList();
	}

	/**
	 * Returns a list of the permissions valid for a given date-time
	 *
	 * @param date reference date-time
	 * @return the valid permissions for a given date-time
	 */
	public List<Permission> validPermissions(final ZonedDateTime date) {
		return permissions.stream().filter(p -> p.timeframe().surroundDate(date)).toList();
	}

	/**
	 * Returns a list of the permissions that are currently valid
	 *
	 * @return the currently valid permissions
	 */
	public List<Permission> currentPermissions() {
		return validPermissions(ZonedDateTime.now());
	}

	public List<Profile> getProfiles() {
		return currentPermissions().stream().map(Permission::profile).toList();
	}

	//retrieve timeframe associated to a right
	public Optional<Timeframe> getTimeframe(final RightAssignable<?> rightAssignable, final Rights right) {
		return permissions.stream()
			.filter(p -> p.profile().hasRight(rightAssignable, right))
			.map(Permission::timeframe)
			.reduce(Timeframe::withExtension);
	}

	private boolean hasRight(final Predicate<Profile> profileCheck) {
		return currentPermissions().stream().anyMatch(p -> profileCheck.test(p.profile()));
	}

	public boolean hasRightAdmin() {
		return hasRight(p -> p.hasFeature(FeatureStatic.ADMIN.name()));
	}

	public boolean hasRight(final FeatureStatic feature) {
		return hasRight(p -> p.hasFeature(feature.name()));
	}

	public boolean hasRight(final Assignable<?> assignable) {
		return hasRight(p -> p.hasRight(assignable));
	}

	public boolean hasRight(final RightAssignable<?> rightAssignable, final Rights right) {
		//to read a right assignable, it's enough to have had the right to read it once
		if(right == Rights.READ) {
			return hasHistoricalRight(rightAssignable, right);
		}
		return hasRight(p -> p.hasRight(rightAssignable, right));
	}

	public boolean hasRight(final FamilyAssignableParent<?> familyAssignable) {
		return hasRight(p -> p.hasRight(familyAssignable));
	}

	public boolean hasRight(final FamilyAssignableChild<?> familyAssignable) {
		return hasRight(p -> p.hasRight(familyAssignable));
	}

	/**
	 * Check if the rights are present on a specific date
	 * @param date The reference date
	 * @param profileCheck The profile predicate
	 * @return True if has rights, false otherwise
	 */
	private boolean hasRight(final ZonedDateTime date, final Predicate<Profile> profileCheck) {
		return validPermissions(date).stream().anyMatch(p -> profileCheck.test(p.profile()));
	}

	public boolean hasRightAdmin(final ZonedDateTime date) {
		return hasRight(date, p -> p.hasFeature(FeatureStatic.ADMIN.name()));
	}

	public boolean hasRight(final ZonedDateTime date, final FeatureStatic feature) {
		return hasRight(date, p -> p.hasFeature(feature.name()));
	}

	public boolean hasRight(final ZonedDateTime date, final RightAssignable<?> rightAssignable, final Rights right) {
		//to read a right assignable, it's enough to have had the right to read it once
		if(right == Rights.READ) {
			return hasHistoricalRight(date, rightAssignable, right);
		}
		return hasRight(date, p -> p.hasRight(rightAssignable, right));
	}

	public boolean hasRight(final ZonedDateTime date, final FamilyAssignableParent<?> familyAssignable) {
		return hasRight(date, p -> p.hasRight(familyAssignable));
	}

	public boolean hasRight(final ZonedDateTime date, final FamilyAssignableChild<?> familyAssignable) {
		return hasRight(date, p -> p.hasRight(familyAssignable));
	}

	//has historical right
	public boolean hasHistoricalRight(final RightAssignable<?> rightAssignable, final Rights right) {
		return historicalPermissions().stream().anyMatch(p -> p.profile().hasRight(rightAssignable, right));
	}

	public boolean hasHistoricalRight(final ZonedDateTime date, final RightAssignable<?> rightAssignable, final Rights right) {
		return historicalPermissions(date).stream().anyMatch(p -> p.profile().hasRight(rightAssignable, right));
	}

	//check rights
	public void checkRight(final FeatureStatic feature) {
		if(!hasRight(feature)) {
			throw new UnauthorizedException(feature);
		}
	}

	public void checkRight(final RightAssignable<?> rightAssignable, final Rights right) {
		if(!hasRight(rightAssignable, right)) {
			throw UnauthorizedException.getInstance(rightAssignable, right);
		}
	}

	public void checkRight(final FamilyAssignableParent<?> familyAssignable) {
		if(!hasRight(familyAssignable)) {
			throw new UnauthorizedException(familyAssignable);
		}
	}

	public void checkRight(final FamilyAssignableChild<?> familyAssignable) {
		if(!hasRight(familyAssignable)) {
			throw UnauthorizedException.getInstance(familyAssignable);
		}
	}
}
