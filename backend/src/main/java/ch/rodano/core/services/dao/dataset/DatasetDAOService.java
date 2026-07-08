package ch.rodano.core.services.dao.dataset;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.function.Function;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.DatasetAuditTrail;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Timeframe;

public interface DatasetDAOService {

	/**
	 * Get the dataset associated with a given primary key
	 * @param pk
	 * @return The dataset associated with the primary key
	 */
	Dataset getDatasetByPk(Long pk);

	/**
	 * Get the datasets associated with given primary keys
	 * @param pks
	 * @return The datasets associated with the given primary keys
	 */
	List<Dataset> getDatasetsByPks(Collection<Long> pks);

	/**
	 * Search for datasets matching the provided criteria
	 * @param scopePk The primary key of the scope
	 * @param eventPk The primary key of the event. When empty, look for datasets that are directly attached to the scope.
	 * @param includeDeleted Whether to include deleted datasets
	 * @param datasetModelIds The dataset model ids to filter by
	 * @return The list of datasets matching the criteria
	 */
	List<Dataset> search(Long scopePk, Optional<Long> eventPk, boolean includeDeleted, Optional<Collection<String>> datasetModelIds);

	/**
	 * Get all the datasets (including deleted) matching the provided dataset model ids
	 *
	 * @param datasetModelIds The dataset model ids
	 * @return The datasets matching the dataset model ids
	 */
	List<Dataset> getAllDatasetsByDatasetModelIds(Collection<String> datasetModelIds);

	/**
	 * Get the datasets associated with a given scope pk
	 *
	 * @param scopePk The scope pk
	 * @return The datasets associated with the scope pk
	 */
	List<Dataset> getDatasetsByScopePk(Long scopePk);

	/**
	 * Get the datasets associated with a given scope pk and dataset model ids
	 *
	 * @param scopePk The scope pk
	 * @param datasetModelIds The dataset model ids
	 * @return The datasets associated with the scope pk and dataset model ids
	 */
	List<Dataset> getDatasetsByScopePkAndDatasetModelIds(Long scopePk, Collection<String> datasetModelIds);

	/**
	 * Get all the datasets (including deleted) associated with the given scope pk
	 *
	 * @param scopePk The scope pk
	 * @return The datasets associated with the scope
	 */
	List<Dataset> getAllDatasetsByScopePk(Long scopePk);

	/**
	 * Get all the datasets (including deleted) associated with the given scope pk and dataset model ids
	 * @param scopePk The scope pk
	 * @param datasetModelIds The dataset model ids
	 * @return The datasets associated with the scope pk and dataset model ids
	 */
	List<Dataset> getAllDatasetsByScopePkAndDatasetModelIds(Long scopePk, Collection<String> datasetModelIds);

	/**
	 * Get the datasets associated with a given event pk
	 *
	 * @param eventPk The event pk
	 * @return The datasets associated with the event pk
	 */
	List<Dataset> getDatasetsByEventPk(Long eventPk);

	/**
	 * Get the datasets associated with a given event pk and dataset model ids
	 *
	 * @param eventPk The event pk
	 * @param datasetModelIds The dataset model ids
	 * @return The datasets associated with the event pk and dataset model ids
	 */
	List<Dataset> getDatasetsByEventPkAndDatasetModelIds(Long eventPk, Collection<String> datasetModelIds);

	/**
	 * Get all the datasets (including deleted) associated with the given event pk
	 *
	 * @param eventPk The event pk
	 * @return The datasets associated with the event pk
	 */
	List<Dataset> getAllDatasetsByEventPk(Long eventPk);

	/**
	 * Get all the datasets (including deleted) associated with the given event pk and dataset model ids
	 * @param eventPk The pk of the event
	 * @param datasetModelIds The dataset model ids
	 * @return The datasets associated with the event pk and dataset model ids
	 */
	List<Dataset> getAllDatasetsByEventPkAndDatasetModelIds(Long eventPk, Collection<String> datasetModelIds);

	/**
	 * Sets the deleted flag to true on a dataset in the database
	 *
	 * @param dataset The dataset to delete
	 * @param context The context of the action
	 * @param rationale The rationale for the operation
	 */
	void deleteDataset(Dataset dataset, DatabaseActionContext context, String rationale);

	/**
	 * Sets the deleted flag to false on a dataset in the database
	 *
	 * @param dataset The dataset to restore
	 * @param context The context of the action
	 * @param rationale The rationale for the operation
	 */
	void restoreDataset(Dataset dataset, DatabaseActionContext context, String rationale);

	/**
	 * Save a dataset in the database
	 *
	 * @param dataset The dataset to create/update
	 * @param context The context of the action
	 * @param rationale The rationale for the operation
	 */
	void saveDataset(Dataset dataset, DatabaseActionContext context, String rationale);

	NavigableSet<DatasetAuditTrail> getAuditTrails(Dataset dataset, Optional<Timeframe> timeframe, Optional<Long> actorPk);

	NavigableSet<DatasetAuditTrail> getAuditTrailsForProperty(Dataset dataset, Optional<Timeframe> timeframe, Function<DatasetAuditTrail, Object> property);

	NavigableSet<DatasetAuditTrail> getAuditTrailsForProperties(Dataset dataset, Optional<Timeframe> timeframe, List<Function<DatasetAuditTrail, Object>> properties);

}
