package ch.rodano.core.services.bll.dataset;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.utils.ACL;

public interface DatasetService {

	/**
	 * Create a candidate dataset, which serves as a pre-filled shell for a new dataset.
	 * @param scope         Dataset scope
	 * @param event         Dataset event
	 * @param datasetModel  Dataset model
	 * @param actor         Actor of the action
	 * @return              The dataset candidate
	 */
	Dataset createCandidate(
		Scope scope,
		Optional<Event> event,
		DatasetModel datasetModel,
		Actor actor
	);

	/**
	 * Create all datasets on a scope.
	 * @param scope     The scope
	 * @param context   Action context
	 * @param rationale The rationale for the operation
	 * @return          Created datasets
	 */
	List<Dataset> createAll(
		Scope scope,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Create all datasets on event.
	 * @param scope     The scope
	 * @param event     The event
	 * @param context   Action context
	 * @param rationale The rationale for the operation
	 * @return          Created datasets
	 */
	List<Dataset> createAll(
		Scope scope,
		Event event,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Create a dataset on scope.
	 * @param scope         Scope
	 * @param datasetModel  Dataset model
	 * @param context       Action context
	 * @param rationale     The rationale for the operation
	 * @param id            Dataset ID
	 * @return              The new dataset
	 */
	Dataset create(
		Scope scope,
		DatasetModel datasetModel,
		DatabaseActionContext context,
		String rationale,
		Optional<String> id
	);

	/**
	 * Create a dataset on scope.
	 * @param scope         Scope
	 * @param datasetModel  Dataset model
	 * @param context       Action context
	 * @param rationale     The rationale for the operation
	 * @return              The new dataset
	 */
	Dataset create(
		Scope scope,
		DatasetModel datasetModel,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Create a dataset on event.
	 * @param scope         Scope
	 * @param event         Event
	 * @param datasetModel  Dataset model
	 * @param context       Action context
	 * @param rationale     The rationale for the operation
	 * @return              The new dataset
	 */
	Dataset create(
		Scope scope,
		Event event,
		DatasetModel datasetModel,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Create a dataset on event.
	 * @param scope         Scope
	 * @param event         Event
	 * @param datasetModel  Dataset model
	 * @param context       Action context
	 * @param rationale     The rationale for the operation
	 * @param id            Dataset ID
	 * @return              The new dataset
	 */
	Dataset create(
		Scope scope,
		Event event,
		DatasetModel datasetModel,
		DatabaseActionContext context,
		String rationale,
		Optional<String> id
	);

	void save(
		Dataset dataset,
		DatabaseActionContext context,
		String rationale
	);

	void delete(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		DatabaseActionContext context,
		String rationale
	);

	void restore(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		DatabaseActionContext context,
		String rationale
	);

	void reset(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		DatabaseActionContext context,
		String rationale
	);

	void recalculatePluginValues(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		DatabaseActionContext context,
		String rationale
	);

	Dataset get(Field field);

	/**
	 * Search for datasets matching the criteria and the provided ACL
	 * @param scope The scope
	 * @param event The optional event
	 * @param datasetModels An optional filter on dataset models
	 * @param acl The ACL for the scope
	 * @return
	 */
	List<Dataset> search(Scope scope, Optional<Event> event, Optional<Collection<DatasetModel>> datasetModels, ACL acl);

	List<Dataset> getAllIncludingRemoved(Collection<DatasetModel> datasetModels);

	List<Dataset> getAllIncludingRemoved(Scope scope);

	List<Dataset> getAllIncludingRemoved(Scope scope, Collection<DatasetModel> datasetModels);

	List<Dataset> getAll(Scope scope);

	List<Dataset> getAll(Scope scope, Collection<DatasetModel> datasetModels);

	Dataset get(Scope scope, DatasetModel datasetModel);

	List<Dataset> getAllIncludingRemoved(Event event);

	List<Dataset> getAllIncludingRemoved(Event event, Collection<DatasetModel> datasetModels);

	List<Dataset> getAll(Event event);

	List<Dataset> getAll(Event event, Collection<DatasetModel> datasetModels);

	Dataset get(Event event, DatasetModel datasetModel);

	/**
	 * Validate all fields in the dataset.
	 * @param scope     Dataset scope
	 * @param event     Dataset event
	 * @param dataset   The dataset
	 * @param context   Action context
	 * @param rationale The rationale for the operation
	 */
	void validateFieldsOnDataset(
		Scope scope,
		Optional<Event> event,
		Dataset dataset,
		DatabaseActionContext context,
		String rationale
	);
}
