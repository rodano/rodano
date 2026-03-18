package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.RuleDefinitionPropertyDTO;

public interface RuleDefinitionPropertyDAOService {

	List<RuleDefinitionPropertyDTO> getRuleDefinitionProperties(UUID projectId);

	RuleDefinitionPropertyDTO getRuleDefinitionProperty(UUID projectId, UUID ruleDefinitionPropertyId);

	RuleDefinitionPropertyDTO createRuleDefinitionProperty(UUID projectId, RuleDefinitionPropertyDTO dto);

	RuleDefinitionPropertyDTO updateRuleDefinitionProperty(UUID projectId, UUID ruleDefinitionPropertyId, RuleDefinitionPropertyDTO dto);

	void deleteRuleDefinitionProperty(UUID projectId, UUID ruleDefinitionPropertyId);
}
