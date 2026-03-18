package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.RuleDefinitionPropertyDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.RuleDefinitionPropertyDAOService;

@Service
@Transactional
public class RuleDefinitionPropertyServiceImpl implements RuleDefinitionPropertyService {

	private final RuleDefinitionPropertyDAOService ruleDefinitionPropertyDAOService;

	public RuleDefinitionPropertyServiceImpl(final RuleDefinitionPropertyDAOService ruleDefinitionPropertyDAOService) {
		this.ruleDefinitionPropertyDAOService = ruleDefinitionPropertyDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RuleDefinitionPropertyDTO> getRuleDefinitionProperties(final UUID projectId) {
		return ruleDefinitionPropertyDAOService.getRuleDefinitionProperties(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public RuleDefinitionPropertyDTO getRuleDefinitionProperty(final UUID projectId, final UUID ruleDefinitionPropertyId) {
		final var ruleDefinitionProperty = ruleDefinitionPropertyDAOService.getRuleDefinitionProperty(projectId, ruleDefinitionPropertyId);
		if(ruleDefinitionProperty == null) {
			throw new NotFoundException("Rule definition property not found: " + ruleDefinitionPropertyId);
		}
		return ruleDefinitionProperty;
	}

	@Override
	public RuleDefinitionPropertyDTO createRuleDefinitionProperty(final UUID projectId, final RuleDefinitionPropertyDTO dto) {
		return ruleDefinitionPropertyDAOService.createRuleDefinitionProperty(projectId, dto);
	}

	@Override
	public RuleDefinitionPropertyDTO updateRuleDefinitionProperty(final UUID projectId, final UUID ruleDefinitionPropertyId, final RuleDefinitionPropertyDTO dto) {
		final var existing = ruleDefinitionPropertyDAOService.getRuleDefinitionProperty(projectId, ruleDefinitionPropertyId);
		if(existing == null) {
			throw new NotFoundException("Rule definition property not found: " + ruleDefinitionPropertyId);
		}
		return ruleDefinitionPropertyDAOService.updateRuleDefinitionProperty(projectId, ruleDefinitionPropertyId, dto);
	}

	@Override
	public void deleteRuleDefinitionProperty(final UUID projectId, final UUID ruleDefinitionPropertyId) {
		final var existing = ruleDefinitionPropertyDAOService.getRuleDefinitionProperty(projectId, ruleDefinitionPropertyId);
		if(existing == null) {
			throw new NotFoundException("Rule definition property not found: " + ruleDefinitionPropertyId);
		}
		ruleDefinitionPropertyDAOService.deleteRuleDefinitionProperty(projectId, ruleDefinitionPropertyId);
	}
}
