package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

public record RuleDTO(
	UUID ruleId,
	String description,
	SortedMap<String, String> message,
	List<String> tags,
	RuleConstraintDTO constraint,
	List<RuleActionDTO> actions
) {
}
