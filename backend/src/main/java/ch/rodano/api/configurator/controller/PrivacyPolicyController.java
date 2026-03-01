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

import ch.rodano.api.config.PrivacyPolicyDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.PrivacyPolicyService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class PrivacyPolicyController {

	private final PrivacyPolicyService privacyPolicyService;

	public PrivacyPolicyController(final PrivacyPolicyService privacyPolicyService) {
		this.privacyPolicyService = privacyPolicyService;
	}

	/**
	 * Get all privacy policies for a project
	 */
	@GetMapping("/policies")
	public ResponseEntity<List<PrivacyPolicyDTO>> getPrivacyPolicies(@PathVariable final UUID projectId) {
		final var privacyPolicies = privacyPolicyService.getPrivacyPolicies(projectId);
		return ResponseEntity.ok(privacyPolicies);
	}

	/**
	 * Get a specific privacy policy
	 */
	@GetMapping("/policies/{privacyPolicyId}")
	public ResponseEntity<PrivacyPolicyDTO> getPrivacyPolicy(
		@PathVariable final UUID projectId,
		@PathVariable final UUID privacyPolicyId
	) {
		final var PrivacyPolicy = privacyPolicyService.getPrivacyPolicy(projectId, privacyPolicyId);
		return ResponseEntity.ok(PrivacyPolicy);
	}

	/**
	 * Create a new privacy policy
	 */
	@PostMapping("/policies")
	@SkipProjectAccessCheck
	public ResponseEntity<PrivacyPolicyDTO> createPrivacyPolicy(
		@PathVariable final UUID projectId,
		@RequestBody final PrivacyPolicyDTO privacyPolicy
	) {
		final var created = privacyPolicyService.createPrivacyPolicy(projectId, privacyPolicy);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing privacy policy
	 */
	@PutMapping("/policies/{privacyPolicyId}")
	@SkipProjectAccessCheck
	public ResponseEntity<PrivacyPolicyDTO> updatePrivacyPolicy(
		@PathVariable final UUID projectId,
		@PathVariable final UUID privacyPolicyId,
		@RequestBody final PrivacyPolicyDTO privacyPolicy
	) {
		final var updated = privacyPolicyService.updatePrivacyPolicy(projectId, privacyPolicyId, privacyPolicy);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a privacy policy
	 */
	@DeleteMapping("/policies/{privacyPolicyId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deletePrivacyPolicy(
		@PathVariable final UUID projectId,
		@PathVariable final UUID privacyPolicyId
	) {
		privacyPolicyService.deletePrivacyPolicy(projectId, privacyPolicyId);
		return ResponseEntity.noContent().build();
	}
}
