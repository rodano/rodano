package ch.rodano.core.services.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.data.ConstraintEvaluationService;
import ch.rodano.core.model.rules.data.DataEvaluation;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.rules.formula.FormulaParserService;
import ch.rodano.core.model.rules.formula.exception.UnableToCalculateFormulaException;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.plugin.entity.EntityPluginService;

@Service
public class RuleServiceImpl implements RuleService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final EntityPluginService entityPluginService;
	private final ConstraintEvaluationService constraintEvaluationService;
	private final RulableEntityBinderService rulableEntityBinderService;
	private final FormulaParserService formulaParserService;

	public RuleServiceImpl(
		final StudyService studyService,
		final EntityPluginService entityPluginService,
		final RulableEntityBinderService rulableEntityBinderService,
		final ConstraintEvaluationService constraintEvaluationService,
		final FormulaParserService formulaParserService
	) {
		this.studyService = studyService;
		this.entityPluginService = entityPluginService;
		this.constraintEvaluationService = constraintEvaluationService;
		this.rulableEntityBinderService = rulableEntityBinderService;
		this.formulaParserService = formulaParserService;
	}

	@Override
	public List<Map<String, String>> execute(
		final DataState state,
		final List<Rule> rules,
		final DatabaseActionContext context
	) {
		return execute(state, rules, context, null);
	}

	@Override
	public List<Map<String, String>> execute(
		final DataState state,
		final List<Rule> rules,
		final DatabaseActionContext context,
		final String message
	) {
		return execute(state, rules, context, message, null);
	}

	@Override
	public List<Map<String, String>> execute(
		final DataState state,
		final List<Rule> rules,
		final DatabaseActionContext context,
		final String message,
		final Map<String, Object> data
	) {
		return execute(state, rules, context, message, data, null);
	}

	@Override
	public List<Map<String, String>> execute(
		final DataState state,
		final List<Rule> rules,
		final DatabaseActionContext context,
		final String message,
		final Map<String, Object> data,
		final Set<String> blockedActions
	) {
		final List<Map<String, String>> messages = new ArrayList<>();
		for(final var rule : rules) {
			if(execute(state, rule, context, message, data, blockedActions)) {
				if(rule.getMessage() != null) {
					messages.add(rule.getMessage());
				}
			}
		}
		return messages;
	}

	private Boolean execute(
		final DataState state,
		final Rule rule,
		final DatabaseActionContext context,
		final String message,
		final Map<String, Object> data,
		final Set<String> blockedActions
	) {
		final var ruleDescription = StringUtils.defaultString(rule.getDescription());
		logger.info("Evaluating rule [{}]", ruleDescription);

		final var evaluation = new DataEvaluation(state, rule.getConstraint());
		constraintEvaluationService.evaluate(evaluation);
		if(!evaluation.isValid()) {
			// Evaluation fails
			logger.info("Aborting execution of rule [{}]", ruleDescription);
			return false;
		}

		//execute rules action
		try {
			for(final var ruleAction : rule.getActions()) {
				if(blockedActions != null && blockedActions.contains(ruleAction.getId())) {
					continue;
				}

				// Configuration action
				if(ruleAction.getConfigurationActionId() != null) {
					execute(state, studyService.getStudy().getWorkflow(ruleAction.getConfigurationWorkflowId()).getAction(ruleAction.getConfigurationActionId()).getRules(), context);
					continue;
				}

				// Find parameters
				final Map<String, Object> parameters = new TreeMap<>();
				for(final var parameter : ruleAction.getParameters()) {
					if(parameter == null) {
						continue;
					}

					if(parameter.getRulableEntity() != null) {
						parameters.put(parameter.getId(), state.getEvaluables(parameter.getRulableEntity()));
						continue;
					}

					if(StringUtils.isNotBlank(parameter.getConditionId())) {
						parameters.put(parameter.getId(), evaluation.getStates().get(parameter.getConditionId()).getReferenceEvaluables());
						continue;
					}

					if(!StringUtils.isNotBlank(parameter.getValue())) {
						continue;
					}

					if(!parameter.getValue().startsWith("=")) {
						parameters.put(parameter.getId(), parameter.getValue());
						continue;
					}

					try {
						final var value = formulaParserService.parse(parameter.getValue(), evaluation.getStates());
						parameters.put(parameter.getId(), value);
					}
					catch(final UnableToCalculateFormulaException e) {
						logger.error(e.getMessage(), e);
					}
				}

				// Static action
				if(ruleAction.getStaticActionId() != null) {
					if(!entityPluginService.checkStaticPluginExists(ruleAction.getStaticActionId())) {
						// If the static action is not found throw an exception.
						throw new UnsupportedOperationException(String.format("No action with id %s", ruleAction.getStaticActionId()));
					}

					logger.info(String.format("Execute static action %s with parameters %s", ruleAction.getStaticActionId(), ruleAction.getParameters().toString()));
					final var staticEntity = entityPluginService.getStaticPlugin(ruleAction.getStaticActionId());
					staticEntity.action(parameters, context);

					continue;
				}

				// Entity action
				final RulableEntity entity;
				final DataState ruleActionState;

				// Action using context
				if(ruleAction.getRulableEntity() != null) {
					entity = ruleAction.getRulableEntity();
					ruleActionState = state;
				}
				// Action using condition
				else {
					ruleActionState = evaluation.getStates().get(ruleAction.getConditionId());
					if(ruleActionState == null) {
						throw new UnsupportedOperationException(String.format("No state matching condition %s in rule %s", ruleAction.getConditionId(), ruleDescription));
					}
					entity = ruleActionState.reference();
				}

				logger.info(
					"Execute action {} with parameters {} on {}: [{}]",
					ruleAction.getActionId(),
					ruleAction.getParameters(), entity.name(),
					ruleActionState.getEvaluables(entity).stream().map(Evaluable::getId).collect(Collectors.joining(","))
				);

				final var action = rulableEntityBinderService.getAction(entity, ruleAction.getActionId());
				for(final var evaluable : ruleActionState.getEvaluables(entity)) {
					action.action(evaluable, parameters, context, message, data);
				}
			}

			logger.info("Rule [{}] executed successfully", ruleDescription);
		}
		catch(final Exception e) {
			logger.error(String.format("Rule [%s] failed", ruleDescription), e);
		}

		return true;
	}
}
