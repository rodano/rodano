package ch.rodano.core.services.bll.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;

import static ch.rodano.core.model.jooq.tables.Dataset.DATASET;
import static ch.rodano.core.model.jooq.tables.Event.EVENT;
import static ch.rodano.core.model.jooq.tables.Field.FIELD;
import static ch.rodano.core.model.jooq.tables.Scope.SCOPE;

@Service
public class DatabaseConfigurationConsistencyServiceImpl implements DatabaseConfigurationConsistencyService {

	private final StudyService studyService;
	private final DSLContext create;
	private final ScopeDAOService scopeDAOService;
	private final EventDAOService eventDAOService;
	private final DatasetDAOService datasetDAOService;
	private final ScopeService scopeService;
	private final EventService eventService;
	private final DatasetService datasetService;
	private final FieldService fieldService;

	public DatabaseConfigurationConsistencyServiceImpl(
		final StudyService studyService,
		final DSLContext create,
		final ScopeDAOService scopeDAOService,
		final EventDAOService eventDAOService,
		final DatasetDAOService datasetDAOService,
		final ScopeService scopeService,
		final EventService eventService,
		final DatasetService datasetService,
		final FieldService fieldService
	) {
		this.studyService = studyService;
		this.create = create;
		this.scopeDAOService = scopeDAOService;
		this.eventDAOService = eventDAOService;
		this.datasetDAOService = datasetDAOService;
		this.scopeService = scopeService;
		this.eventService = eventService;
		this.datasetService = datasetService;
		this.fieldService = fieldService;
	}

	@Override
	public List<ConfigurationInconsistencyGroup> fixInconsistencies(
		final boolean dryRun,
		final DatabaseActionContext context,
		final String rationale
	) {
		final var issues = new ArrayList<ConfigurationInconsistency>();
		issues.addAll(checkDatasetConsistencyInScopes(dryRun, context, rationale));
		issues.addAll(checkDatasetConsistencyInEvents(dryRun, context, rationale));
		issues.addAll(checkFieldConsistency(dryRun, context, rationale));
		final var issuesGroups = new ArrayList<ConfigurationInconsistencyGroup>();
		for(final var issue : issues) {
			var group = issuesGroups.stream()
				.filter(g -> g.entity().equals(issue.entity()) && g.modelId().equals(issue.modelId()) && g.type().equals(issue.type()) && g.missingEntityId() == issue.missingEntityId() && g.status() == issue.status())
				.findFirst()
				.orElse(null);
			if(group == null) {
				group = new ConfigurationInconsistencyGroup(issue.entity(), issue.modelId(), new ArrayList<>(), issue.type(), issue.missingEntityId(), issue.status());
				issuesGroups.add(group);
			}
			group.pks().add(issue.pk());
		}
		return issuesGroups;
	}

	private List<ConfigurationInconsistency> checkDatasetConsistencyInScopes(
		final boolean dryRun,
		final DatabaseActionContext context,
		final String rationale
	) {
		final var issues = new ArrayList<ConfigurationInconsistency>();
		//retrieve multiple datasets to exclude them from the query
		final var multipleDatasetIds = studyService.getStudy().getDatasetModels().stream()
			.filter(DatasetModel::isMultiple)
			.map(DatasetModel::getId)
			.toList();

		//define the aggregation of dataset ids per entity, to be used in the SQL query
		final var datasetIdsField = DSL.multisetAgg(DATASET.DATASET_MODEL_ID)
			.orderBy(DATASET.DATASET_MODEL_ID)
			.convertFrom(r -> r.map(Record1::value1));

		final var scopeQuery = create
			.select(SCOPE.PK, SCOPE.SCOPE_MODEL_ID, datasetIdsField)
			.from(SCOPE)
			.innerJoin(DATASET).on(SCOPE.PK.eq(DATASET.SCOPE_FK))
			.where(DATASET.EVENT_FK.isNull())
			.and(DATASET.DATASET_MODEL_ID.notIn(multipleDatasetIds))
			.groupBy(SCOPE.PK);

		try(var cursor = scopeQuery.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var scopePk = r.get(SCOPE.PK);
				final var scopeModel = studyService.getStudy().getScopeModel(r.get(SCOPE.SCOPE_MODEL_ID));

				final var requiredDatasetIds = scopeModel.getDatasetModels().stream()
					.filter(d -> !d.isMultiple())
					.map(DatasetModel::getId)
					.sorted()
					.toList();
				final var datasetIds = r.get(datasetIdsField);

				//the two lists must be strictly equal:
				//there must be no dataset in the database that don't have their dataset model in the configuration
				//there must be no dataset model in the configuration that have not been instanced in the database
				//there must be no more that one instance of each single dataset model
				if(!datasetIds.equals(requiredDatasetIds)) {
					final var missingDatasetIds = new ArrayList<String>(requiredDatasetIds);
					missingDatasetIds.removeAll(datasetIds);

					final var extraDatasetIds = new ArrayList<String>(datasetIds);
					extraDatasetIds.removeAll(requiredDatasetIds);

					if(!missingDatasetIds.isEmpty()) {
						if(!dryRun) {
							final var scope = scopeDAOService.getScopeByPk(scopePk);
							for(final String datasetModelId : missingDatasetIds) {
								final var datasetModel = scopeModel.getDatasetModels().stream()
									.filter(d -> d.getId().equals(datasetModelId))
									.findFirst()
									.orElseThrow();
								datasetService.create(scope, datasetModel, context, rationale);
								issues.add(new ConfigurationInconsistency(InconsistentEntity.SCOPE, scopeModel.getId(), scopePk, ConfigurationInconsistencyType.MISSING_IN_DATABASE, datasetModelId, InconsistencyStatus.FIXED));
							}
						}
						else {
							for(final String datasetModelId : missingDatasetIds) {
								issues.add(new ConfigurationInconsistency(InconsistentEntity.SCOPE, scopeModel.getId(), scopePk, ConfigurationInconsistencyType.MISSING_IN_DATABASE, datasetModelId, InconsistencyStatus.FIXABLE));
							}
						}
					}
					for(final String datasetModelId : extraDatasetIds) {
						issues.add(new ConfigurationInconsistency(InconsistentEntity.SCOPE, scopeModel.getId(), scopePk, ConfigurationInconsistencyType.MISSING_IN_CONFIGURATION, datasetModelId, InconsistencyStatus.NOT_FIXABLE));
					}
				}
			}
		}
		return issues;
	}

	private List<ConfigurationInconsistency> checkDatasetConsistencyInEvents(
		final boolean dryRun,
		final DatabaseActionContext context,
		final String rationale
	) {
		final var issues = new ArrayList<ConfigurationInconsistency>();
		final var multipleDatasetIds = studyService.getStudy().getDatasetModels().stream()
			.filter(DatasetModel::isMultiple)
			.map(DatasetModel::getId)
			.toList();

		final var datasetIdsField = DSL.multisetAgg(DATASET.DATASET_MODEL_ID)
			.orderBy(DATASET.DATASET_MODEL_ID)
			.convertFrom(r -> r.map(Record1::value1));

		final var eventQuery = create
			.select(EVENT.PK, EVENT.SCOPE_MODEL_ID, EVENT.EVENT_MODEL_ID, datasetIdsField)
			.from(EVENT)
			.innerJoin(DATASET).on(EVENT.PK.eq(DATASET.EVENT_FK))
			.where(DATASET.DATASET_MODEL_ID.notIn(multipleDatasetIds))
			.groupBy(EVENT.PK);

		try(var cursor = eventQuery.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var eventPk = r.get(EVENT.PK);
				final var scopeModel = studyService.getStudy().getScopeModel(r.get(EVENT.SCOPE_MODEL_ID));
				final var eventModel = scopeModel.getEventModel(r.get(EVENT.EVENT_MODEL_ID));

				final var requiredDatasetIds = eventModel.getDatasetModels().stream()
					.filter(d -> !d.isMultiple())
					.map(DatasetModel::getId)
					.sorted()
					.toList();
				final var datasetIds = r.get(datasetIdsField);

				if(!datasetIds.equals(requiredDatasetIds)) {
					final var missingDatasetIds = new ArrayList<String>(requiredDatasetIds);
					missingDatasetIds.removeAll(datasetIds);

					final var extraDatasetIds = new ArrayList<String>(datasetIds);
					extraDatasetIds.removeAll(requiredDatasetIds);

					//the two lists must be strictly equal
					if(!missingDatasetIds.isEmpty()) {
						if(!dryRun) {
							final var event = eventDAOService.getEventByPk(eventPk);
							final var scope = scopeService.get(event);
							for(final String datasetModelId : missingDatasetIds) {
								final var datasetModel = eventModel.getDatasetModels().stream()
									.filter(d -> d.getId().equals(datasetModelId))
									.findFirst()
									.orElseThrow();
								datasetService.create(scope, event, datasetModel, context, rationale);
								issues.add(new ConfigurationInconsistency(InconsistentEntity.EVENT, eventModel.getId(), eventPk, ConfigurationInconsistencyType.MISSING_IN_DATABASE, datasetModelId, InconsistencyStatus.FIXED));
							}
						}
						else {
							for(final String datasetModelId : missingDatasetIds) {
								issues.add(new ConfigurationInconsistency(InconsistentEntity.EVENT, eventModel.getId(), eventPk, ConfigurationInconsistencyType.MISSING_IN_DATABASE, datasetModelId, InconsistencyStatus.FIXABLE));
							}
						}
					}
					for(final String datasetModelId : extraDatasetIds) {
						issues.add(new ConfigurationInconsistency(InconsistentEntity.EVENT, eventModel.getId(), eventPk, ConfigurationInconsistencyType.MISSING_IN_CONFIGURATION, datasetModelId, InconsistencyStatus.NOT_FIXABLE));
					}
				}
			}
		}
		return issues;
	}

	private List<ConfigurationInconsistency> checkFieldConsistency(
		final boolean dryRun,
		final DatabaseActionContext context,
		final String rationale
	) {
		final var issues = new ArrayList<ConfigurationInconsistency>();
		final Map<String, DatasetModel> datasetModelsById = studyService.getStudy().getDatasetModels().stream()
			.collect(Collectors.toMap(DatasetModel::getId, d -> d));

		//define the aggregation of field ids per entity, to be used in the SQL query
		final var fieldIdsField = DSL.multisetAgg(FIELD.FIELD_MODEL_ID)
			.orderBy(FIELD.FIELD_MODEL_ID)
			.convertFrom(r -> r.map(Record1::value1));

		final var query = create
			.select(DATASET.PK, DATASET.DATASET_MODEL_ID, fieldIdsField)
			.from(FIELD)
			.innerJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.groupBy(DATASET.PK);

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var datasetPk = r.get(DATASET.PK);
				final var datasetModelId = r.get(DATASET.DATASET_MODEL_ID);
				final var datasetModel = datasetModelsById.get(datasetModelId);

				//if the dataset model is not found in the configuration, there is nothing we can do for the fields
				//the issue of the missing dataset model will be already reported by the checkDatasetConsistencyInScopes or checkDatasetConsistencyInEvents method
				if(datasetModel != null) {
					final var requiredFieldIds = datasetModel.getFieldModels().stream()
						.map(FieldModel::getId)
						.sorted()
						.toList();
					final var fieldIds = r.get(fieldIdsField);

					//the two lists must be strictly equal:
					//there must be no field in the database that don't have their field model in the configuration
					//there must be no field model in the configuration that have not been instanced in the database
					//there must be no more that one instance of each single field model
					if(!fieldIds.equals(requiredFieldIds)) {
						final var missingFieldIds = new ArrayList<String>(requiredFieldIds);
						missingFieldIds.removeAll(fieldIds);

						final var extraFieldIds = new ArrayList<String>(fieldIds);
						extraFieldIds.removeAll(requiredFieldIds);

						if(!missingFieldIds.isEmpty()) {
							if(!dryRun) {
								final var dataset = datasetDAOService.getDatasetByPk(datasetPk);
								final var scope = scopeService.get(dataset);
								final var event = eventService.get(dataset);
								for(final String fieldModelId : missingFieldIds) {
									final var fieldModel = datasetModel.getFieldModel(fieldModelId);
									fieldService.create(scope, event, dataset, fieldModel, context, rationale);
									issues.add(new ConfigurationInconsistency(InconsistentEntity.DATASET, datasetModelId, datasetPk, ConfigurationInconsistencyType.MISSING_IN_DATABASE, fieldModelId, InconsistencyStatus.FIXED));
								}
							}
							else {
								for(final String fieldModelId : missingFieldIds) {
									issues.add(new ConfigurationInconsistency(InconsistentEntity.DATASET, datasetModelId, datasetPk, ConfigurationInconsistencyType.MISSING_IN_DATABASE, fieldModelId, InconsistencyStatus.FIXABLE));
								}
							}
						}
						for(final String fieldModelId : extraFieldIds) {
							issues.add(new ConfigurationInconsistency(InconsistentEntity.DATASET, datasetModelId, datasetPk, ConfigurationInconsistencyType.MISSING_IN_CONFIGURATION, fieldModelId, InconsistencyStatus.NOT_FIXABLE));
						}
					}
				}
			}
		}
		return issues;
	}
}
