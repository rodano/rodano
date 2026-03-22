package ch.rodano.api.config;

import java.util.List;
import java.util.UUID;

public record RuleConditionDTO(
	UUID ruleConditionId,
	String id,
	RuleCriterionDTO criterion,
	boolean inverse,
	boolean dependency,
	String breakType,
	String mode,
	List<RuleConditionDTO> conditions
) {
}
