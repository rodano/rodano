package ch.rodano.api.configurator.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.api.config.EntityRightDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.FormModelRightsService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class FormModelRightsController {

	private final FormModelRightsService formModelRightsService;

	public FormModelRightsController(final FormModelRightsService formModelRightsService) {
		this.formModelRightsService = formModelRightsService;
	}

	@GetMapping("/form-model-rights")
	public ResponseEntity<Map<UUID, Map<UUID, EntityRightDTO>>> getFormModelRights(@PathVariable final UUID projectId) {
		return ResponseEntity.ok(formModelRightsService.getFormModelRights(projectId));
	}

	@PutMapping("/form-model-rights")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> saveFormModelRights(
		@PathVariable final UUID projectId,
		@RequestBody final Map<UUID, Map<UUID, EntityRightDTO>> rights
	) {
		formModelRightsService.saveFormModelRights(projectId, rights);
		return ResponseEntity.noContent().build();
	}
}
