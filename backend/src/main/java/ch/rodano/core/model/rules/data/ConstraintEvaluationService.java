package ch.rodano.core.model.rules.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.exceptions.RuleBreakException;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.configuration.model.rules.RuleBreakType;
import ch.rodano.configuration.model.rules.RuleCondition;
import ch.rodano.configuration.model.rules.RuleConditionListEvaluationMode;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.model.rules.formula.FormulaParserService;
import ch.rodano.core.model.rules.formula.exception.UnableToCalculateFormulaException;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.rule.RulableEntityBinderService;

@Service
public class ConstraintEvaluationService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private final RulableEntityBinderService rulableEntityBinderService;
	private final FormulaParserService formulaParserService;
	private final StudyService studyService;

	public ConstraintEvaluationService(final RulableEntityBinderService rulableEntityBinderService, final FormulaParserService formulaParserService, final StudyService studyService) {
		this.rulableEntityBinderService = rulableEntityBinderService;
		this.formulaParserService = formulaParserService;
		this.studyService = studyService;
	}

	//TODO improve this
	//side effect oriented programming
	public void evaluate(final DataEvaluation dataEvaluation) {
		//store result in the data evaluation
		dataEvaluation.valid = evaluate(dataEvaluation, dataEvaluation.getConstraint());

		//store dependencies
		dataEvaluation.dependencies = dataEvaluation.getDependenciesConditions().stream()
			.map(c -> c.getRuleConditionId().toString())
			.map(c -> dataEvaluation.getStates().get(c))
			.filter(Objects::nonNull)
			.flatMap(s -> s.getReferenceEvaluables().stream())
			.toList();
	}

	private boolean evaluate(final DataEvaluation dataEvaluation, final RuleConstraint constraint) {
		//evaluate root conditions using [and] operator
		boolean isValid = true;

		if(constraint != null) {
			//conditions must be evaluated in a precise order, which is the order of the rulable enum
			//this is very important because a condition may be referenced by another one and the referenced condition must be evaluated first
			for(final var entity : RulableEntity.values()) {
				if(constraint.getConditions().containsKey(entity)) {
					final var conditionList = constraint.getConditions().get(entity);
					boolean isValidEntity = true;
					DataState lastState = null;

					if(!conditionList.getConditions().isEmpty()) {
						//evaluate entity conditions using [mode] operator
						isValidEntity = RuleConditionListEvaluationMode.AND.equals(conditionList.getMode());

						for(final RuleCondition condition : conditionList.getConditions()) {
							final DataState specificState = new DataState(dataEvaluation.getInitialState()).withReference(entity);

							//all conditions must be evaluated to retrieve data state for each condition
							try {
								final boolean evaluation = evaluate(dataEvaluation, specificState, condition);
								lastState = dataEvaluation.getStates().get(condition.getRuleConditionId().toString());
								//[and] operator
								if(RuleConditionListEvaluationMode.AND.equals(conditionList.getMode()) && !evaluation) {
									isValidEntity = false;
								}
								//[or] operator
								if(RuleConditionListEvaluationMode.OR.equals(conditionList.getMode()) && evaluation) {
									isValidEntity = true;
								}
							}
							catch(final RuleBreakException e) {
								//rule break stop execution
								return e.isValid();
							}
							catch(final Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
					else {
						// empty condition list = match all, use initial state for this entity
						lastState = new DataState(dataEvaluation.getInitialState()).withReference(entity);
					}

					// save final state keyed by the condition list ID so rule actions can reference it
					if(conditionList.getRuleConditionListId() != null) {
						final DataState stateToSave = lastState != null
							? lastState
							: new DataState(dataEvaluation.getInitialState()).withReference(entity);
						dataEvaluation.getStates().put(conditionList.getRuleConditionListId().toString(), stateToSave);
					}

					if(!isValidEntity) {
						isValid = false;
					}
				}
			}
		}

		return isValid;
	}

	private boolean evaluate(final DataEvaluation dataEvaluation, final DataState state, final RuleCondition condition) throws Exception {
		final var criterion = condition.getCriterion();
		final DataState resultState;

		//attribute
		if(rulableEntityBinderService.attributeExists(state.reference(), condition.getCriterion().getProperty())) {

			//build results list
			Set<Evaluable> results = new HashSet<>();
			if(StringUtils.isNotBlank(criterion.getConditionId())) {
				final boolean valid = state.getReferenceEvaluables().equals(dataEvaluation.getStates().get(criterion.getConditionId()).getReferenceEvaluables());
				results = valid ? state.getReferenceEvaluables() : Collections.emptySet();
			}
			else {
				//retrieve property
				final EntityAttribute property = rulableEntityBinderService.getAttribute(state.reference(), criterion.getProperty());

				//transform value in object
				final Set<Object> values = new HashSet<>();
				for(final String value : criterion.getValues()) {
					//formula
					if(value.startsWith("=")) {
						try {
							values.add(formulaParserService.parse(value, dataEvaluation.getStates()));
						}
						catch(final UnableToCalculateFormulaException e) {
							logger.error("Unable to calculate formula", e);
						}
					}
					else {
						switch(property.getType()) {
							case DATE:
								if(value.length() == 4 && value.matches("\\d{4}")) {
									values.add(ZonedDateTime.of(LocalDate.of(Integer.parseInt(value), 1, 1), LocalTime.MIDNIGHT, ZoneId.of("UTC")));
								}
								else {
									values.add(ZonedDateTime.of(LocalDate.parse(value, DATE_FORMAT), LocalTime.MIDNIGHT, ZoneId.of("UTC")));
								}
								break;
							case NUMBER:
								values.add(Double.valueOf(value));
								break;
							case BOOLEAN:
								values.add(Boolean.valueOf(value));
								break;
							default:
								try {
									values.add(UUID.fromString(value));
								}
								catch(IllegalArgumentException e) {
									final UUID convertedUuid = convertStringIdToUuid(state.reference(), criterion.getProperty(), value);
									if(convertedUuid != null) {
										values.add(convertedUuid);
									}
									else {
										if(state.reference() == RulableEntity.FIELD && "VALUE".equals(criterion.getProperty())) {
											final UUID possibleValueUuid = convertPossibleValueToUuid(state, value);
											values.add(Objects.requireNonNullElse(possibleValueUuid, value));
										}
										else {
											values.add(value);
										}
									}
								}
								break;
						}
					}
				}

				//test each evaluable
				for(final Evaluable evaluable : state.getReferenceEvaluables()) {
					final Operator operator = criterion.getOperator();
					boolean valid = false;//operator.isNegate();
					final Object evaluableProperty = property.getValue(evaluable);

					if(operator.hasValue()) {
						for(final Object value : values) {
							final boolean test = operator.test(property.getType(), evaluableProperty, value);
							logger.debug("Doing a [{}] operation. Is [{}] [{}] [{}]? Result is [{}]", property.getType(), evaluableProperty, operator.name(), value, test ? "valid" : "invalid");
							if(test) {
								valid = true;
								//no need to continue, one success is enough
								break;
							}
							/*if(operator.isNegate() && !test || !operator.isNegate() && test) {
								valid = !operator.isNegate();
								break;
							}*/
						}
					}
					else {
						if(operator.test(property.getType(), evaluableProperty)) {
							valid = true;
						}
					}
					if(valid) {
						results.add(evaluable);
					}
				}
			}

			//reference is not modified, only results
			resultState = state.withEvaluables(state.reference(), results);
		}
		else {
			//relations
			if(criterion.getProperty() == null && StringUtils.isBlank(criterion.getConditionId())) {
				dataEvaluation.getStates().put(condition.getRuleConditionId().toString(), state);
				dataEvaluation.getStates().put(condition.getId(), state);
				return true;
			}
			final EntityRelation relation = rulableEntityBinderService.getRelation(state.reference(), criterion.getProperty());

			//retrieve results of relation
			final Set<Evaluable> results = new HashSet<>();

			for(final Evaluable evaluable : state.getReferenceEvaluables()) {
				results.addAll(relation.getTargetEvaluables(evaluable));
			}

			resultState = state.withReference(relation.getTargetEntity()).withEvaluables(relation.getTargetEntity(), results);
		}
		//save result
		//TODO fix this
		dataEvaluation.getStates().put(condition.getRuleConditionId().toString(), resultState);
		dataEvaluation.getStates().put(condition.getId(), resultState);

		//check validity
		final boolean isValid = condition.isInverse() ^ resultState.isValid();

		//manage break if rule is not valid
		if(!isValid && !RuleBreakType.NONE.equals(condition.getBreakType())) {
			throw new RuleBreakException(RuleBreakType.ALLOW.equals(condition.getBreakType()));
		}

		logger.debug("Criterion [{}] {} {} with {}", condition.getId(), condition.isInverse() ? "[inversed]" : "", isValid ? "is valid" : "is not valid", resultState);

		//no need to check validity on children if there is no children
		boolean areChildrenValid = true;
		if(!condition.getConditions().isEmpty()) {
			//evaluate children conditions
			//all conditions must be evaluated anyway to build data state for each condition
			areChildrenValid = RuleConditionListEvaluationMode.AND.equals(condition.getMode());
			for(final RuleCondition childCondition : condition.getConditions()) {
				final boolean evaluation = evaluate(dataEvaluation, resultState, childCondition);

				//[and] operator
				if(RuleConditionListEvaluationMode.AND.equals(condition.getMode()) && !evaluation) {
					areChildrenValid = false;
				}

				//[or] operator
				if(RuleConditionListEvaluationMode.OR.equals(condition.getMode()) && evaluation) {
					areChildrenValid = true;
				}
			}
		}
		//return validity for current and children
		return isValid && areChildrenValid;
	}

	private UUID convertStringIdToUuid(final RulableEntity entity, final String property, final String stringId) {
		final var study = studyService.getStudy();

		if(entity == RulableEntity.WORKFLOW && "STATUS".equals(property)) {
			return study.getWorkflows().stream()
				.flatMap(w -> w.getStates().stream())
				.filter(s -> s.getId().equals(stringId))
				.findFirst()
				.map(WorkflowState::getWorkflowStateId)
				.orElse(null);
		}

		return switch(entity) {
			case SCOPE -> study.getScopeModel(stringId).getScopeModelId();
			case DATASET -> study.getDatasetModel(stringId).getDatasetModelId();
			case FIELD -> study.getFieldModels().stream()
				.filter(f -> f.getId().equals(stringId))
				.findFirst()
				.map(FieldModel::getFieldModelId)
				.orElse(null);
			case WORKFLOW -> study.getWorkflow(stringId).getWorkflowId();
			default -> null;
		};
	}

	private UUID convertPossibleValueToUuid(final DataState state, final String possibleValueCode) {
		if(state.getReferenceEvaluables().isEmpty()) {
			return null;
		}

		try {
			final var evaluable = state.getReferenceEvaluables().iterator().next();
			if(evaluable instanceof ch.rodano.core.model.field.Field field) {
				final var fieldModel = field.getFieldModel();
				if(fieldModel != null && !fieldModel.getPossibleValues().isEmpty()) {
					return fieldModel.getPossibleValue(possibleValueCode).getPossibleValueId();
				}
			}
		}
		catch(Exception e) {
			logger.debug("Could not convert possible value code to UUID: {}", possibleValueCode, e);
		}

		return null;
	}
}
