package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.RuleDefinitionActionDTO;


public interface RuleDefinitionActionDAOService {

	List<RuleDefinitionActionDTO> getRuleDefinitionActions(UUID projectId);

	RuleDefinitionActionDTO getRuleDefinitionAction(UUID projectId, UUID ruleDefinitionActionId);

	RuleDefinitionActionDTO createRuleDefinitionAction(UUID projectId, RuleDefinitionActionDTO dto);

	RuleDefinitionActionDTO updateRuleDefinitionAction(UUID projectId, UUID ruleDefinitionActionId, RuleDefinitionActionDTO dto);

	void deleteRuleDefinitionAction(UUID projectId, UUID ruleDefinitionActionId);
}
