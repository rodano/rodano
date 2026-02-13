package ch.rodano.api.configurator.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

import ch.rodano.api.configurator.dto.FormulaConditionDTO;
import ch.rodano.api.configurator.dto.FormulaFunctionDTO;
import ch.rodano.core.model.rules.formula.FormulaFunction;

@RestController
@RequestMapping("/superuser/configurator")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class FormulaAutocompleteController {

	@Operation(summary = "Get available formula functions")
	@GetMapping("/projects/{projectId}/formula/functions")
	@ResponseStatus(HttpStatus.OK)
	public List<FormulaFunctionDTO> getFormulaFunctions(@PathVariable final UUID projectId) {
		return Arrays.stream(FormulaFunction.values())
			.map(FormulaFunctionDTO::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Get available formula conditions")
	@GetMapping("/projects/{projectId}/formula/conditions")
	@ResponseStatus(HttpStatus.OK)
	public List<FormulaConditionDTO> getFormulaConditions(@PathVariable final UUID projectId) {
		return List.of(
			new FormulaConditionDTO("DATASET", "Condition DATASET", "DATASET"),
			new FormulaConditionDTO("EVENT", "Condition EVENT", "EVENT"),
			new FormulaConditionDTO("FIELD", "Condition FIELD", "FIELD"),
			new FormulaConditionDTO("SCOPE", "Condition SCOPE", "SCOPE")
		);
	}
}
