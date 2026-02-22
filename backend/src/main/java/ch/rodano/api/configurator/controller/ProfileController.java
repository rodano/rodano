package ch.rodano.api.configurator.controller;

import java.util.List;
import java.util.UUID;

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

import ch.rodano.api.config.ProfileDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.ProfileService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ProfileController {

	private final ProfileService profileService;

	public ProfileController(final ProfileService profileService) {
		this.profileService = profileService;
	}

	/**
	 * Get all profiles for a project
	 */
	@GetMapping("/profiles")
	public ResponseEntity<List<ProfileDTO>> getProfiles(@PathVariable final UUID projectId) {
		final var profiles = profileService.getProfiles(projectId);
		return ResponseEntity.ok(profiles);
	}

	/**
	 * Get a specific profile
	 */
	@GetMapping("/profiles/{profileId}")
	public ResponseEntity<ProfileDTO> getProfile(
		@PathVariable final UUID projectId,
		@PathVariable final UUID profileId
	) {
		final var profile = profileService.getProfile(projectId, profileId);
		return ResponseEntity.ok(profile);
	}

	/**
	 * Create a new profile
	 */
	@PostMapping("/profiles")
	@SkipProjectAccessCheck
	public ResponseEntity<ProfileDTO> createProfile(
		@PathVariable final UUID projectId,
		@RequestBody final ProfileDTO profile
	) {
		final var created = profileService.createProfile(projectId, profile);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing profile
	 */
	@PutMapping("/profiles/{profileId}")
	@SkipProjectAccessCheck
	public ResponseEntity<ProfileDTO> updateProfile(
		@PathVariable final UUID projectId,
		@PathVariable final UUID profileId,
		@RequestBody final ProfileDTO profile
	) {
		final var updated = profileService.updateProfile(projectId, profileId, profile);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a profile
	 */
	@DeleteMapping("/profiles/{profileId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteProfile(
		@PathVariable final UUID projectId,
		@PathVariable final UUID profileId
	) {
		profileService.deleteProfile(projectId, profileId);
		return ResponseEntity.noContent().build();
	}
}
