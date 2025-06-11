package ch.rodano.core.database.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.helpers.UserCreatorService;
import ch.rodano.core.helpers.builder.UserBuilder;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.user.UserService;

@Service
public class DemoUsersInitializer {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final ScopeService scopeService;
	private final UserService userService;
	private final UserCreatorService userCreatorService;
	private final UserSecurityService userSecurityService;

	public DemoUsersInitializer(
		final StudyService studyService,
		final ScopeService scopeService,
		final UserCreatorService userCreatorService,
		final UserSecurityService userSecurityService,
		final UserService userService
	) {
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.userService = userService;
		this.userCreatorService = userCreatorService;
		this.userSecurityService = userSecurityService;
	}

	@Transactional
	public void initialize(final String baseEmail, final String password, final DatabaseActionContext context) {
		final var root = scopeService.getRootScope();
		final var study = studyService.getStudy();
		final var encodedPassword = userSecurityService.encodePassword(password);
		final var emailParts = baseEmail.split("@");

		for(final var profile : study.getProfiles()) {
			final var profileId = profile.getId().toLowerCase();
			final var email = String.format("%s+%s-%s@%s", emailParts[0], study.getId().toLowerCase(), profileId, emailParts[1]);
			if(userService.getUserByEmail(email) == null) {
				final var userAndRoles = UserBuilder.createUser(profile.getDefaultLocalizedShortname() + " Demo", email)
					.setHashedPassword(encodedPassword)
					.addRole(root, profile)
					.getUserAndRoles();
				userCreatorService.createAndEnable(userAndRoles, context);
			}
			else {
				logger.info("User {} already exists", email);
			}
		}
	}
}
