package ch.rodano.api.form;

import java.util.List;
import java.util.Optional;

import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.utils.ACL;

public interface FormDTOService {

	List<FormDTO> createDTOs(Scope scope, Optional<Event> event, List<Form> forms, ACL acl);

	FormDTO createDTO(Scope scope, Optional<Event> event, Form form, ACL acl);

	FormDTO createDTO(Scope scope, Form form, ACL acl);

	FormDTO createDTO(Scope scope, Event event, Form form, ACL acl);
}
