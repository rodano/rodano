package ch.rodano.api.dataset;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.utils.ACL;

public interface DatasetDTOService {

	DatasetDTO createDTO(Scope scope, Optional<Event> event, Dataset dataset, ACL acl);

	List<DatasetDTO> createDTOs(Scope scope, Optional<Event> event, Form form, Collection<Dataset> datasets, ACL acl);

}
