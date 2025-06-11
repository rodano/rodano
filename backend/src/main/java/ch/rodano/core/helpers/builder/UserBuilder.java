package ch.rodano.core.helpers.builder;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.helpers.UserCreatorService;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;

public class UserBuilder {
	private final UserCreatorService.UserCreation userAndRoles;

	private UserBuilder(final User user) {
		this.userAndRoles = new UserCreatorService.UserCreation(user, new ArrayList<>());
	}

	public static UserBuilder generateRandomUser() {
		final var user = new User();
		user.setName(RandomStringUtils.random(10));
		user.setEmail(generateEmail());
		return new UserBuilder(user);
	}

	public static UserBuilder createUser(final String name, final String email) {
		final var user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setActivated(true);
		return new UserBuilder(user);
	}

	public UserBuilder setHashedPassword(final String hashedPassword) {
		userAndRoles.user().setPassword(hashedPassword);
		userAndRoles.user().setPasswordChangedDate(ZonedDateTime.now());
		return this;
	}

	public UserBuilder setLanguage(final LanguageStatic language) {
		userAndRoles.user().setLanguageId(language.name());
		return this;
	}

	public UserBuilder setPhone(final String phone) {
		userAndRoles.user().setPhone(phone);
		return this;
	}

	public UserBuilder addRole(final Scope scope, final Profile profile) {
		userAndRoles.roles().add(new ImmutablePair<>(profile, scope));
		return this;
	}

	public UserCreatorService.UserCreation getUserAndRoles() {
		return userAndRoles;
	}

	private static String generateEmail() {
		return RandomStringUtils.randomAlphanumeric(10) + "@" + RandomStringUtils.randomAlphanumeric(7) + ".com";
	}
}
