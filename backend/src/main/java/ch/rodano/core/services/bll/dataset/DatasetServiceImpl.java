package ch.rodano.core.services.bll.dataset;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.workflow.WorkflowAction;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.LockedObjectException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.field.ValidationService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.core.services.rule.RuleService;
import ch.rodano.core.utils.ACL;
import ch.rodano.core.utils.UtilsServiceImpl;

@Service
public class DatasetServiceImpl implements DatasetService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final DatasetDAOService datasetDAOService;
	private final FieldService fieldService;
	private final FieldDAOService fieldDAOService;
	private final RuleService ruleService;
	private final ValidationService validationService;
	private final AuditActionService auditActionService;
	private final PlatformTransactionManager transactionManager;
	private final UtilsServiceImpl utilsService;

	public DatasetServiceImpl(
		final StudyService studyService,
		final DatasetDAOService datasetDAOService,
		final FieldService fieldService,
		final FieldDAOService fieldDAOService,
		final RuleService ruleService,
		final ValidationService validationService,
		final AuditActionService auditActionService,
		final PlatformTransactionManager transactionManager,
		final UtilsServiceImpl utilsService
	) {
		this.studyService = studyService;
		this.datasetDAOService = datasetDAOService;
		this.fieldService = fieldService;
		this.fieldDAOService = fieldDAOService;
		this.ruleService = ruleService;
		this.validationService = validationService;
		this.auditActionService = auditActionService;
		this.transactionManager = transactionManager;
		this.utilsService = utilsService;
	}

	/**
	 * This function programmatically crates its own transaction and sets it to "rollback only"
	 * in order to roll back the transaction.
	 */
	@Override
	public Dataset createCandidate(
		final Scope scope,
		final Optional<Event> event,
		final DatasetModel datasetModel,
		final Actor actor
	) {
		final var transactionTemplate = new TransactionTemplate(transactionManager);

		return transactionTemplate.execute(status -> {
			status.setRollbackOnly();

			final var context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, "Create candidate dataset");

			if(event.isPresent()) {
				return create(scope, event.get(), datasetModel, context, "Create candidate dataset");
			}
			return create(scope, datasetModel, context, "Create candidate dataset");
		});
	}

	@Override
	public List<Dataset> createAll(final Scope scope, final DatabaseActionContext context, final String rationale) {
		return scope.getScopeModel().getDatasetModels().stream()
			.filter(d -> !d.isMultiple())
			.map(d -> create(scope, d, context, rationale))
			.toList();
	}

	@Override
	public List<Dataset> createAll(final Scope scope, final Event event, final DatabaseActionContext context, final String rationale) {
		return event.getEventModel().getDatasetModels().stream()
			.filter(d -> !d.isMultiple())
			.map(d -> create(scope, event, d, context, rationale))
			.toList();
	}

	@Override
	public Dataset create(final Scope scope, final DatasetModel datasetModel, final DatabaseActionContext context, final String rationale) {
		return create(scope, datasetModel, context, rationale, Optional.empty());
	}

	@Override
	public Dataset create(final Scope scope, final DatasetModel datasetModel, final DatabaseActionContext context, final String rationale, final Optional<String> id) {
		//check that scope is not locked
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}

		//check that the dataset model is allowed for the scope model
		if(!scope.getScopeModel().getDatasetModelIds().contains(datasetModel.getId())) {
			throw new NoRespectForConfigurationException(
				String.format(
					"Dataset model %s is not allowed for the scope model %s", datasetModel.getId(), scope.getScopeModel().getId()
				)
			);
		}

		//dataset
		final var dataset = new Dataset();
		id.ifPresent(dataset::setId);
		dataset.setDatasetModel(datasetModel);
		dataset.setScopeFk(scope.getPk());

		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create dataset" : "Create dataset: " + rationale;
		datasetDAOService.saveDataset(dataset, context, enhancedRationale);
		fieldService.createAll(scope, Optional.empty(), dataset, context, enhancedRationale);

		//trigger rules
		final var rules = studyService.getStudy().getEventActions().get(WorkflowAction.CREATE_DATASET);
		if(rules != null && !rules.isEmpty()) {
			final var state = new DataState(scope, Optional.empty(), dataset);
			ruleService.execute(state, rules, context);
		}

		return dataset;
	}

	@Override
	public Dataset create(final Scope scope, final Event event, final DatasetModel datasetModel, final DatabaseActionContext context, final String rationale) {
		return create(scope, event, datasetModel, context, rationale, Optional.empty());
	}

	@Override
	public Dataset create(final Scope scope, final Event event, final DatasetModel datasetModel, final DatabaseActionContext context, final String rationale, final Optional<String> id) {
		//check that scope and event are not locked
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}
		if(event.getLocked()) {
			throw new LockedObjectException(event);
		}

		//check that the dataset model is allowed for the event model
		if(!event.getEventModel().getDatasetModelIds().contains(datasetModel.getId())) {
			throw new NoRespectForConfigurationException(
				String.format(
					"Dataset model %s is not allowed for the event model %s", datasetModel.getId(), event.getEventModelId()
				)
			);
		}

		//dataset
		final var dataset = new Dataset();
		id.ifPresent(dataset::setId);
		dataset.setDatasetModel(datasetModel);
		dataset.setEventFk(event.getPk());

		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create dataset" : "Create dataset: " + rationale;
		datasetDAOService.saveDataset(dataset, context, enhancedRationale);
		fieldService.createAll(scope, Optional.of(event), dataset, context, enhancedRationale);

		//trigger rules
		final var rules = studyService.getStudy().getEventActions().get(WorkflowAction.CREATE_DATASET);
		if(rules != null && !rules.isEmpty()) {
			final var state = new DataState(scope, Optional.of(event), dataset);
			ruleService.execute(state, rules, context);
		}

		return dataset;
	}

	@Override
	public void delete(final Scope scope, final Optional<Event> event, final Dataset dataset, final DatabaseActionContext context, final String rationale) {
		utilsService.checkNotDeleted(scope);
		event.ifPresent(utilsService::checkNotDeleted);

		utilsService.checkNotLocked(scope);
		event.ifPresent(utilsService::checkNotLocked);

		final var datasetModel = dataset.getDatasetModel();

		final var baseRationale = "Dataset removed";
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);
		datasetDAOService.deleteDataset(dataset, context, enhancedRationale);

		final var state = new DataState(scope, event, dataset);

		//execute dataset model removal rules
		var rules = datasetModel.getDeleteRules();
		if(rules != null && !rules.isEmpty()) {
			ruleService.execute(state, rules, context);
		}

		//execute global dataset model removal rules
		rules = studyService.getStudy().getEventActions().get(WorkflowAction.REMOVE_DATASET);
		if(rules != null && !rules.isEmpty()) {
			ruleService.execute(state, rules, context);
		}
	}

	@Override
	public void restore(final Scope scope, final Optional<Event> event, final Dataset dataset, final DatabaseActionContext context, final String rationale) {
		utilsService.checkNotDeleted(scope);
		event.ifPresent(utilsService::checkNotDeleted);

		utilsService.checkNotLocked(scope);
		event.ifPresent(utilsService::checkNotLocked);

		final var datasetModel = dataset.getDatasetModel();

		final var baseRationale = "Dataset restored";
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);
		datasetDAOService.restoreDataset(dataset, context, enhancedRationale);

		validateFieldsOnDataset(scope, event, dataset, context, enhancedRationale);

		final var state = new DataState(scope, event, dataset);

		//execute dataset restoration rules
		var rules = datasetModel.getRestoreRules();
		if(rules != null && !rules.isEmpty()) {
			ruleService.execute(state, rules, context);
		}

		//execute global dataset restoration rules
		rules = studyService.getStudy().getEventActions().get(WorkflowAction.RESTORE_DATASET);
		if(rules != null && !rules.isEmpty()) {
			ruleService.execute(state, rules, context);
		}
	}

	@Override
	public void validateFieldsOnDataset(final Scope scope, final Optional<Event> event, final Dataset dataset, final DatabaseActionContext context, final String rationale) {
		//re-validate all fields that have a value
		final var fields = fieldDAOService.getFieldsFromDatasetWithAValue(dataset.getPk());
		for(final var f : fields) {
			validationService.validateField(scope, event, dataset, f, context.toSystemAction(), rationale);
		}
	}

	@Override
	public void save(final Dataset dataset, final DatabaseActionContext context, final String rationale) {
		datasetDAOService.saveDataset(dataset, context, rationale);
	}

	@Override
	public void reset(final Scope scope, final Optional<Event> event, final Dataset dataset, final DatabaseActionContext context, final String rationale) {
		for(final var f : fieldService.getAll(dataset)) {
			fieldService.reset(scope, event, dataset, f, context, rationale);
		}
	}

	@Override
	public void recalculatePluginValues(final Scope scope, final Optional<Event> event, final Dataset dataset, final DatabaseActionContext context, final String rationale) {
		for(final var fieldModel : dataset.getDatasetModel().getFieldModels()) {
			if(fieldModel.isPlugin()) {
				try {
					final var field = fieldService.get(dataset, fieldModel);
					final var calculatedValue = fieldService.getPluginValue(scope, event, dataset, field);
					fieldService.updateValue(scope, event, dataset, field, calculatedValue, context.toSystemAction(), rationale);
				}
				//we don't want the calculation of the plugin to fail
				catch(final InvalidValueException e) {
					logger.error(
						"Dataset [{}] - plugin [{}]: value [{}] is not acceptable [{}]",
						dataset.getDatasetModelId(),
						fieldModel.getId(),
						e.getValue(),
						e.getError().getLocalizedMessage(studyService.getStudy().getDefaultLanguageId())
					);
				}
				catch(final BadlyFormattedValue e) {
					logger.error(
						"Dataset [{}] - plugin [{}]: value [{}] is badly formatted, expected [{}]",
						dataset.getDatasetModelId(),
						fieldModel.getId(),
						e.getDesiredValue(),
						e.getSanitizedValue()
					);
				}
				catch(final Exception e) {
					logger.error(
						"Dataset [{}] - plugin [{}]: unable to calculate [{}]",
						dataset.getDatasetModelId(),
						fieldModel.getId(),
						e.getLocalizedMessage()
					);
				}
			}
		}
	}

	@Override
	public Dataset get(final Field field) {
		return datasetDAOService.getDatasetByPk(field.getDatasetFk());
	}

	@Override
	public List<Dataset> search(final Scope scope, final Optional<Event> event, final Optional<Collection<DatasetModel>> datasetModels, final ACL acl) {
		//TODO make this legible and do not fetch deleted datasets if they are filtered out afterwards
		final List<Dataset> datasets;
		if(datasetModels.isPresent()) {
			datasets = event.isPresent() ? getAllIncludingRemoved(event.get(), datasetModels.get()) : getAllIncludingRemoved(scope, datasetModels.get());
		}
		else {
			datasets = event.isPresent() ? getAllIncludingRemoved(event.get()) : getAllIncludingRemoved(scope);
		}

		final var includeDeleted = acl.hasRight(FeatureStatic.MANAGE_DELETED_DATA);
		return datasets
			.stream()
			.filter(d -> !d.getDeleted() || includeDeleted)
			.filter(
				d -> event.isPresent() && acl.hasRight(event.get().getDateOrExpectedDate(), d.getDatasetModel(), Rights.READ) || acl.hasRight(d.getCreationTime(), d.getDatasetModel(), Rights.READ)
			)
			.toList();
	}

	@Override
	public List<Dataset> getAllIncludingRemoved(final Collection<DatasetModel> datasetModels) {
		final var datasetModelIds = datasetModels.stream().map(DatasetModel::getId).collect(Collectors.toList());
		return datasetDAOService.getAllDatasetsByDatasetModelIds(datasetModelIds);
	}

	//scope
	@Override
	public List<Dataset> getAllIncludingRemoved(final Scope scope) {
		return datasetDAOService.getAllDatasetsByScopePk(scope.getPk());
	}

	@Override
	public List<Dataset> getAllIncludingRemoved(final Scope scope, final Collection<DatasetModel> datasetModels) {
		final var datasetModelIds = datasetModels.stream().map(DatasetModel::getId).collect(Collectors.toList());
		return datasetDAOService.getAllDatasetsByScopePkAndDatasetModelIds(scope.getPk(), datasetModelIds);
	}

	@Override
	public List<Dataset> getAll(final Scope scope) {
		return datasetDAOService.getDatasetsByScopePk(scope.getPk());
	}

	@Override
	public List<Dataset> getAll(final Scope scope, final Collection<DatasetModel> datasetModels) {
		final var datasetModelIds = datasetModels.stream().map(DatasetModel::getId).collect(Collectors.toList());
		return datasetDAOService.getDatasetsByScopePkAndDatasetModelIds(scope.getPk(), datasetModelIds);
	}

	@Override
	public Dataset get(final Scope scope, final DatasetModel datasetModel) {
		if(datasetModel.isMultiple()) {
			throw new InvalidParameterException("Unable to retrieve unique dataset if dataset model is a multiple");
		}
		return datasetDAOService.getDatasetsByScopePkAndDatasetModelIds(scope.getPk(), Collections.singletonList(datasetModel.getId())).get(0);
	}

	//event
	@Override
	public List<Dataset> getAllIncludingRemoved(final Event event) {
		return datasetDAOService.getAllDatasetsByEventPk(event.getPk());
	}

	@Override
	public List<Dataset> getAllIncludingRemoved(final Event event, final Collection<DatasetModel> datasetModels) {
		final var datasetModelIds = datasetModels.stream().map(DatasetModel::getId).collect(Collectors.toList());
		return datasetDAOService.getAllDatasetsByEventPkAndDatasetModelIds(event.getPk(), datasetModelIds);
	}

	@Override
	public List<Dataset> getAll(final Event event) {
		return datasetDAOService.getDatasetsByEventPk(event.getPk());
	}

	@Override
	public List<Dataset> getAll(final Event event, final Collection<DatasetModel> datasetModels) {
		final var datasetModelIds = datasetModels.stream().map(DatasetModel::getId).collect(Collectors.toList());
		return datasetDAOService.getDatasetsByEventPkAndDatasetModelIds(event.getPk(), datasetModelIds);
	}

	@Override
	public Dataset get(final Event event, final DatasetModel datasetModel) {
		if(datasetModel.isMultiple()) {
			throw new InvalidParameterException("Unable to retrieve unique dataset if dataset model is a multiple");
		}
		return datasetDAOService.getDatasetsByEventPkAndDatasetModelIds(event.getPk(), Collections.singletonList(datasetModel.getId())).get(0);
	}

}
