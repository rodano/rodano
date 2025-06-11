package ch.rodano.core.services.bll.actor;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Attributable;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.robot.RobotDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;

@Service
public class ActorServiceImpl implements ActorService {

	private final StudyService studyService;
	private final UserDAOService userDAOService;
	private final RobotDAOService robotDAOService;
	private final ScopeService scopeService;
	private final ScopeDAOService scopeDAOService;
	private final RoleService roleService;

	public ActorServiceImpl(
		final StudyService studyService,
		final UserDAOService userDAOService,
		final RobotDAOService robotDAOService,
		final ScopeService scopeService,
		final RoleService roleService,
		final ScopeDAOService scopeDAOService
	) {
		this.studyService = studyService;
		this.userDAOService = userDAOService;
		this.robotDAOService = robotDAOService;
		this.scopeService = scopeService;
		this.scopeDAOService = scopeDAOService;
		this.roleService = roleService;
	}

	@Override
	public Actor getActor(final Role role) {
		if(role.getUserFk() != null) {
			return userDAOService.getUserByPk(role.getUserFk());
		}
		return robotDAOService.getRobotByPk(role.getRobotFk());
	}

	@Override
	public String[] getLanguages(final Actor actor) {
		final var defaultLanguageId = studyService.getStudy().getDefaultLanguageId();
		if(actor != null) {
			final var languageId = actor.getLanguageId();
			if(languageId != null && !languageId.equals(defaultLanguageId)) {
				return new String[] { languageId, defaultLanguageId };
			}
		}

		return new String[] { defaultLanguageId };
	}

	@Override
	public List<Profile> getActiveProfiles(final Actor actor) {
		return roleService.getActiveRoles(actor).stream().map(Role::getProfile).toList();
	}

	@Override
	public List<Profile> getActiveProfiles(final Actor actor, final Scope scope) {
		return roleService.getActiveRoles(actor, scope).stream().map(Role::getProfile).toList();
	}

	@Override
	public List<Scope> getRootScopes(final Actor actor) {
		final var scopePks = roleService.getActiveRoles(actor).stream().map(Role::getScopeFk).toList();
		return scopeDAOService.getScopesByPks(scopePks);
	}

	@Override
	public List<Scope> getRootScopes(final Actor actor, final Profile profile) {
		final var scopePks = roleService.getActiveRoles(actor).stream()
			.filter(r -> r.getProfileId().equals(profile.getId()))
			.map(Role::getScopeFk).toList();
		return scopeDAOService.getScopesByPks(scopePks);
	}

	@Override
	public List<Scope> getRootScopes(final Actor actor, final FeatureStatic feature) {
		final var scopePks = roleService.getActiveRoles(actor, feature).stream().map(Role::getScopeFk).toList();
		return scopeDAOService.getScopesByPks(scopePks);
	}

	@Override
	public List<Scope> getRootScopes(final Actor actor, final RightAssignable<?> rightAssignable, final Rights right) {
		final var scopePks = roleService.getActiveRoles(actor, rightAssignable, right).stream().map(Role::getScopeFk).toList();
		return scopeDAOService.getScopesByPks(scopePks);
	}

	@Override
	public List<Scope> getRootScopes(final Actor actor, final Attributable<?> attributable) {
		final var scopePks = roleService.getActiveRoles(actor, attributable).stream().map(Role::getScopeFk).toList();
		return scopeDAOService.getScopesByPks(scopePks);
	}

	@Override
	public Optional<Scope> getRootScope(final Actor actor) {
		final var scopes = getRootScopes(actor);
		return findHighestScopeInHierarchy(scopes);
	}

	@Override
	public Optional<Scope> getRootScope(final Actor actor, final Profile profile) {
		final var scopes = getRootScopes(actor, profile);
		return findHighestScopeInHierarchy(scopes);
	}

	@Override
	public Optional<Scope> getRootScope(final Actor actor, final FeatureStatic feature) {
		final var scopes = getRootScopes(actor, feature);
		return findHighestScopeInHierarchy(scopes);
	}

	@Override
	public Optional<Scope> getRootScope(final Actor actor, final RightAssignable<?> rightAssignable, final Rights right) {
		final var scopes = getRootScopes(actor, rightAssignable, right);
		return findHighestScopeInHierarchy(scopes);
	}

	@Override
	public Optional<Scope> getRootScope(final Actor actor, final Attributable<?> attributable) {
		final var scopes = getRootScopes(actor, attributable);
		return findHighestScopeInHierarchy(scopes);
	}

	private Optional<Scope> findHighestScopeInHierarchy(final List<Scope> scopes) {
		return scopes.stream().sorted((s1, s2) -> ScopeModel.COMPARATOR_DEPTH.compare(s1.getScopeModel(), s2.getScopeModel())).findFirst();
	}

	@Override
	public Optional<Timeframe> getTimeframeForScope(final Actor actor, final Scope scope, final List<Role> scopeRoles) {
		//do not call this method with all the roles of the user
		if(scopeRoles.isEmpty()) {
			return Optional.empty();
		}
		final var timeframe = getRealTimeframeForScope(scope, scopeRoles);
		//adjust the timeframe to the business
		//a user who has the right on a scope has the right from the beginning of the scope
		//think about an investigator who is in charge of a transferred patient: he needs to see the history of the patient
		return timeframe.map(Timeframe::withInfiniteStartDate);
	}

	private Optional<Timeframe> getRealTimeframeForScope(final Scope scope, final List<Role> scopeRoles) {
		//do not call this method with all the roles of the user
		final var ancestorPks = scopeRoles.stream().map(Role::getScopeFk).toList();
		final var ancestors = scopeDAOService.getScopesByPks(ancestorPks);
		//if one ancestor is virtual, this gives the full right on the scope
		//this works only if the list scopeRoles contains only the roles that gives a right on the scope parameter
		if(ancestors.stream().anyMatch(s -> s.getScopeModel().isVirtual())) {
			return Optional.of(Timeframe.INFINITE_TIMEFRAME);
		}
		Optional<Timeframe> timeframe = Optional.empty();
		for(final var ancestor : ancestors) {
			final var ancestorTimeframe = scopeService.getRelationTimeframe(ancestor, scope);
			if(ancestorTimeframe.isPresent()) {
				//initialize timeframe
				if(timeframe.isEmpty()) {
					timeframe = ancestorTimeframe;
				}
				timeframe = timeframe.map(t -> t.withExtension(ancestorTimeframe.get()));
			}
		}
		return timeframe;
	}
}
