package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.RuleConstraintDTO;
import ch.rodano.api.config.RuleDTO;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

public interface RuleDAOService {

	List<RuleDTO> getRules(UUID projectId, RuleEntityType entityType, UUID entityId);

	RuleConstraintDTO getConstraint(UUID projectId, UUID ownerId);

	RuleConstraintDTO saveConstraint(UUID projectId, UUID ownerId, RuleConstraintOwnerType ownerType, RuleConstraintConstraintType constraintType, RuleConstraintDTO dto);

	List<String> getAllTags(UUID projectId);

	RuleDTO createRule(UUID projectId, RuleEntityType entityType, UUID entityId, String ruleType, RuleDTO dto);

	RuleDTO updateRule(UUID projectId, RuleEntityType entityType, UUID entityId, UUID ruleId, RuleDTO dto);

	void deleteRule(UUID projectId, RuleEntityType entityType, UUID entityId, UUID ruleId);
}
