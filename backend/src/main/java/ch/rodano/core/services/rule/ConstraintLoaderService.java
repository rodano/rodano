package ch.rodano.core.services.rule;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.core.dao.RuleDAO;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;

@Service
public class ConstraintLoaderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintLoaderService.class);

	private final RuleDAO ruleDAO;

	public ConstraintLoaderService(final RuleDAO ruleDAO) {
		this.ruleDAO = ruleDAO;
	}

	public void loadAndAssignConstraints(final Study study) {
		LOGGER.info("Loading all constraints for study {}", study.getId());

		final var allConstraints = ruleDAO.loadAllConstraintsForProject(study.getProjectId());

		assignConstraints(
			"field models",
			study.getFieldModels(),
			allConstraints.getOrDefault(RuleConstraintOwnerType.FIELD_MODEL, Map.of()),
			FieldModel::getFieldModelId,
			FieldModel::setValueConstraint
		);

		assignConstraints(
			"validators",
			study.getValidators(),
			allConstraints.getOrDefault(RuleConstraintOwnerType.VALIDATOR, Map.of()),
			Validator::getValidatorId,
			Validator::setConstraint
		);

		final var allLayouts = study.getFormModels().stream()
			.flatMap(fm -> fm.getLayouts().stream()).toList();

		assignConstraints(
			"form layouts",
			allLayouts,
			allConstraints.getOrDefault(RuleConstraintOwnerType.FORM_LAYOUT, Map.of()),
			Layout::getLayoutId,
			Layout::setConstraint
		);

		final var allCells = study.getFormModels().stream()
			.flatMap(fm -> fm.getCells().stream()).toList();

		assignConstraints(
			"form layout cells",
			allCells,
			allConstraints.getOrDefault(RuleConstraintOwnerType.FORM_LAYOUT_CELL, Map.of()),
			Cell::getLayoutCellId,
			Cell::setConstraint
		);

		LOGGER.info("Finished loading and assigning constraints");
	}

	private <T> void assignConstraints(final String entityTypeName,
									   final Collection<T> entities,
									   final Map<UUID, RuleConstraint> constraints,
									   final Function<T, UUID> idGetter,
									   final BiConsumer<T, RuleConstraint> constraintSetter) {
		int assigned = 0;
		int missing = 0;

		for(T entity : entities) {
			final UUID entityId = idGetter.apply(entity);
			final var constraint = constraints.get(entityId);

			if(constraint != null) {
				constraintSetter.accept(entity, constraint);
				assigned++;
			}
			else {
				missing++;
			}
		}
		LOGGER.info("Assigned {} constraints to validators", assigned);
		if(missing > 0) {
			LOGGER.info("{} {} have no constraints in database", missing, entityTypeName);
		}
	}
}
