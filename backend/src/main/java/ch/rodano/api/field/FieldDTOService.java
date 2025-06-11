package ch.rodano.api.field;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.utils.ACL;

public interface FieldDTOService {

	/**
	 * Create a list of DTOs from a collection of fields
	 */
	List<FieldDTO> createDTOs(Scope scope, Optional<Event> event, Dataset dataset, Collection<Field> fields, ACL acl);

	/**
	 * Create a DTO from a field
	 *
	 */
	FieldDTO createDTO(Scope scope, Optional<Event> event, Dataset dataset, Field field, ACL acl);

}
