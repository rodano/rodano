package ch.rodano.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Assignable;
import ch.rodano.configuration.model.rights.Attributable;
import ch.rodano.configuration.model.rights.ProfileRightAssignable;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.study.StudyService;

import static ch.rodano.core.model.jooq.Tables.ROLE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;

@Service
public class RightsServiceImpl implements RightsService {

	private final DSLContext create;
	private final StudyService studyService;
	private final ScopeRelationService scopeRelationService;
	private final RoleService roleService;

	public RightsServiceImpl(
		final StudyService studyService,
		final ScopeRelationService scopeRelationService,
		final RoleService roleService,
		final DSLContext create
	) {
		this.create = create;
		this.studyService = studyService;
		this.scopeRelationService = scopeRelationService;
		this.roleService = roleService;
	}

	@Override
	public ACL getACL(final Actor actor) {
		final var conditions = new ArrayList<Condition>();
		if(actor instanceof User) {
			conditions.add(ROLE.USER_FK.eq(actor.getPk()));
		}
		else {
			conditions.add(ROLE.ROBOT_FK.eq(actor.getPk()));
		}
		conditions.add(ROLE.STATUS.eq(RoleStatus.ENABLED));

		final var query = create.selectDistinct(ROLE.PROFILE_ID)
			.from(ROLE)
			.where(DSL.and(conditions));
		final List<Permission> permissions = new ArrayList<>();
		for(final var result : query.fetch()) {
			final var profile = studyService.getStudy().getProfile(result.get(ROLE.PROFILE_ID));
			permissions.add(new Permission(profile, Timeframe.INFINITE_TIMEFRAME));
		}
		return new ACL(actor, Optional.empty(), permissions);
	}

	@Override
	public ACL getACL(final Actor actor, final Scope scope) {
		final var conditions = new ArrayList<Condition>();
		if(actor instanceof User) {
			conditions.add(ROLE.USER_FK.eq(actor.getPk()));
		}
		else {
			conditions.add(ROLE.ROBOT_FK.eq(actor.getPk()));
		}
		conditions.add(ROLE.STATUS.eq(RoleStatus.ENABLED));

		final List<Permission> permissions = new ArrayList<>();

		final var query = create.selectDistinct(ROLE.PROFILE_ID, SCOPE_ANCESTOR.START_DATE, SCOPE_ANCESTOR.END_DATE, SCOPE_ANCESTOR.VIRTUAL)
			.from(ROLE)
			.leftJoin(SCOPE_ANCESTOR).on(ROLE.SCOPE_FK.eq(SCOPE_ANCESTOR.ANCESTOR_FK))
			.where(DSL.and(ROLE.SCOPE_FK.eq(scope.getPk()).or(SCOPE_ANCESTOR.SCOPE_FK.eq(scope.getPk())), DSL.and(conditions)));
		for(final var result : query.fetch()) {
			final var profile = studyService.getStudy().getProfile(result.get(ROLE.PROFILE_ID));
			final Timeframe timeframe;
			//for the roles directly attached to the scope, SCOPE_ANCESTOR.START_DATE, SCOPE_ANCESTOR.END_DATE and SCOPE_ANCESTOR.VIRTUAL are null
			final var virtual = Optional.ofNullable(result.get(SCOPE_ANCESTOR.VIRTUAL)).orElse(false);
			if(virtual) {
				timeframe = Timeframe.INFINITE_TIMEFRAME;
			}
			else {
				final var startDate = result.get(SCOPE_ANCESTOR.START_DATE);
				final var stopDate = result.get(SCOPE_ANCESTOR.END_DATE);
				timeframe = new Timeframe(Optional.ofNullable(startDate), Optional.ofNullable(stopDate));
			}
			permissions.add(new Permission(profile, timeframe));
		}
		return new ACL(actor, Optional.of(scope), permissions);
	}

	@Override
	public List<Role> filterEnabledRoles(final Collection<Role> roles) {
		return roles.stream().filter(Role::isEnabled).toList();
	}

	@Override
	public List<Role> filterRoles(final Collection<Role> roles, final Scope scope) {
		return roles.stream()
			.filter(r -> scope.getPk().equals(r.getScopeFk()) || scopeRelationService.isDescendantOfEnabled(scope.getPk(), r.getScopeFk()))
			.toList();
	}

	@Override
	public void checkRight(final Actor actor, final List<Role> roles, final Scope scope) {
		final var scopeRoles = filterRoles(roles, scope);
		if(scopeRoles.isEmpty()) {
			throw new UnauthorizedException();
		}
	}

	@Override
	public void checkRightAdmin(final Actor actor, final List<Role> roles) {
		if(!hasRightAdmin(roles)) {
			final var feature = studyService.getStudy().getFeatureStatic(FeatureStatic.ADMIN).orElseThrow();
			throw new UnauthorizedException(feature);
		}
	}

	@Override
	public List<Role> filterRoles(final Collection<Role> roles, final Feature feature) {
		return roles.stream().filter(r -> r.getProfile().hasRight(feature)).toList();
	}

	@Override
	public List<Role> filterRoles(final Collection<Role> roles, final RightAssignable<?> assignableRight, final Rights right) {
		return roles.stream().filter(r -> r.getProfile().hasRight(assignableRight, right)).toList();
	}

	@Override
	public void checkRight(final Actor actor, final Collection<Role> roles, final Feature feature) {
		//check features
		if(!hasRight(roles, feature)) {
			throw new UnauthorizedException(feature);
		}
	}

	@Override
	public void checkRight(final Actor actor, final Collection<Role> roles, final FeatureStatic staticFeature) {
		checkRight(actor, roles, studyService.getStudy().getFeatureStatic(staticFeature).get());
	}

	@Override
	public void checkRight(final Actor actor, final Collection<Role> roles, final Assignable<?> node) {
		if(!hasRight(roles, node)) {
			throw new UnauthorizedException(node);
		}
	}

	@Override
	public void checkRight(final Actor actor, final Collection<Role> roles, final Attributable<?> node) {
		if(!hasRight(roles, node)) {
			throw new UnauthorizedException(node);
		}
	}

	//right assignable
	@Override
	public Role checkRight(final Actor actor, final Collection<Role> roles, final RightAssignable<?> rightAssignable, final Rights right) {
		return getRole(roles, rightAssignable, right).orElseThrow(() -> UnauthorizedException.getInstance(rightAssignable, right));
	}

	@Override
	public Role checkRight(final Actor actor, final Collection<Role> roles, final Entity entity, final Rights right) {
		return roles.stream()
			.filter(r -> r.getProfile().hasRight(entity, right))
			.findFirst()
			.orElseThrow(() -> new UnauthorizedException(right));
	}

	@Override
	public void checkRight(final Actor actor, final List<Role> roles, final User targetUser, final Rights right) {
		if(!hasRight(actor, roles, targetUser, right)) {
			throw new UnauthorizedException(right);
		}
	}

	@Override
	public void checkRight(final Actor actor, final Collection<Role> roles, final ProfileRightAssignable<?> profileRightAssignable, final Optional<Profile> creatorProfile) {
		if(!hasRight(roles, profileRightAssignable, creatorProfile)) {
			throw UnauthorizedException.getInstance(profileRightAssignable, creatorProfile);
		}
	}

	@Override
	public void checkRightToRead(final Actor actor, final List<Role> roles, final Resource resource) {
		if(!hasRightToRead(roles, resource)) {
			throw new UnauthorizedException();
		}
	}

	@Override
	public boolean hasRight(final Scope scope, final Rights right, final Collection<Role> roles) {
		if(right == Rights.READ) {
			return hasRightOnBranch(scope, right, roles);
		}

		return hasRightOnDescendants(scope, right, roles);
	}

	@Override
	public boolean hasRight(final Collection<Role> roles, final Entity entity, final Rights right) {
		return roles.stream()
			.flatMap(r -> r.getProfile().getEnumRightMatrixIds(entity).values().stream())
			.anyMatch(r -> r.contains(right));
	}

	@Override
	public boolean hasRight(final Collection<Role> roles, final Entity entity, final String nodeId, final Rights right) {
		return roles.stream().anyMatch(r -> r.getProfile().hasRight(entity, nodeId, right));
	}

	// TODO this shouldn't be here, maybe move it to the Role Service?
	@Override
	public Optional<Role> getRole(final Collection<Role> roles, final RightAssignable<?> rightAssignable, final Rights right) {
		return roles.stream().filter(r -> r.getProfile().hasRight(rightAssignable, right)).findAny();
	}

	@Override
	public boolean hasRightAdmin(final Collection<Role> roles) {
		return roles.stream().map(Role::getProfile).anyMatch(p -> p.hasFeature(FeatureStatic.ADMIN.name()));
	}

	@Override
	public boolean hasRight(final Collection<Role> roles, final FeatureStatic feature) {
		return roles.stream().map(Role::getProfile).anyMatch(p -> p.hasFeature(feature.name()));
	}

	@Override
	public boolean hasRight(final Collection<Role> roles, final Assignable<?> assignable) {
		return roles.stream().anyMatch(r -> r.getProfile().hasRight(assignable));
	}

	@Override
	public boolean hasRight(final Collection<Role> roles, final RightAssignable<?> rightAssignable, final Rights right) {
		return getRole(roles, rightAssignable, right).isPresent();
	}

	@Override
	public boolean hasRight(final Collection<Role> roles, final Attributable<?> attributable) {
		return roles.stream().map(Role::getProfile).anyMatch(p -> p.hasRight(attributable));
	}

	@Override
	public boolean hasRight(final Collection<Role> roles, final ProfileRightAssignable<?> profileRightAssignable) {
		return roles.stream().map(Role::getProfile).anyMatch(p -> p.hasRight(profileRightAssignable, Optional.empty()));
	}

	//TODO delete this when the advanced workflow matrix has been updated
	@Override
	public boolean hasRight(final Collection<Role> roles, final ProfileRightAssignable<?> profileRightAssignable, final Optional<Profile> creatorProfile) {
		return roles.stream().map(Role::getProfile).anyMatch(p -> p.hasRight(profileRightAssignable, creatorProfile));
	}

	@Override
	public boolean hasRightToRead(final Collection<Role> roles, final Resource resource) {
		if(roles.stream().anyMatch(role -> role.getScopeFk().equals(resource.getScopeFk()))) {
			return true;
		}

		if(roles.stream().anyMatch(role -> scopeRelationService.isDescendantOfEnabled(role.getScopeFk(), resource.getScopeFk()))) {
			return true;
		}

		return roles.stream()
			.anyMatch(role -> scopeRelationService.isDescendantOfEnabled(resource.getScopeFk(), role.getScopeFk()));
	}

	@Override
	public boolean hasRightOnActorRole(final Actor actor, final Collection<Role> roles, final Actor targetActor, final Role targetRole, final Rights right) {
		//everyone can read his own roles
		//(but not necessarily write them)
		if(targetActor.equals(actor) && right == Rights.READ) {
			return true;
		}

		//user has rights to write and read on their roles that have not been activated yet
		if(targetActor.equals(actor) && targetRole.getStatus().equals(RoleStatus.PENDING)) {
			return true;
		}

		for(final var role : roles) {
			//check profile first because it does not require any SQL query
			if(!role.getProfile().hasRight(targetRole.getProfile(), right)) {
				continue;
			}
			//check scope hierarchy
			//if both roles are on the same scope, we are good
			//do this early as this does not require any SQL query
			if(role.getScopeFk().equals(targetRole.getScopeFk())) {
				return true;
			}
			//TODO shortcut: at that point, if the role is on the root scope, he necessarily has the right on the target role
			//the problem is that checking retrieving the scope of the role to check if it is the root scope requires a query to the database

			//to read, a relation between the two scopes is enough
			if(right == Rights.READ && scopeRelationService.isDescendantOf(role.getScopeFk(), targetRole.getScopeFk())) {
				return true;
			}
			//for all rights (including read right), check if the actor's role dominates the target role
			if(scopeRelationService.isDescendantOf(targetRole.getScopeFk(), role.getScopeFk())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean hasRight(final Actor actor, final Collection<Role> roles, final Actor targetActor, final Rights right) {
		//rights on himself
		if(targetActor.equals(actor)) {
			return true;
		}
		return hasRight(actor, roles, targetActor, roleService.getRoles(targetActor), right);
	}

	@Override
	public boolean hasRight(final Actor actor, final Collection<Role> roles, final Actor targetActor, final Collection<Role> targetAllRoles, final Rights right) {
		//rights on himself
		if(targetActor.equals(actor)) {
			return true;
		}

		final var targetActiveRoles = filterEnabledRoles(targetAllRoles);
		final List<Role> targetRoles;
		//if the target user has no active role (for example a user that has just been created), pending roles must be considered
		if(targetActiveRoles.isEmpty()) {
			targetRoles = targetAllRoles.stream()
				.filter(r -> r.getStatus().equals(RoleStatus.PENDING))
				.toList();
		}
		//if the target user has at least one active role, the active roles must be used to check rights
		else {
			targetRoles = targetActiveRoles;
		}

		if(right == Rights.WRITE) {
			//for a user to write another user, it must have the right on every role of the other user
			return targetRoles.stream().allMatch(r -> hasRightOnActorRole(actor, roles, targetActor, r, Rights.WRITE));
		}
		//for a user to read another user, it must have at least one role that grant read access on a role from the other user (branch rule)
		return targetRoles.stream().anyMatch(r -> hasRightOnActorRole(actor, roles, targetActor, r, Rights.READ));
	}

	private boolean hasRightOnBranch(final Scope scope, final Rights right, final Collection<Role> roles) {
		final var scopeModel = scope.getScopeModel();
		return roles.stream()
			.anyMatch(role -> role.getProfile().hasRight(scopeModel, right) && scopeRelationService.areRelated(role.getScopeFk(), scope.getPk()));
	}

	private boolean hasRightOnDescendants(final Scope scope, final Rights right, final Collection<Role> roles) {
		final var scopeModel = scope.getScopeModel();
		return roles.stream()
			.anyMatch(role -> role.getProfile().hasRight(scopeModel, right) && scopeRelationService.isDescendantOfEnabled(scope.getPk(), role.getScopeFk()));
	}

}
