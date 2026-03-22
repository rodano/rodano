package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

public record RuleActionDTO(
	UUID ruleActionId,
	String id,
	SortedMap<String, String> label,
	boolean optional,
	String staticActionId,
	String configurationWorkflowId,
	String configurationActionId,
	String rulableEntity,
	String conditionId,
	String actionId,
	List<RuleActionParameterDTO> parameters
) {
}
