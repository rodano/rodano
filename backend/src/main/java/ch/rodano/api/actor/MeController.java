package ch.rodano.api.actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.scope.ScopeMiniDTO;
import ch.rodano.configuration.model.cms.RequiredRight;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.configuration.core.Environment;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Me", description = "Get information about the currently connected user/robot")
@RestController
//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
@RequestMapping("/me")
@Validated
@Transactional(readOnly = true)
public class MeController extends AbstractSecuredController {
	private final ActorDTOService actorDTOService;
	private final Configurator configurator;
	private final ScopeService scopeService;

	private final String[] superUsers;

	public MeController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ActorDTOService actorDTOService,
		final Configurator configurator,
		final ScopeService scopeService,
		@Value("${rodano.superusers:}") final String[] superUsers
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.actorDTOService = actorDTOService;
		this.configurator = configurator;
		this.scopeService = scopeService;
		this.superUsers = superUsers;
	}

	@Operation(summary = "Get the currently connected user (yourself)")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public UserDTO getCurrentUser() {
		final var user = (User) currentActor();
		return actorDTOService.createUserDTO(user, user, currentActiveRoles());
	}

	// TODO change the endpoint maybe
	@Operation(summary = "Get the currently connected robot (yourself)")
	@GetMapping("robot")
	@ResponseStatus(HttpStatus.OK)
	public ActorDTO getCurrentRobot() {
		final var actor = (Robot) currentActor();
		return actorDTOService.createRobotDTO(actor, actor, currentActiveRoles());
	}

	// TODO Replace this by a utility endpoint that gets the most "powerful" scope that the user has access to.
	// TODO For all other use-cases use "root-scopes".
	@Deprecated
	@Operation(summary = "Get the root scope of the currently connected actor (yourself)")
	@GetMapping("root-scope")
	@ResponseStatus(HttpStatus.OK)
	public ScopeMiniDTO getRootScope() {
		return new ScopeMiniDTO(actorService.getRootScope(currentActor()).orElseThrow());
	}

	@Operation(summary = "Get the root scopes of the currently connected actor (yourself)")
	@GetMapping("root-scopes")
	@ResponseStatus(HttpStatus.OK)
	public List<ScopeMiniDTO> getRootScopes() {
		return actorService.getRootScopes(currentActor()).stream()
			.map(ScopeMiniDTO::new)
			.toList();
	}

	@Operation(summary = "Get scopes on which the currently connected actor (yourself) has a feature or a required right")
	@GetMapping("scopes")
	@ResponseStatus(HttpStatus.OK)
	public List<ScopeMiniDTO> getScopes(
		@Parameter(description = "Feature ID the actor must have a right on") @RequestParam("feature") final Optional<FeatureStatic> feature,
		@Parameter(description = "Entity on which the required right applies") @RequestParam("rightEntity") final Optional<Entity> rightEntity,
		@Parameter(description = "Id of the entity of the actor must have a right on") @RequestParam("rightId") final Optional<String> rightId,
		@Parameter(description = "Right the actor must have on the entity") @RequestParam("right") final Optional<Rights> right,
		@Parameter(description = "Restrict the returned scopes to these scope models; if absent, all non-leaf scope models are considered") @RequestParam("scopeModelIds") final Optional<Collection<String>> scopeModelIds
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		final var study = studyService.getStudy();

		//find root scopes based on the required right or feature
		List<Scope> rootScopes = new ArrayList<>();
		if(rightEntity.isPresent() || rightId.isPresent() || right.isPresent()) {
			final var requiredRight = new RequiredRight(
				rightId.orElse(null),
				right.orElse(null),
				rightEntity.orElse(null)
			);
			if(!requiredRight.isValid()) {
				throw new IllegalArgumentException("Invalid required right");
			}
			if(feature.isPresent()) {
				throw new IllegalArgumentException("Cannot specify both a feature and a required right");
			}
			final var assignable = study.getChild(requiredRight.getRightEntity(), requiredRight.getId());
			if(!(assignable instanceof RightAssignable)) {
				throw new IllegalArgumentException("Assignable is not a RightAssignable");
			}

			rootScopes = actorService.getRootScopes(currentActor, (RightAssignable<?>) assignable, requiredRight.getRight());
		}
		else if(feature.isPresent()) {
			rootScopes = actorService.getRootScopes(currentActor, feature.get());
		}
		else {
			rootScopes = actorService.getRootScopes(currentActor);
		}

		//target scope models
		Collection<ScopeModel> targetScopeModels = new ArrayList<>();

		if(scopeModelIds.isPresent()) {
			targetScopeModels = scopeModelIds.get().stream().map(study::getScopeModel).collect(Collectors.toSet());
			targetScopeModels.forEach(s -> rightsService.checkRight(currentActor, currentRoles, s, Rights.READ));
		}
		else {
			targetScopeModels = study.getScopeModels().stream()
				.filter(s -> !s.isLeaf())
				.filter(s -> rightsService.hasRight(currentRoles, s, Rights.READ))
				.collect(Collectors.toSet());
		}

		//scope service returns root scopes as well as their descendants matching the configured target scope models
		return scopeService.getAll(targetScopeModels, rootScopes).stream()
			.sorted(Scope.DEPTH_COMPARATOR)
			.map(ScopeMiniDTO::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Change the profile of the currently connected actor (yourself) if they are a superuser", hidden = true)
	@PutMapping("impersonate")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public UserDTO impersonate(
		@Valid @RequestBody final ImpersonationDTO impersonation
	) {
		final var currentUser = (User) currentActor();
		final var currentRoles = currentActiveRoles();

		if(currentRoles.size() != 1 || !Arrays.asList(superUsers).contains(currentUser.getEmail()) || !Environment.DEV.equals(configurator.getEnvironment())) {
			throw new UnauthorizedException("Only superusers can change their profile in development mode");
		}

		final var profile = studyService.getStudy().getProfile(impersonation.profileId());
		final var role = currentRoles.getFirst();
		role.setProfile(profile);
		roleService.updateRole(role, currentContext(), "Set profile");

		return actorDTOService.createUserDTO(currentUser, currentUser, currentRoles);
	}
}
