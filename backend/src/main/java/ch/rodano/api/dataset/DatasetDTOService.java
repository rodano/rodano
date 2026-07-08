package ch.rodano.api.dataset;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.utils.ACL;

public interface DatasetDTOService {

	/**
	 * Creates a DatasetDTO for the given dataset
	 * @param scope
	 * @param event
	 * @param dataset
	 * @param acl
	 * @return
	 */
	DatasetDTO createDTO(Scope scope, Optional<Event> event, Dataset dataset, ACL acl);

	/**
	 * Creates a list of DatasetDTOs for the given datasets and fields.
	 * Only provided fields will be included in the DTOs
	 * @param scope
	 * @param event
	 * @param datasets
	 * @param field
	 * @param acl
	 * @return
	 */
	List<DatasetDTO> createDTOs(Scope scope, Optional<Event> event, Collection<Dataset> datasets, Collection<Field> fields, ACL acl);

}
