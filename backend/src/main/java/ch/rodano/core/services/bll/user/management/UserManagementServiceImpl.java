package ch.rodano.core.services.bll.user.management;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.api.user.management.CreateUserRequest;
import ch.rodano.api.user.management.UpdateUserRequest;
import ch.rodano.api.user.management.UserManagementDTO;
import ch.rodano.core.constants.SystemConstants;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.user.UserDAOService;

@Service
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

	private final UserDAOService userDAOService;
	private final MailService mailService;
	private final AuditActionService auditActionService;
	private final UserSecurityService userSecurityService;

	public UserManagementServiceImpl(final UserDAOService userDAOService,
									 final MailService mailService,
									 final AuditActionService auditActionService,
									 final UserSecurityService userSecurityService) {
		this.userDAOService = userDAOService;
		this.mailService = mailService;
		this.auditActionService = auditActionService;
		this.userSecurityService = userSecurityService;
	}

	/**
	 * Create audit context inside the transaction if none provided
	 */
	private DatabaseActionContext getOrCreateContext(final DatabaseActionContext providedContext, final String rationale) {
		if(providedContext != null) {
			return providedContext;
		}

		return auditActionService.createAuditActionAndGenerateContext(
			Optional.of(userSecurityService.getCurrentUser()),
			rationale,
			ZonedDateTime.now(),
			SystemConstants.SYSTEM_PROJECT_ID
		);
	}

	@Override
	public List<UserManagementDTO> getAllUsers() {
		return userDAOService.getAllUsersForManagement();
	}

	@Override
	public UserManagementDTO getUser(final Long userPk) {
		final var user = userDAOService.getUserByPk(userPk);
		if(user == null) {
			throw new NotFoundException("User not found with pk: " + userPk);
		}
		return mapToDTO(user);
	}

	@Override
	public UserManagementDTO createUser(final CreateUserRequest request,
										final String contextUrl,
										final DatabaseActionContext providedContext) {

		final var context = getOrCreateContext(providedContext, "User created by superuser");

		final var existingUser = userDAOService.getUserByEmail(request.email());
		if(existingUser != null) {
			throw new IllegalArgumentException("User already exists with email: " + request.email());
		}

		final var user = new User();
		user.setName(request.name());
		user.setEmail(request.email());
		user.setSuperuser(request.isSuperuser());
		user.setLanguageId(request.languageId() != null ? request.languageId() : "en");
		user.setActivated(false);
		user.setActivationCode(UUID.randomUUID().toString());

		final var contextTime = context.auditAction().getDate();
		user.setCreationTime(contextTime);
		user.setLastUpdateTime(contextTime);
		user.setDeleted(false);
		user.setExternallyManaged(false);

		userDAOService.saveUser(user, context, "User created by superuser");

		if(request.sendActivationEmail()) {
			mailService.sendUserCreationInvitation(user, contextUrl, context);
		}

		return mapToDTO(user);
	}

	@Override
	public UserManagementDTO updateUser(final Long userPk, final UpdateUserRequest request, final DatabaseActionContext providedContext) {
		final var context = getOrCreateContext(providedContext, "User updated by superuser");

		final var user = userDAOService.getUserByPk(userPk);
		if(user == null) {
			throw new NotFoundException("User not found with pk: " + userPk);
		}

		user.setName(request.name());
		user.setLanguageId(request.languageId());
		user.setSuperuser(request.isSuperuser());
		user.setLastUpdateTime(context.auditAction().getDate());
		user.setPhone(request.phone());

		userDAOService.saveUser(user, context, "User updated by superuser");

		return mapToDTO(user);
	}

	@Override
	public void deleteUser(final Long userPk, final DatabaseActionContext providedContext) {
		final var context = getOrCreateContext(providedContext, "User deleted by superuser");

		final var user = userDAOService.getUserByPk(userPk);
		if(user == null) {
			throw new NotFoundException("User not found with pk: " + userPk);
		}

		userDAOService.deleteUser(user, context, "User deleted by superuser");
	}

	@Override
	public void restoreUser(final Long userPk, final DatabaseActionContext providedContext) {
		final var context = getOrCreateContext(providedContext, "User restored by superuser");

		final var user = userDAOService.getUserByPk(userPk);
		if(user == null) {
			throw new NotFoundException("User not found with pk: " + userPk);
		}

		userDAOService.restoreUser(user, context, "User restored by superuser");
	}

	@Override
	public void toggleSuperuser(final Long userPk, final boolean isSuperuser, final DatabaseActionContext providedContext) {
		final var context = getOrCreateContext(providedContext, isSuperuser ? "Granted superuser status" : "Revoked superuser status");

		final var user = userDAOService.getUserByPk(userPk);
		if(user == null) {
			throw new NotFoundException("User not found with pk: " + userPk);
		}

		user.setSuperuser(isSuperuser);
		user.setLastUpdateTime(context.auditAction().getDate());

		userDAOService.saveUser(user, context, isSuperuser ? "Granted superuser status" : "Revoked superuser status");
	}

	@Override
	@Transactional
	public void unblockUser(final Long userPk, final DatabaseActionContext providedContext) {
		final var user = userDAOService.getUserByPk(userPk);
		if(user == null) {
			throw new NotFoundException("User not found with pk: " + userPk);
		}

		final var context = getOrCreateContext(providedContext, "Unblock user: " + user.getEmail());

		user.setPasswordAttempts(0);
		user.setLoginBlockingDate(null);
		userDAOService.saveUser(user, context, "User unblocked by superuser");
	}

	private UserManagementDTO mapToDTO(final User user) {
		return new UserManagementDTO(
			user.getPk(),
			user.getName(),
			user.getEmail(),
			user.isSuperuser(),
			user.isActivated(),
			user.getDeleted(),
			user.isExternallyManaged(),
			user.getPasswordAttempts() >= UserSecurityService.PASSWORD_MAX_ATTEMPTS,
			StringUtils.isNotEmpty(user.getPassword()),
			user.getCreationTime(),
			user.getLastUpdateTime(),
			user.getLoginDate(),
			user.getLanguageId(),
			user.getPhone()
		);
	}
}
