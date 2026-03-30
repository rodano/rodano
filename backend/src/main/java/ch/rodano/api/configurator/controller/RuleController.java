package ch.rodano.api.configurator.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.core.services.dao.configurator.RuleDAOService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config/rules")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class RuleController {

	private final RuleDAOService ruleDAOService;

	public RuleController(final RuleDAOService ruleDAOService) {
		this.ruleDAOService = ruleDAOService;
	}

	@GetMapping("/tags")
	public List<String> getAllTags(@PathVariable final UUID projectId) {
		return ruleDAOService.getAllTags(projectId);
	}
}
