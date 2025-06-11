package ch.rodano.core.model.exception;

import java.io.Serial;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Assignable;
import ch.rodano.configuration.model.rights.Attributable;
import ch.rodano.configuration.model.rights.ProfileRightAssignable;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.actor.Actor;

public class UnauthorizedException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = 203540561709788783L;

	private static final String DEFAULT_MESSAGE = "You do not have enough rights";

	//generic right
	private static final String RIGHT_MESSAGE = "You are missing the right to %s";

	//assignable
	private static final String ASSIGNABLE_MESSAGE = "You do not have enough rights on this object. You must have rights on %s %s";

	//right assignable
	private static final String RIGHT_ASSIGNABLE_MESSAGE = "You do not have enough rights on this object. You must have rights to %s";

	//profile right assignable
	private static final String PROFILE_RIGHT_ASSIGNABLE_MESSAGE = "You do not have enough rights on this object. You must have rights on %s managed by %s";

	public UnauthorizedException() {
		super(DEFAULT_MESSAGE);
	}

	public UnauthorizedException(final String message) {
		super(message);
	}

	public UnauthorizedException(final Rights right) {
		super(String.format(RIGHT_MESSAGE, right.name()));
	}

	//feature
	public UnauthorizedException(final FeatureStatic feature) {
		super(String.format(RIGHT_MESSAGE, feature.getId()));
	}

	//assignable
	public UnauthorizedException(final Assignable<?> assignable) {
		super(String.format(ASSIGNABLE_MESSAGE, assignable.getEntity().name(), assignable.getId()));
	}

	//attributable
	public UnauthorizedException(final Attributable<?> attributable) {
		super(String.format(ASSIGNABLE_MESSAGE, attributable.getEntity().name(), attributable.getId()));
	}

	//right assignable
	public static UnauthorizedException getInstance(final RightAssignable<?> rightAssignable, final Rights right) {
		final var rightMessage = String.format("%s %s %s", right.name(), rightAssignable.getEntity().name(), rightAssignable.getId());
		return new UnauthorizedException(String.format(RIGHT_ASSIGNABLE_MESSAGE, rightMessage));
	}

	public static UnauthorizedException getInstance(final Collection<RightAssignable<?>> rightAssignables, final Rights right) {
		final var assignablesIds = rightAssignables.stream().map(RightAssignable::getId).collect(Collectors.joining(", "));
		final var entity = rightAssignables.iterator().next().getEntity();
		final var rightMessage = String.format("%s %s %s", right.name(), entity.name(), assignablesIds);
		return new UnauthorizedException(String.format(RIGHT_ASSIGNABLE_MESSAGE, rightMessage));
	}

	//profile right assignable
	public static UnauthorizedException getInstance(final ProfileRightAssignable<?> profileRightAssignable, final Optional<Profile> profile) {
		final var creator = profile.map(Profile::getId).orElse(Actor.SYSTEM_USERNAME);
		return new UnauthorizedException(String.format(PROFILE_RIGHT_ASSIGNABLE_MESSAGE, profileRightAssignable.getId(), creator));
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
