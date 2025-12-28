package ch.rodano.core.database.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.helpers.UserCreatorService;
import ch.rodano.core.helpers.builder.UserBuilder;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.dao.role.RoleDAOService;

@Service
public class DemoUsersInitializer {

	private static final String RATIONALE = "Demo user initialization";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final ScopeService scopeService;
	private final UserService userService;
	private final UserCreatorService userCreatorService;
	private final UserSecurityService userSecurityService;
	private final RoleDAOService roleDAOService;

	public DemoUsersInitializer(
		final StudyService studyService,
		final ScopeService scopeService,
		final UserCreatorService userCreatorService,
		final UserSecurityService userSecurityService,
		final UserService userService,
		final RoleDAOService roleDAOService
	) {
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.userService = userService;
		this.userCreatorService = userCreatorService;
		this.userSecurityService = userSecurityService;
		this.roleDAOService = roleDAOService;
	}

	@Transactional
	public void initialize(final String baseEmail, final String password, final DatabaseActionContext context) {
		final var root = scopeService.getRootScope();
		final var study = studyService.getStudy();
		final var encodedPassword = userSecurityService.encodePassword(password);
		final var emailParts = baseEmail.split("@");

		for(final var profile : study.getProfiles()) {
			final var profileId = profile.getId().toLowerCase();
			final var email = String.format("%s+%s@%s", emailParts[0], profileId, emailParts[1]);

			final var existingUser = userService.getUserByEmail(email);

			if(existingUser == null) {
				logger.info("Creating new demo user: {}", email);
				final var userAndRoles = UserBuilder.createUser(profile.getDefaultLocalizedShortname() + " Demo", email)
					.setHashedPassword(encodedPassword)
					.addRole(root, profile)
					.getUserAndRoles();
				userCreatorService.createAndEnable(userAndRoles, context);
			}
			else {
				logger.info("User {} already exists, adding role for project {}", email, study.getId());

				final var role = new Role();
				role.setProjectId(study.getProjectId());
				role.setProfile(profile);
				role.setScopeFk(root.getPk());
				role.setUserFk(existingUser.getPk());
				role.setStatus(RoleStatus.ENABLED);

				roleDAOService.saveRole(role, context, RATIONALE);
			}
		}
	}
}
