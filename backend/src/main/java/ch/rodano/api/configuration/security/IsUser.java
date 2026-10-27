package ch.rodano.api.configuration.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Allow only requests authenticated as a user, rejecting robots in particular
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("principal instanceof T(ch.rodano.core.model.user.User)")
public @interface IsUser {
	//no content for annotation
}
