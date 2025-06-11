package ch.rodano.api.actor.activation;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.config.PrivacyPolicyDTO;
import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "User Activation")
@SecurityRequirements
@RestController
// Warning : if you change this API endpoint, do not forget to change it in the security configuration !
@RequestMapping("/user/activation")
@Validated
@Transactional(readOnly = true)
public class UserActivationController extends AbstractSecuredController {

	private final UserDAOService userDAOService;
	private final UserSecurityService userSecurityService;

	public UserActivationController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final UserDAOService userDAOService,
		final UserSecurityService userSecurityService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.userDAOService = userDAOService;
		this.userSecurityService = userSecurityService;
	}

	@Operation(summary = "Get the user's e-mail and privacy policies for acceptance")
	@GetMapping("{activationCode}")
	@ResponseStatus(HttpStatus.OK)
	public UserPrivacyPoliciesDTO getPrivacyPolicies(
		@PathVariable final String activationCode
	) {
		final var user = userDAOService.getUserByActivationCode(activationCode);

		if(user == null) {
			throw new IllegalArgumentException("Invalid activation code");
		}

		final var policies = roleService.getRoles(user).stream()
			.flatMap(role -> role.getProfile().getPrivacyPolicies().stream())
			.map(PrivacyPolicyDTO::new)
			.toList();

		return new UserPrivacyPoliciesDTO(
			user.getEmail(),
			policies
		);
	}

	@Operation(summary = "Activate the user")
	@PostMapping("{activationCode}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void activateUserAccount(
		@PathVariable final String activationCode,
		@Valid @RequestBody final UserActivationDTO userActivationDTO
	) {
		userSecurityService.activateUser(
			activationCode,
			userActivationDTO.acceptPolicies(),
			userActivationDTO.password(),
			currentContext()
		);
	}
}
