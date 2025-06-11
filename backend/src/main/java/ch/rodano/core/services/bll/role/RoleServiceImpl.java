package ch.rodano.core.services.bll.role;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Attributable;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.workflow.WorkflowAction;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.rule.RuleService;

@Service
public class RoleServiceImpl implements RoleService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final RoleDAOService roleDAOService;
	private final StudyService studyService;
	private final RuleService ruleService;
	private final ScopeDAOService scopeDAOService;
	private final MailService mailService;

	public RoleServiceImpl(
		final StudyService studyService,
		final RoleDAOService roleDAOService,
		final RuleService ruleService,
		final ScopeDAOService scopeService,
		final MailService mailService
	) {
		this.studyService = studyService;
		this.roleDAOService = roleDAOService;
		this.ruleService = ruleService;
		this.scopeDAOService = scopeService;
		this.mailService = mailService;
	}

	@Override
	public List<Role> getRoles(final Actor actor) {
		if(actor instanceof User) {
			return roleDAOService.getRolesByUserPk(actor.getPk());
		}
		return roleDAOService.getRolesByRobotPk(actor.getPk());
	}

	// TODO rename this to getEnabledRoles
	@Override
	public List<Role> getActiveRoles(final Actor actor) {
		return getRoles(actor).stream().filter(Role::isEnabled).toList();
	}

	@Override
	public List<Role> getActiveRoles(final Actor actor, final Scope scope) {
		if(actor instanceof User) {
			return roleDAOService.getActiveRolesByUserPkOverScopePk(actor.getPk(), scope.getPk());
		}
		return roleDAOService.getActiveRolesByRobotPkOverScopePk(actor.getPk(), scope.getPk());
	}

	@Override
	public List<Role> getActiveRoles(final Actor actor, final FeatureStatic feature) {
		return getActiveRoles(actor).stream().filter(r -> r.getProfile().hasRight(feature)).toList();
	}

	@Override
	public List<Role> getActiveRoles(final Actor actor, final String featureId) {
		return getActiveRoles(actor).stream().filter(r -> r.getProfile().hasFeature(featureId)).toList();
	}

	@Override
	public List<Role> getRoles(final Actor actor, final FeatureStatic feature) {
		return getRoles(actor).stream().filter(r -> r.getProfile().hasRight(feature)).toList();
	}

	@Override
	public List<Role> getActiveRoles(final Actor actor, final RightAssignable<?> rightAssignable, final Rights right) {
		return getActiveRoles(actor).stream()
			.filter(r -> r.getProfile().hasRight(rightAssignable, right))
			.toList();
	}

	@Override
	public List<Role> getActiveRoles(final Actor actor, final Attributable<?> attributable) {
		return getActiveRoles(actor).stream()
			.filter(r -> r.getProfile().hasRight(attributable))
			.toList();
	}

	@Override
	public List<Role> getRoles(final Scope scope, final Profile profile) {
		return roleDAOService.getRolesByScopePkAndProfiles(scope.getPk(), Collections.singleton(profile.getId()));
	}

	@Override
	public List<Role> getActiveRoles(final Scope scope, final Profile profile) {
		return getRoles(scope, profile).stream().filter(Role::isEnabled).toList();
	}

	@Override
	public Role createRoleWithoutNotification(
		final Actor actor,
		final Profile profile,
		final Scope scope,
		final DatabaseActionContext context
	) {
		final var role = new Role();
		role.setProfile(profile);

		if(actor instanceof User) {
			role.setUserFk(actor.getPk());
		}
		else if(actor instanceof Robot) {
			role.setRobotFk(actor.getPk());
		}
		else {
			throw new UnsupportedOperationException("Can not create a role for an actor that is not a user or a robot");
		}

		role.setScopeFk(scope.getPk());
		roleDAOService.saveRole(role, context, "Create role");

		// Rules
		final var rules = studyService.getStudy().getEventActions().get(WorkflowAction.ROLE_CREATE);
		if(rules != null && !rules.isEmpty()) {
			final var state = new DataState(scope);
			ruleService.execute(state, rules, context);
		}

		logger.info("Create role {} on {} for actor {}", role.getProfileId(), scope.getCodeAndShortname(), actor.getName());

		return role;
	}

	@Override
	public Role createRole(
		final User user,
		final Profile profile,
		final Scope scope,
		final DatabaseActionContext context
	) {
		final var role = createRoleWithoutNotification(
			user,
			profile,
			scope,
			context
		);

		// Send a role creation confirmation e-mail for the user
		// If the user is creating a role on themselves, no role creation notification is needed and the role is enabled directly
		if(context.actor().isPresent() && context.actor().get().getPk().equals(user.getPk())) {
			enableRole(
				user,
				role,
				context
			);
		}
		else {
			mailService.sendRoleCreationConfirmation(
				user,
				role,
				context
			);
		}

		return role;
	}

	@Override
	public void updateRole(final Role role, final DatabaseActionContext context, final String rationale) {
		roleDAOService.saveRole(role, context, rationale);
	}

	@Override
	public void enableRoleWithoutNotification(
		final Actor actor,
		final Role role,
		final DatabaseActionContext context
	) {
		if(role.getStatus() == RoleStatus.ENABLED) {
			throw new UnsupportedOperationException("Unable to enable to role that is already enabled");
		}

		//do this only for a role on a user (not on a robot)
		if(actor instanceof final User user) {
			if(!user.isExternallyManaged() && !user.isActivated()) {
				throw new UnsupportedOperationException("The user's role cannot be enabled because the user has not been activated yet");
			}
		}

		// Reset the pin code and set the role status to enabled
		role.enable();
		roleDAOService.saveRole(role, context, "Enable role");

		// Retrieve the scope of the role
		final var scope = scopeDAOService.getScopeByPk(role.getScopeFk());

		// Rules
		final var rules = studyService.getStudy().getEventActions().get(WorkflowAction.ROLE_ENABLE);
		if(rules != null && !rules.isEmpty()) {
			final var state = new DataState(scope);
			ruleService.execute(state, rules, context);
		}

		logger.info("Enable role {} on {} for actor {}", role.getProfileId(), scope.getCodeAndShortname(), actor.getName());
	}

	@Override
	public void enableRole(
		final User user,
		final Role role,
		final DatabaseActionContext context
	) {
		enableRoleWithoutNotification(
			user,
			role,
			context
		);

		// Send a role enable confirmation e-mail for the user
		mailService.sendRoleEnableConfirmation(
			user,
			role,
			context
		);
	}

	@Override
	public void disableRole(
		final Actor actor,
		final Role role,
		final DatabaseActionContext context
	) {
		if(role.getStatus() == RoleStatus.DISABLED) {
			throw new UnsupportedOperationException("Unable to disable a role that is already disabled");
		}

		//disallow disabling the last role
		final var enabledRoles = getActiveRoles(actor);
		if(enabledRoles.size() == 1 && enabledRoles.get(0).equals(role)) {
			throw new UnsupportedOperationException("User must have at least one active role");
		}

		role.disable();
		roleDAOService.saveRole(role, context, "Disable role");

		//retrieve the scope of the role
		final var scope = scopeDAOService.getScopeByPk(role.getScopeFk());

		// Rules
		executeRoleDisableRules(scope, context);

		logger.info("Disable role {} on {} for actor {}", role.getProfileId(), scope.getCodeAndShortname(), actor.getName());
	}

	@Override
	public void rejectRole(
		final Actor actor,
		final Role role,
		final DatabaseActionContext context
	) {
		if(role.getStatus() != RoleStatus.PENDING) {
			throw new UnsupportedOperationException("Unable to reject a role that is not pending");
		}

		role.disable();
		roleDAOService.saveRole(role, context, "Reject role");

		//retrieve the scope of the role
		final var scope = scopeDAOService.getScopeByPk(role.getScopeFk());

		logger.info("Rejected role {} on {} by actor {}", role.getProfileId(), scope.getCodeAndShortname(), actor.getName());
	}

	private void executeRoleDisableRules(
		final Scope scope,
		final DatabaseActionContext context
	) {
		// Rules
		final var rules = studyService.getStudy().getEventActions().get(WorkflowAction.ROLE_DISABLE);
		if(rules != null && !rules.isEmpty()) {
			final var state = new DataState(scope);
			ruleService.execute(state, rules, context);
		}
	}

}
