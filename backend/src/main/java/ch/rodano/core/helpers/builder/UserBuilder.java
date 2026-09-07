package ch.rodano.core.helpers.builder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;

public class UserBuilder {

	private final User user;
	private final List<Pair<Profile, Scope>> roles = new ArrayList<>();

	private UserBuilder(final User user) {
		this.user = user;
	}

	public static UserBuilder createUser(final String name, final String email) {
		final var user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setActivated(true);
		return new UserBuilder(user);
	}

	public UserBuilder setHashedPassword(final String hashedPassword) {
		user.setPassword(hashedPassword);
		user.setPasswordChangedDate(ZonedDateTime.now());
		return this;
	}

	public UserBuilder setLanguage(final LanguageStatic language) {
		user.setLanguageId(language.name());
		return this;
	}

	public UserBuilder setPhone(final String phone) {
		user.setPhone(phone);
		return this;
	}

	public UserBuilder addRole(final Scope scope, final Profile profile) {
		roles.add(new ImmutablePair<>(profile, scope));
		return this;
	}

	public User getUser() {
		return user;
	}

	public List<Pair<Profile, Scope>> getRoles() {
		return roles;
	}
}
