package ch.rodano.api.config;

import java.util.UUID;

public record RuleActionParameterDTO(
	UUID ruleActionParameterId,
	String id,
	String value,
	String rulableEntity,
	String conditionId
) {
}
