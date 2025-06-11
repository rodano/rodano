package ch.rodano.core.services.bll.field;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.PossibleValue;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.model.workflow.WorkflowAction;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.FieldAuditTrail;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.field.FieldRecord;
import ch.rodano.core.model.rules.data.ConstraintEvaluationService;
import ch.rodano.core.model.rules.data.DataEvaluation;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.rules.formula.FormulaParserService;
import ch.rodano.core.model.rules.formula.exception.UnableToCalculateFormulaException;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.file.FileService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.study.SubstudyService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.plugin.export.ExportPluginService;
import ch.rodano.core.services.plugin.pv.PossibleValuesPluginService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.core.services.rule.RuleService;

@Service
public class FieldServiceImpl implements FieldService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final SubstudyService substudyService;
	private final FieldDAOService fieldDAOService;
	private final WorkflowStatusService workflowStatusService;
	private final RuleService ruleService;
	private final FileService fileService;
	private final StudyService studyService;
	private final ExportPluginService exportPluginService;
	private final PossibleValuesPluginService possibleValuesPluginService;
	private final ConstraintEvaluationService constraintEvaluationService;
	private final FormulaParserService formulaParserService;

	public FieldServiceImpl(
		@Lazy final SubstudyService substudyService,
		final FieldDAOService fieldDAOService,
		final WorkflowStatusService workflowStatusService,
		final RuleService ruleService,
		final FileService fileService,
		final StudyService studyService,
		final ExportPluginService exportPluginService,
		final ConstraintEvaluationService constraintEvaluationService,
		final FormulaParserService formulaParserService,
		final PossibleValuesPluginService possibleValuesPluginService
	) {
		this.substudyService = substudyService;
		this.fieldDAOService = fieldDAOService;
		this.workflowStatusService = workflowStatusService;
		this.ruleService = ruleService;
		this.fileService = fileService;
		this.studyService = studyService;
		this.exportPluginService = exportPluginService;
		this.possibleValuesPluginService = possibleValuesPluginService;
		this.constraintEvaluationService = constraintEvaluationService;
		this.formulaParserService = formulaParserService;
	}

	@Override
	public Field create(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final FieldModel fieldModel,
		final DatabaseActionContext context,
		final String rationale
	) {
		final var document = dataset.getDatasetModel();

		//check that field is allowed in the dataset
		if(!document.getFieldModels().contains(fieldModel)) {
			throw new NoRespectForConfigurationException(
				String.format(
					"Field model %s is not allowed in %s",
					fieldModel.getLocalizedShortname("en"),
					document.getLocalizedShortname("en")
				)
			);
		}

		final var field = new Field();
		field.setDatasetModel(document);
		field.setFieldModel(fieldModel);
		field.setDatasetFk(dataset.getPk());

		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create field" : "Create field: " + rationale;
		fieldDAOService.saveField(field, context, enhancedRationale);
		final var family = new DataFamily(scope, event, dataset, field);
		workflowStatusService.createAll(family, field, Collections.emptyMap(), context, enhancedRationale);

		return field;
	}

	@Override
	public List<Field> createAll(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final DatabaseActionContext context,
		final String rationale
	) {
		return dataset.getDatasetModel().getFieldModels().stream()
			.sorted()
			.map(f -> create(scope, event, dataset, f, context, rationale))
			.toList();
	}

	@Override
	public List<PossibleValue> getPossibleValues(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field
	) {
		final var fieldModel = field.getFieldModel();
		if(fieldModel.hasPossibleValuesProvider()) {
			return possibleValuesPluginService.provide(scope, event, dataset, field);
		}
		return fieldModel.getPossibleValues();
	}

	@Override
	public String valueToLabel(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final String value,
		final String... languages
	) {
		final var possibleValues = getPossibleValues(scope, event, dataset, field);
		return field.getFieldModel().valueToLabel(possibleValues, value, languages);
	}

	@Override
	public void updateValue(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final String value,
		final DatabaseActionContext context,
		final String rationale
	) throws InvalidValueException, BadlyFormattedValue {
		//no null value can be set
		if(value == null) {
			throw new IllegalArgumentException("Null value is not allowed");
		}
		final var fieldModel = field.getFieldModel();

		//do basic check on provided value
		//do not run validators
		final var possibleValues = getPossibleValues(scope, event, dataset, field);
		final var valueCheck = fieldModel.checkAndSanitizeValue(possibleValues, value);
		if(valueCheck.hasError()) {
			throw new InvalidValueException(field, value, valueCheck.getError());
		}
		if(!valueCheck.getSanitizedValue().equals(value)) {
			throw new BadlyFormattedValue(field, value, valueCheck.getSanitizedValue());
		}

		final var isUpdated = !value.equals(field.getValue());

		//manage file associated to the field
		//even if the value of the field (which is the concatenation of the name of the file and its checksum) has not changed, there is something to do
		//that's because the same file (same name and same checksum) could have been re-uploaded (removing the existing file and re-adding the same one, client side)
		//in this case, there is new row in the file SQL table containing the new file and we must deal with this row
		if(OperandType.BLOB.equals(fieldModel.getDataType())) {
			//retrieve existing file and the new file being uploaded
			//these files may be null or identical (if the same file has been reuploaded
			final var previousFile = fileService.getFile(field);
			final var newFile = fileService.getUnsubmittedFile(field);
			//handle the case when the same file has been re-uploaded
			//checking if both files are the same is the same as testing the value of the field
			if(!isUpdated) {
				//in this case, get rid of the new file
				//only the previous copy will be kept
				if(newFile != null) {
					fileService.deleteFile(newFile);
				}
			}
			else {
				//if a file already exist for this field and is different, it must be linked to the latest audit trail of the field and deleted
				if(previousFile != null) {
					previousFile.setTrailFk(getLastAuditTrail(field).getPk());
					fileService.saveFile(previousFile, context, "Save field");
				}
				//handle new file only if field has a field value and the file has been uploaded
				if(StringUtils.isNotBlank(value)) {
					if(newFile == null) {
						throw new UnsupportedOperationException("A file must have been uploaded");
					}
					newFile.setSubmitted(true);
					fileService.saveFile(newFile, context, rationale);
				}
			}
		}

		field.setValue(value);
		fieldDAOService.saveField(field, context, rationale);

		if(isUpdated) {
			//some substudies may be interested in the scope of this field
			//do this only if at least one substudy uses the field model as a criteria
			final var fieldModelSubstudies = substudyService.getOpenSubstudiesByFieldModel(fieldModel);
			if(!fieldModelSubstudies.isEmpty()) {
				final var substudies = substudyService.filterPotentialSubstudies(fieldModelSubstudies, scope);
				substudyService.enrollScopeInSubstudies(scope, substudies, context, "Add in virtual scope because a field has been updated");
				for(final var substudy : substudies) {
					logger.debug("Scope {} enrolled in virtual scope {} because field {} value changed to [{}]", scope.getCode(), substudy.getCode(), field.getFieldModelId(), value);
				}
			}

			executeRules(scope, event, dataset, field, context);
		}
	}

	@Override
	public String getDefaultValue(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field
	) {
		final var fieldModel = field.getFieldModel();
		//only non plugin can have a default value
		if(!fieldModel.isPlugin()) {
			if(fieldModel.hasValueFormula()) {
				return calculateFormulaValue(scope, event, dataset, field);
			}
		}
		return null;
	}

	@Override
	public String getPluginValue(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field
	) {
		final var fieldModel = field.getFieldModel();
		//only plugin can have a plugin value
		if(!fieldModel.isPlugin()) {
			return "";
		}
		if(fieldModel.hasValueFormula()) {
			return calculateFormulaValue(scope, event, dataset, field);
		}
		return exportPluginService.calculate(scope, event, dataset, field);
	}

	@Override
	public Object getValueObject(final Field field) {
		return getValueObject(field, Optional.empty());
	}

	@Override
	public Object getValueObject(final Field field, final Optional<ZonedDateTime> date) {
		return field.getFieldModel().stringToObject(getLatestValue(field, date));
	}

	@Override
	public String getLatestValue(final Field field, final Optional<ZonedDateTime> date) {
		if(date.isPresent()) {
			final var timeframe = new Timeframe(Optional.empty(), date);
			//date parameter could be before this field even exists
			//in this case, there will no field versions
			//in this case return null, because it does not make any sense to ask for the value of a field at a time when the field didnt exist
			final var fieldVersions = fieldDAOService.getAuditTrailsForProperty(field, Optional.of(timeframe), FieldRecord::getValue);
			return !fieldVersions.isEmpty() ? fieldVersions.last().getValue() : null;
		}
		return field.getValue();
	}

	@Override
	public String getValueLabel(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final Optional<ZonedDateTime> date,
		final String... languages
	) {
		final var value = getLatestValue(field, date);
		return valueToLabel(scope, event, dataset, field, value, languages);
	}

	@Override
	public String getInterpretedValue(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final Optional<ZonedDateTime> date
	) {
		//if a date is provided, return past value without any interpretation
		//we don't want to interfere with the past :)
		if(date.isPresent()) {
			return getLatestValue(field, date);
		}
		//if no date is provided, we return the default value if the value is blank
		if(field.isNull()) {
			return getDefaultValue(scope, event, dataset, field);
		}
		return field.getValue();
	}

	@Override
	public void reset(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final DatabaseActionContext context,
		final String rationale
	) {
		final var family = new DataFamily(scope, event, dataset, field);
		workflowStatusService.resetMandatoryAndDeleteTheRest(family, field, context, rationale);

		//reset field value and execute rules only if field has a value
		if(field.getValue() != null) {
			field.setValue(null);
			fieldDAOService.saveField(field, context, rationale);
			executeRules(scope, event, dataset, field, context);
		}
	}

	@Override
	public List<Field> getAll(final Dataset dataset) {
		final var fieldModelIds = dataset.getDatasetModel().getFieldModels().stream().map(FieldModel::getId).toList();
		return fieldDAOService.getFieldsByDatasetPkHavingFieldModelIds(dataset.getPk(), fieldModelIds);
	}

	@Override
	public List<Field> getAll(final Dataset dataset, final Collection<FieldModel> fieldModels) {
		final var fieldModelIds = fieldModels.stream()
			.map(FieldModel::getId)
			.toList();
		return fieldDAOService.getFieldsByDatasetPkHavingFieldModelIds(dataset.getPk(), fieldModelIds);
	}

	@Override
	public Field get(final Dataset dataset, final FieldModel fieldModel) {
		return fieldDAOService.getFieldsByDatasetPkHavingFieldModelIds(dataset.getPk(), Collections.singleton(fieldModel.getId())).stream()
			.findFirst()
			.orElseThrow();
	}

	@Override
	public List<Field> getAll(final Scope scope, final Optional<Event> event) {
		return fieldDAOService.getFieldsRelatedToEvent(scope.getPk(), event.map(Event::getPk));
	}

	@Override
	public Optional<Field> get(final WorkflowStatus workflowStatus) {
		//workflow status may not be linked to a field
		return Optional.ofNullable(workflowStatus.getFieldFk()).map(fieldDAOService::getFieldByPk);
	}

	public List<Field> getSearchableFieldsOnScopes(final List<Scope> scopes) {
		// searchablefieldModels
		final var searchableFieldModel = studyService.getStudy().getLeafScopeModel().getDatasetModels().stream()
			.flatMap(dm -> dm.getFieldModels().stream())
			.filter(FieldModel::isSearchable)
			.map(FieldModel::getId)
			.collect(Collectors.toList());

		return fieldDAOService.getSearchableFields(
			scopes.stream().map(Scope::getPk).collect(Collectors.toList()),
			searchableFieldModel
		);
	}

	private void executeRules(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final DatabaseActionContext context
	) {
		final var fieldModel = field.getFieldModel();
		final var fieldModelRules = fieldModel.getRules();
		final var triggerRules = studyService.getStudy().getEventActions().getOrDefault(WorkflowAction.UPDATE_VALUE, Collections.emptyList());

		if(!fieldModelRules.isEmpty() || !triggerRules.isEmpty()) {
			//execute rules
			final var state = new DataState(scope, event, dataset, field);
			//execute modifications rules
			try {
				ruleService.execute(state, fieldModelRules, context);
			}
			catch(final Exception e) {
				logger.error(e.getLocalizedMessage(), e);
			}
			//execute trigger rules
			try {
				ruleService.execute(state, triggerRules, context);
			}
			catch(final Exception e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
	}

	private FieldAuditTrail getLastAuditTrail(final Field field) {
		return fieldDAOService.getAuditTrails(field, Optional.empty(), Optional.empty()).last();
	}

	private String calculateFormulaValue(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field
	) {
		final var fieldModel = field.getFieldModel();
		//return raw value if it's not a formula
		if(!fieldModel.getValueFormula().startsWith("=")) {
			return fieldModel.getValueFormula();
		}
		//execute rules to get the value if it's a formula
		final var dataState = new DataState(scope, event, dataset, field);
		final var evaluation = new DataEvaluation(dataState, fieldModel.getValueConstraint());
		constraintEvaluationService.evaluate(evaluation);
		if(evaluation.isValid()) {
			try {
				final var result = formulaParserService.parse(fieldModel.getValueFormula(), evaluation.getStates());
				if(result != null) {
					//if value is already a string, no need to transform it
					if(result instanceof String) {
						return (String) result;
					}
					return fieldModel.objectToString(result);
				}
			}
			catch(final UnableToCalculateFormulaException e) {
				logger.error("Unable to calculate value using value formula", e);
			}
		}
		//returns the empty string value if constraint can no be evaluated or if formula can not be parsed
		return "";
	}
}
