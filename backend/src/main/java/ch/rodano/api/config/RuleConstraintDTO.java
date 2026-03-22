package ch.rodano.api.config;

import java.util.Map;
import java.util.UUID;

public record RuleConstraintDTO(
	UUID ruleConstraintId,
	Map<String, RuleConditionListDTO> conditions
) {
}
