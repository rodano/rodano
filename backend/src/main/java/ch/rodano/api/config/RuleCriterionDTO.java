package ch.rodano.api.config;

import java.util.List;
import java.util.UUID;

public record RuleCriterionDTO(
	UUID ruleCriterionId,
	String property,
	String operator,
	List<String> values
) {
}
