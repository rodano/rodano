package ch.rodano.api.actor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
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
import ch.rodano.configuration.model.feature.FeatureStatic;
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
	public static final Comparator<Scope> SCOPE_COMPARATOR = Comparator
		.comparing(Scope::getScopeModel, ScopeModel.COMPARATOR_DEPTH)
		.thenComparing(Scope::getCode)
		.thenComparing(Scope::getCreationTime)
		.thenComparing(Scope::getPk);

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

	@Operation(summary = "Get all scopes on which the currently connected actor (yourself) has a right")
	@GetMapping("scopes")
	@ResponseStatus(HttpStatus.OK)
	public List<ScopeMiniDTO> getScopes(
		@Parameter(description = "Filter scope to a specific feature") @RequestParam final Optional<FeatureStatic> feature,
		@Parameter(description = "Exclude leaf") @RequestParam final boolean excludeLeaf,
		@Parameter(description = "Exclude virtual") @RequestParam final boolean excludeVirtual
	) {
		final var currentActor = currentActor();

		final List<Scope> rootScopes = feature.map(f -> actorService.getRootScopes(currentActor, f)).orElseGet(() -> actorService.getRootScopes(currentActor));

		// Retrieve interesting scope models
		final var scopeModels = studyService.getStudy().getScopeModels()
			.stream()
			.filter(s -> !excludeLeaf || !s.isLeaf())
			.filter(s -> !excludeVirtual || !s.isVirtual())
			.collect(Collectors.toSet());

		final Set<Scope> scopes = new TreeSet<>(SCOPE_COMPARATOR);
		for(final var rootScope : rootScopes) {
			scopes.add(rootScope);
			scopes.addAll(scopeService.getAll(scopeModels, Collections.singleton(rootScope)));
		}

		return scopes.stream()
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
		final var role = currentRoles.get(0);
		role.setProfile(profile);
		roleService.updateRole(role, currentContext(), "Set profile");

		return actorDTOService.createUserDTO(currentUser, currentUser, currentRoles);
	}
}
