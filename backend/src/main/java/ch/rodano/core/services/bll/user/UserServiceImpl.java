package ch.rodano.core.services.bll.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import ch.rodano.api.controller.user.exception.EmailAlreadyUsedException;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.exception.InvalidOneUseCodeException;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeExtension;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.user.UserSearch;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.utils.RightsService;

@Service
public class UserServiceImpl implements UserService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final UserDAOService userDAOService;
	private final StudyService studyService;
	private final ScopeDAOService scopeDAOService;
	private final ScopeService scopeService;
	private final RoleService roleService;
	private final RightsService rightsService;
	private final MailService mailService;

	public UserServiceImpl(
		final UserDAOService userDAOService,
		final StudyService studyService,
		final ScopeDAOService scopeDAOService,
		final ScopeService scopeService,
		final RoleService roleService,
		final RightsService rightsService,
		final MailService mailService
	) {
		this.userDAOService = userDAOService;
		this.studyService = studyService;
		this.scopeDAOService = scopeDAOService;
		this.scopeService = scopeService;
		this.roleService = roleService;
		this.rightsService = rightsService;
		this.mailService = mailService;
	}

	@Override
	public User getUserByPk(final Long userPk) {
		return userDAOService.getUserByPk(userPk);
	}

	@Override
	public User getUserByEmail(final String email) {
		return userDAOService.getUserByEmail(email);
	}

	@Override
	public User getUserByPendingEmail(final String pendingEmail) {
		return userDAOService.getUserByPendingEmail(pendingEmail);
	}

	@Override
	public User getUserByRecoveryCode(final String recoveryCode) {
		return userDAOService.getUserByRecoveryCode(recoveryCode);
	}

	@Override
	public User getUserByVerificationCode(final String verificationCode) {
		return userDAOService.getUserByVerificationCode(verificationCode);
	}

	@Override
	public void checkEmailCanBeUsed(final String email) {
		mailService.checkEmailAddress(email);

		//check that e-mail is not already in use
		if(userDAOService.getUserByEmail(email) != null) {
			throw new EmailAlreadyUsedException();
		}
		if(userDAOService.getUserByPendingEmail(email) != null) {
			throw new EmailAlreadyUsedException();
		}
	}

	@Override
	public User createUser(
		final User user,
		final Profile profile,
		final Scope roleScope,
		final String contextURL,
		final DatabaseActionContext context
	) {
		checkEmailCanBeUsed(user.getEmail());

		// if the user is externally managed, activate them immediately, else set a new activation code
		if(user.isExternallyManaged()) {
			user.setActivated(true);
		}
		else {
			user.setActivationCode(UUID.randomUUID().toString());
		}

		// save the user
		userDAOService.saveUser(user, context, "Create user");

		// create the user role
		final var role = roleService.createRoleWithoutNotification(
			user, profile,
			roleScope,
			context
		);

		// if the user is externally managed, enable the role immediately
		if(user.isExternallyManaged()) {
			roleService.enableRole(
				user,
				role,
				context
			);
		}

		// log the user creation
		logger.info("Created user {} with role {} on scope {} by {}", user.getEmail(), role.getProfileId(), roleScope.getCodeAndShortname(), context.getActorName());

		// send an activation e-mail if a user is not externally managed
		if(!user.isExternallyManaged()) {
			mailService.sendUserAccountActivationInvitation(
				user,
				role,
				contextURL,
				context
			);
		}

		return user;
	}

	@Override
	public void saveUser(
		final User user,
		final DatabaseActionContext context,
		final String rationale
	) {
		userDAOService.saveUser(user, context, rationale);
	}

	@Override
	public void deleteUser(
		final User user,
		final DatabaseActionContext context,
		final String rationale
	) {
		user.setActivated(false);
		userDAOService.deleteUser(user, context, rationale);
	}

	@Override
	public void restoreUser(final User user, final DatabaseActionContext context, final String rationale) {
		user.setActivated(true);
		userDAOService.restoreUser(user, context, rationale);
	}

	@Override
	public PagedResult<User> search(final UserSearch search, final Actor actor, final List<Role> roles) {
		//mutate scope pks in the predicate according to rights
		if(search.getScopePks().isEmpty()) {
			final var scopePks = roles.stream()
				.map(Role::getScopeFk)
				.collect(Collectors.toSet());
			search.setScopePks(Optional.of(scopePks));
		}
		else {
			final var scopes = scopeDAOService.getScopesByPks(search.getScopePks().get());
			for(final var scope : scopes) {
				rightsService.checkRight(actor, roles, scope);
			}
		}

		search.setExtension(Optional.of(ScopeExtension.BRANCH));

		if(search.getProfileIds().isEmpty()) {
			final var profileIds = roles.stream()
				.map(Role::getProfile)
				.flatMap(p -> p.getProfiles(Rights.READ).stream())
				.map(Profile::getId)
				.collect(Collectors.toSet());
			search.setProfileIds(Optional.of(profileIds));
		}
		else {
			final var study = studyService.getStudy();
			final var profiles = search.getProfileIds().get().stream()
				.map(study::getProfile)
				.collect(Collectors.toSet());
			for(final var profile : profiles) {
				rightsService.checkRight(actor, roles, profile, Rights.READ);
			}
		}

		return userDAOService.search(search);
	}

	@Override
	public void verifyUserEmail(final String verificationCode, final DatabaseActionContext context) {
		final var user = getUserByVerificationCode(verificationCode);
		// HTTP exception is thrown here but OK
		if(user == null) {
			throw new InvalidOneUseCodeException();
		}

		user.setEmail(user.getPendingEmail());
		user.setPendingEmail(null);
		user.setEmailModificationDate(null);
		user.setEmailVerificationCode(null);
		saveUser(user, context, "User email verified");
	}

	// TODO 2FA
	/*
	@Override
	public void enableTwoStep(final User user) throws WrongDataConditionException {
		if(user.getUserData().isTwoStep()) {
			throw new WrongDataConditionException("Two step is already enabled");
		}

		if(user.getUserData().getEncryptedTwoStepKey() == null || user.getUserData().getOneUseTwoStepCodes().isEmpty()) {
			throw new WrongDataConditionException("Two step key and one use codes must have been generated before enabling two step");
		}

		user.getUserData().setTwoStep(true);
	}

	@Override
	public void disableTwoStep(final User user) throws WrongDataConditionException {
		if(!user.getUserData().isTwoStep()) {
			throw new WrongDataConditionException("Two step is already disabled");
		}

		user.getUserData().getOneUseTwoStepCodes().clear();
		user.getUserData().getTwoStepTrustedClients().clear();
		user.getUserData().setEncryptedTwoStepKey((String) null);
		user.getUserData().setTwoStep(false);
	}
	 */

	@Override
	public User getUser(final Resource resource) {
		return getUserByPk(resource.getUserFk());
	}

	@Override
	public User getUser(final Role role) {
		if(role.getUserFk() != null) {
			return userDAOService.getUserByPk(role.getUserFk());
		}
		return null;
	}

	@Override
	public void exportUsers(final OutputStream out, final UserSearch search, final Actor actor, final List<Role> roles, final String[] languages) throws IOException {
		//TODO create a custom jOOQ queries to gather all the details that needs to be exported
		final var users = search(search, actor, roles).getObjects();

		try(var writer = new CSVWriter(new OutputStreamWriter(out))) {

			final var header = new String[] {
				"Name",
				"Phone",
				"E-mail",
				"Profile",
				"Scope",
				"Status",
				"Removed"
			};
			writer.writeNext(header);

			for(final var user : users) {
				final var userRoles = roleService.getRoles(user);

				for(final var role : userRoles) {
					final var row = new String[header.length];
					var i = 0;
					row[i++] = user.getName();
					row[i++] = StringUtils.defaultString(user.getPhone());
					row[i++] = user.getEmail();

					row[i++] = role.getProfile().getLocalizedShortname(languages);
					row[i++] = scopeService.get(role).getCodeAndShortname();
					row[i++] = role.getStatus().getLabel();
					row[i++] = Boolean.toString(user.getDeleted());
					writer.writeNext(row);
				}
			}
		}
	}
}
