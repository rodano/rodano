package ch.rodano.core.services.bll.field;

import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.rules.data.ConstraintEvaluationService;
import ch.rodano.core.model.rules.data.DataEvaluation;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.plugin.validator.ValidatorPluginService;
import ch.rodano.core.services.plugin.validator.exception.ValidatorException;

@Service
public class ValidationServiceImpl implements ValidationService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final ValidatorPluginService validatorPluginService;
	private final WorkflowStatusService workflowStatusService;
	private final ConstraintEvaluationService constraintEvaluationService;

	public ValidationServiceImpl(
		final StudyService studyService,
		final ValidatorPluginService validatorPluginService,
		final WorkflowStatusService workflowStatusService,
		final ConstraintEvaluationService constraintEvaluationService
	) {
		this.studyService = studyService;
		this.validatorPluginService = validatorPluginService;
		this.workflowStatusService = workflowStatusService;
		this.constraintEvaluationService = constraintEvaluationService;
	}

	@Override
	public void applyNonBlockingValidators(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field
	) throws ValidatorException {
		final var validators = field.getFieldModel().getValidators().stream()
			.filter(v -> !v.isBlocking()).sorted(Validator.COMPARATOR_IMPORTANCE)
			.toList();
		for(final var validator : validators) {
			checkValue(scope, event, dataset, field, validator);
		}
	}

	@Override
	public void applyBlockingValidators(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field
	) throws ValidatorException {
		for(final var validator : field.getFieldModel().getValidators()) {
			if(validator.isBlocking()) {
				checkValue(scope, event, dataset, field, validator);
			}
		}
	}

	@Override
	public void checkValue(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final Validator validator
	) throws ValidatorException {
		logger.info("Checking field [{}] with value [{}] against validator [{}]", field.getId(), field.getValue(), validator.getId());

		// required validator
		if(validator.isRequired() && field.isBlank()) {
			logger.debug("Field [{}] with blank value is invalid according to required validator [{}]", field.getId(), validator.getId());
			throw new ValidatorException(field, validator);
		}

		// script validators
		if(validator.isScript()) {
			if(!validatorPluginService.validate(validator, scope, event, dataset, field)) {
				logger.debug("Field [{}] with value [{}] is invalid according to script validator [{}]", field.getId(), field.getValue(), validator.getId());
				throw new ValidatorException(field, validator);
			}
			return;
		}

		// rules validators
		if(validator.getConstraint() != null) {
			final var state = new DataState(scope, event, dataset, field);
			final var evaluation = new DataEvaluation(state, validator.getConstraint());
			constraintEvaluationService.evaluate(evaluation);
			if(!evaluation.isValid()) {
				logger.debug("Field [{}] with value [{}] is invalid according to rules validator [{}]", field.getId(), field.getValue(), validator.getId());
				throw new ValidatorException(field, validator, evaluation);
			}
		}
	}

	@Override
	public void validateField(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final DatabaseActionContext context,
		final String rationale
	) {
		//do not validate fields that don't have any validator
		if(field.getFieldModel().getValidatorIds().isEmpty()) {
			return;
		}

		//do not validate fields that are in a deleted element
		if(dataset.getDeleted() || event.isPresent() && event.get().getDeleted() || scope.getDeleted()) {
			logger.info("Skipping validation for field [{}] that is inside a deleted element (dataset, event or scope)", field.getId());
			return;
		}

		final var family = new DataFamily(scope, event, dataset, field);

		logger.info("Validating field [{}] with value [{}]", field.getId(), field.getValue());

		//DO NOT HANDLE MANUAL WORKFLOWS HERE, ONLY VALIDATION WORKFLOWS
		try {
			applyNonBlockingValidators(scope, event, dataset, field);
			logger.info("Field [{}] is valid", field.getId());

			//retrieve the list of validation workflow statuses that are still "open"
			//that means workflow statuses that have been created by a validator and are in the validator invalid state
			final var runningValidationStatuses = workflowStatusService.getAll(field).stream()
				.filter(ws -> StringUtils.isNotBlank(ws.getValidatorId()))
				.filter(ws -> ws.getStateId().equals(ws.getValidator().getInvalidStateId()))
				.toList();

			//put theses statuses in their valid state (according to their validator) if any
			for(final var status : runningValidationStatuses) {
				logger.info("Field [{}], existing workflow [{}] in state [{}] will be discarded (set to valid state) because field is now valid", field.getId(), status.getId(), status.getStateId());
				final var validWorkflowState = status.getValidator().getValidWorkflowState();
				if(validWorkflowState != null) {
					workflowStatusService.updateState(family, status, validWorkflowState, Collections.emptyMap(), context, "Re-assessing due to value change. Validation criteria satisfied.");
				}
			}
		}
		catch(final ValidatorException validationException) {
			//keep a hook on validation data
			final var validator = validationException.getValidator();
			final var workflow = validator.getWorkflow();
			var validationAlreadyDone = false;

			logger.info("Field [{}] is invalid according to validator [{}]", field.getId(), validator.getId());

			//retrieve the list of all validation workflow statuses that are related to the field
			//that means workflow statuses that have been created by a validator
			final var validationStatuses = workflowStatusService.getAll(field).stream()
				.filter(ws -> StringUtils.isNotBlank(ws.getValidatorId()))
				.toList();

			//handle each validation workflow statuses
			for(final var status : validationStatuses) {

				//now, the goal is to find if the validation error that has just been triggered matches an existing workflow status (either an "open" workflow status or an old workflow status)
				//check if workflow is the the same
				var contextChanged = !workflow.getId().equals(status.getWorkflowId());
				if(contextChanged) {
					logger.debug("Field [{}], different context: current workflow [{}] - existing workflow [{}]", field.getId(), workflow.getId(), status.getWorkflowId());
				}

				if(!contextChanged) {
					//check if validator is the same
					contextChanged |= !validator.getId().equals(status.getValidatorId());
					if(contextChanged) {
						logger.debug(
							"Field [{}], different context for workflow [{}]: current validator [{}] - workflow validator [{}]", field.getId(), status.getId(), validator.getId(), status
								.getValidatorId()
						);
					}
				}

				//check if field value has been updated since the workflow status has been created
				if(!contextChanged) {
					contextChanged |= field.getLastUpdateTime().isAfter(status.getCreationTime());
					if(contextChanged) {
						logger.debug(
							"Field [{}], different context for workflow [{}]: field last update time [{}] - status creation time [{}]", field.getId(), status.getId(), field
								.getLastUpdateTime(), status.getCreationTime()
						);
					}
				}

				//check if dependencies has been updated since the workflow status has been created
				if(!contextChanged && validationException.getEvaluation() != null) {
					final var hasChangedDependencies = validationException.getEvaluation().getDependenciesHaveChangedSince(status.getLastUpdateTime());
					contextChanged |= hasChangedDependencies;
					if(contextChanged) {
						logger.debug("Field [{}], different context for workflow [{}]: dependencies values have changed since the creation of the workflow", field.getId(), status.getId());
					}
				}

				//now we know there is already an "open" or old workflow status matching current validation error
				//handle running workflows that are related to an old context
				if(contextChanged) {
					//care, do not touch workflows that are "closed" (not in their invalid state)
					if(status.getValidator().getInvalidStateId().equals(status.getStateId())) {
						//context has changed since the creation of the workflow status
						logger.info(
							"Field [{}], existing workflow [{}] in state [{}] will be discarded (set to valid state) and a new workflow will be created", field.getId(), status.getId(), status
								.getStateId()
						);

						//these existing workflows must be handled
						final var validWorkflowState = status.getValidator().getValidWorkflowState();
						if(validWorkflowState != null) {
							//if workflow has been created automatically (i.e. from a validator) put it in its valid state
							//that means in the valid state for the validator that has issued this workflow
							workflowStatusService.updateState(family, status, status.getValidator().getValidWorkflowState(), Collections.emptyMap(), context, "Re-assessing due to value change");
						}
					}
				}
				else {
					logger.debug("Field [{}], found that context has not changed. Existing old workflow [{}] will be kept", field.getId(), status.getId());
					validationAlreadyDone = true;
				}
			}

			if(!validationAlreadyDone) {
				//create the workflow status triggered by validator
				final var message = validationException.getUserFriendlyErrorMessage(studyService.getStudy().getDefaultLanguageId());
				//do not send a role to workflow initialization
				//workflows triggered by validation are considered "system" workflows
				workflowStatusService.create(
					family,
					field,
					workflow,
					Optional.of(validator.getInvalidWorkflowState()),
					Optional.empty(),
					Optional.of(validator),
					Optional.empty(),
					Collections.emptyMap(),
					context,
					message
				);

				logger.info("Field [{}] with value [{}], automatic [{}] generated with message [{}]", field.getId(), field.getValue(), workflow.getId(), message);
			}
		}
		catch(final Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

}
