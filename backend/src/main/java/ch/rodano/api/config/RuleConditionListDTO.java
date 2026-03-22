package ch.rodano.api.config;

import java.util.List;
import java.util.UUID;

public record RuleConditionListDTO(
	UUID ruleConditionListId,
	String mode,
	List<RuleConditionDTO> conditions
) {
}
