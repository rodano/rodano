package ch.rodano.core.services.bll.user;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.user.UserSearch;

public interface UserService {

	int PENDING_EMAIL_EXPIRY_LIMIT_IN_DAYS = 7;

	User getUserByPk(Long userPk);

	User getUserByEmail(String email);

	User getUserByPendingEmail(String pendingEmail);

	User getUserByVerificationCode(String verificationCode);

	User getUserByRecoveryCode(String recoveryCode);

	/**
	 * Create a new user
	 *
	 * @param user       The user object
	 * @param profile
	 * @param roleScope
	 * @param contextURL The context URL for user activation link construction
	 * @param context    Database action context
	 * @return New user
	 */
	User createUser(
		User user,
		Profile profile,
		Scope roleScope,
		String contextURL,
		DatabaseActionContext context
	);

	/**
	 * Save user
	 * @param user      The user object
	 * @param context   Database action context
	 * @param rationale The rationale for the operation
	 */
	void saveUser(User user, DatabaseActionContext context, String rationale);

	void deleteUser(User user, DatabaseActionContext context, String rationale);

	void restoreUser(User user, DatabaseActionContext context, String rationale);

	/**
	 * Fetch the users to which the current user has rights and using the given predicate
	 * @param search     The predicate for the user search
	 * @param actor         The actor
	 * @return              A collection of users
	 */
	PagedResult<User> search(UserSearch search, Actor actor, List<Role> roles);

	/**
	 * Check that an e-mail is available, i.e. that it is not used by a user
	 * @param email
	 */
	void checkEmailCanBeUsed(String email);

	void verifyUserEmail(String verificationCode, DatabaseActionContext context);

	// TODO 2FA
	//void enableTwoStep(User user) throws WrongDataConditionException;

	//void disableTwoStep(User user) throws WrongDataConditionException;

	User getUser(Resource resource);

	User getUser(Role role);

	/**
	 * Export the users in a CSV file
	 *
	 * @param out        The output stream where the CSV will be written
	 * @param search  The predicate for the user search
	 * @param actor      The actor
	 * @param roles      The actor roles
	 * @param languages  The languages
	 */
	void exportUsers(OutputStream out, UserSearch search, Actor actor, List<Role> roles, String[] languages) throws IOException;
}
