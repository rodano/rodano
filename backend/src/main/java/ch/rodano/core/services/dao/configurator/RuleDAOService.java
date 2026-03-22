package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.RuleDTO;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

public interface RuleDAOService {

	List<RuleDTO> getRules(UUID projectId, RuleEntityType entityType, UUID entityId);

	RuleDTO createRule(UUID projectId, RuleEntityType entityType, UUID entityId, String ruleType, RuleDTO dto);

	RuleDTO updateRule(UUID projectId, RuleEntityType entityType, UUID entityId, UUID ruleId, RuleDTO dto);

	void deleteRule(UUID projectId, RuleEntityType entityType, UUID entityId, UUID ruleId);
}
