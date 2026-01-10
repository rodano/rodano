package ch.rodano.api.user.management;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.user.management.UserManagementService;

@RestController
@RequestMapping("/superuser/users")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class UserManagementController {

	private final UserManagementService userManagementService;

	public UserManagementController(final UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	/**
	 * Get all users
	 */
	@GetMapping
	public ResponseEntity<List<UserManagementDTO>> getAllUsers() {
		final var users = userManagementService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	/**
	 * Get specific user
	 */
	@GetMapping("/{userPk}")
	public ResponseEntity<UserManagementDTO> getUser(@PathVariable final Long userPk) {
		final var user = userManagementService.getUser(userPk);
		return ResponseEntity.ok(user);
	}

	/**
	 * Create new user
	 */
	@PostMapping
	@SkipProjectAccessCheck
	public ResponseEntity<UserManagementDTO> createUser(@Valid @RequestBody final CreateUserRequest request,
														final HttpServletRequest httpRequest) {
		final var contextUrl = httpRequest.getScheme() + "://" +
			httpRequest.getServerName() +
			(httpRequest.getServerPort() != 80 && httpRequest.getServerPort() != 443
				? ":" + httpRequest.getServerPort()
				: "");

		final var user = userManagementService.createUser(request, contextUrl, null);
		return ResponseEntity.ok(user);
	}

	/**
	 * Update an existing user
	 */
	@PutMapping("/{userPk}")
	@SkipProjectAccessCheck
	public ResponseEntity<UserManagementDTO> updateUser(@PathVariable final Long userPk,
														@Valid @RequestBody final UpdateUserRequest request) {
		final var user = userManagementService.updateUser(userPk, request, null);
		return ResponseEntity.ok(user);
	}

	/**
	 * Delete a user
	 */
	@DeleteMapping("/{userPk}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteUser(@PathVariable final Long userPk) {
		userManagementService.deleteUser(userPk, null);
		return ResponseEntity.ok().build();
	}

	/**
	 * Restore a deleted user
	 */
	@PostMapping("/{userPk}/restore")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> restoreUser(@PathVariable final Long userPk) {
		userManagementService.restoreUser(userPk, null);
		return ResponseEntity.ok().build();
	}

	/**
	 * Toggle superuser status
	 */
	@PutMapping("/{userPk}/superuser")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> toggleSuperuser(@PathVariable final Long userPk,
												@RequestBody final ToggleSuperuserRequest request) {
		userManagementService.toggleSuperuser(userPk, request.isSuperuser(), null);
		return ResponseEntity.ok().build();
	}

	/**
	 * Unblock a user
	 */
	@PostMapping("/{userPk}/unblock")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> unblockUser(@PathVariable final Long userPk) {
		userManagementService.unblockUser(userPk, null);
		return ResponseEntity.ok().build();
	}
}
