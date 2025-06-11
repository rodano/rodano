package ch.rodano.core.services.dao.user;

import java.util.NavigableSet;
import java.util.Optional;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.UserAuditTrail;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.user.UserSearch;

public interface UserDAOService {

	/**
	 * Create or update a user
	 *
	 * @param user    The user to create or update
	 * @param context The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void saveUser(User user, DatabaseActionContext context, String rationale);

	void deleteUser(User user, DatabaseActionContext context, String rationale);

	void restoreUser(User user, DatabaseActionContext context, String rationale);

	/**
	 * Find a user by its email or throw an exception if none is found
	 *
	 * @param email The email of the user
	 * @return The user with the e-mail
	 */
	User getUserByEmail(String email);

	/**
	 * Check if a user with the provided pending e-mail exists
	 * @param pendingEmail The e-mail of the pending user
	 * @return The user with the e-mail or null
	 */
	User getUserByPendingEmail(String pendingEmail);

	User getUserByActivationCode(String activationCode);

	User getUserByVerificationCode(String verificationCode);

	User getUserByRecoveryCode(String recoveryCode);

	User getUserByResetCode(String resetCode);

	User getUserByPk(Long pk);

	/**
	 * Find all users who correspond to the given predicate
	 *
	 * @param search The predicate to find users
	 * @return A paged results of users
	 */
	PagedResult<User> search(UserSearch search);

	NavigableSet<UserAuditTrail> getAuditTrails(User user, Optional<Timeframe> empty, Optional<Long> actorPk);

}
