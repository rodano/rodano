package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.RuleDefinitionActionDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.RuleDefinitionActionDAOService;

@Service
@Transactional
public class RuleDefinitionActionServiceImpl implements RuleDefinitionActionService {

	private final RuleDefinitionActionDAOService ruleDefinitionActionDAOService;

	public RuleDefinitionActionServiceImpl(final RuleDefinitionActionDAOService ruleDefinitionActionDAOService) {
		this.ruleDefinitionActionDAOService = ruleDefinitionActionDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RuleDefinitionActionDTO> getRuleDefinitionActions(final UUID projectId) {
		return ruleDefinitionActionDAOService.getRuleDefinitionActions(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public RuleDefinitionActionDTO getRuleDefinitionAction(final UUID projectId, final UUID ruleDefinitionActionId) {
		final var ruleDefinitionAction = ruleDefinitionActionDAOService.getRuleDefinitionAction(projectId, ruleDefinitionActionId);
		if(ruleDefinitionAction == null) {
			throw new NotFoundException("Rule definition action not found: " + ruleDefinitionActionId);
		}
		return ruleDefinitionAction;
	}

	@Override
	public RuleDefinitionActionDTO createRuleDefinitionAction(final UUID projectId, final RuleDefinitionActionDTO dto) {
		return ruleDefinitionActionDAOService.createRuleDefinitionAction(projectId, dto);
	}

	@Override
	public RuleDefinitionActionDTO updateRuleDefinitionAction(final UUID projectId, final UUID ruleDefinitionActionId, final RuleDefinitionActionDTO dto) {
		final var existing = ruleDefinitionActionDAOService.getRuleDefinitionAction(projectId, ruleDefinitionActionId);
		if(existing == null) {
			throw new NotFoundException("Rule definition action not found: " + ruleDefinitionActionId);
		}
		return ruleDefinitionActionDAOService.updateRuleDefinitionAction(projectId, ruleDefinitionActionId, dto);
	}

	@Override
	public void deleteRuleDefinitionAction(final UUID projectId, final UUID ruleDefinitionActionId) {
		final var existing = ruleDefinitionActionDAOService.getRuleDefinitionAction(projectId, ruleDefinitionActionId);
		if(existing == null) {
			throw new NotFoundException("Rule definition action not found: " + ruleDefinitionActionId);
		}
		ruleDefinitionActionDAOService.deleteRuleDefinitionAction(projectId, ruleDefinitionActionId);
	}
}
