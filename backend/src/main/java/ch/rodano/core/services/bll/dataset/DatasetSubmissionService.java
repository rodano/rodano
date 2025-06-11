package ch.rodano.core.services.bll.dataset;

import java.util.Collection;
import java.util.Optional;

import ch.rodano.api.controller.form.exception.DatasetSubmissionException;
import ch.rodano.api.dataset.DatasetCreationDTO;
import ch.rodano.api.dataset.DatasetSubmissionDTO;
import ch.rodano.api.dataset.DatasetUpdateDTO;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.utils.ACL;

public interface DatasetSubmissionService {

	Collection<Dataset> saveDatasets(
		ACL acl,
		Scope scope,
		Optional<Event> event,
		Collection<DatasetUpdateDTO> datasets,
		DatabaseActionContext context,
		Optional<String> rationale
	) throws DatasetSubmissionException;

	/**
	 * Create and save a new multiple dataset
	 * @param acl
	 * @param scope
	 * @param event
	 * @param datasetSubmissionDTO
	 * @param context
	 * @param rationale The rationale for the operation
	 * @return
	 * @throws DatasetSubmissionException
	 */
	Collection<Dataset> createAndSaveDatasets(
		ACL acl,
		Scope scope,
		Optional<Event> event,
		Collection<DatasetCreationDTO> datasetCreationDTO,
		DatabaseActionContext context,
		Optional<String> rationale
	) throws DatasetSubmissionException;

	/**
	 * Submit a dataset submission DTO, containing datasets to create/update/restore/remove
	 * @param acl
	 * @param scope
	 * @param event
	 * @param datasetSubmissionDTO
	 * @param context
	 * @param rationale The rationale for the operation
	 * @return
	 * @throws DatasetSubmissionException
	 */
	Collection<Dataset> submit(ACL acl, Scope scope, Optional<Event> event, DatasetSubmissionDTO datasetSubmissionDTO, DatabaseActionContext context, Optional<String> rationale)
		throws DatasetSubmissionException;
}
