package ch.rodano.core.services.bll.database;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.core.services.bll.study.StudyService;

import static ch.rodano.core.model.jooq.tables.Dataset.DATASET;
import static ch.rodano.core.model.jooq.tables.Event.EVENT;
import static ch.rodano.core.model.jooq.tables.Field.FIELD;
import static ch.rodano.core.model.jooq.tables.Scope.SCOPE;

@Service
public class DBConsistencyServiceImpl implements DBConsistencyService {

	private final StudyService studyService;
	private final DSLContext create;

	public DBConsistencyServiceImpl(
		final StudyService studyService,
		final DSLContext create
	) {
		this.studyService = studyService;
		this.create = create;
	}

	@Override
	public List<String> checkConsistency() {
		final var issues = new ArrayList<String>();

		issues.addAll(checkDatasetConsistencyInScopes());
		issues.addAll(checkDatasetConsistencyInEvents());
		issues.addAll(checkFieldConsistency());

		return issues;
	}

	private List<String> checkDatasetConsistencyInScopes() {
		final var issues = new ArrayList<String>();
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
			.where(DATASET.DATASET_MODEL_ID.notIn(multipleDatasetIds))
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

					final var errors = new ArrayList<String>();
					if(!missingDatasetIds.isEmpty()) {
						errors.add(String.format("[%s] are missing", String.join(",", missingDatasetIds)));
					}
					if(!extraDatasetIds.isEmpty()) {
						errors.add(String.format("[%s] have no matching dataset model", String.join(",", extraDatasetIds)));
					}

					issues.add(
						String.format(
							"Scope %s (pk %d) datasets are incorrect: %s",
							scopeModel.getId(),
							scopePk,
							String.join(" ; ", errors)
						)
					);
				}
			}
		}
		return issues;
	}

	private List<String> checkDatasetConsistencyInEvents() {
		final var issues = new ArrayList<String>();
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

					final var errors = new ArrayList<String>();
					if(!missingDatasetIds.isEmpty()) {
						errors.add(String.format("[%s] are missing", String.join(",", missingDatasetIds)));
					}
					if(!extraDatasetIds.isEmpty()) {
						errors.add(String.format("[%s] have no matching dataset model", String.join(",", extraDatasetIds)));
					}

					issues.add(
						String.format(
							"Event %s (pk %d) datasets are incorrect: %s",
							eventModel.getId(),
							eventPk,
							String.join(" ; ", errors)
						)
					);
				}
			}
		}
		return issues;
	}

	private List<String> checkFieldConsistency() {
		final var issues = new ArrayList<String>();
		final var datasetModelFieldModels = studyService.getStudy().getDatasetModels().stream()
			.collect(
				Collectors.toMap(
					DatasetModel::getId,
					d -> d.getFieldModels().stream().map(FieldModel::getId).sorted().toList()
				)
			);

		//define the aggregation of dataset ids per entity, to be used in the SQL query
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
				final var requiredFieldIds = datasetModelFieldModels.get(datasetModelId);

				//if the dataset model is not found in the configuration, it means that it exists only in the database and that's an error
				if(requiredFieldIds == null) {
					issues.add(String.format("Dataset pk %d: there is no dataset model with id %s in the configuration", datasetPk, datasetModelId));
				}
				else {
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

						final var errors = new ArrayList<String>();
						if(!missingFieldIds.isEmpty()) {
							errors.add(String.format("[%s] are missing", String.join(",", missingFieldIds)));
						}
						if(!extraFieldIds.isEmpty()) {
							errors.add(String.format("[%s] have no matching field model", String.join(",", extraFieldIds)));
						}

						issues.add(
							String.format(
								"Dataset %s (pk %d) fields are incorrect: %s",
								datasetModelId,
								datasetPk,
								String.join(" ; ", errors)
							)
						);
					}
				}
			}
		}
		return issues;
	}
}
