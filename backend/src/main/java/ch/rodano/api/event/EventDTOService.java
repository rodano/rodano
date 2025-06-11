package ch.rodano.api.event;

import java.util.Collection;
import java.util.List;

import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.utils.ACL;

public interface EventDTOService {

	List<EventDTO> createDTOs(Scope scope, Collection<Event> events, ACL acl);

	EventDTO createDTO(Scope scope, Event event, ACL acl);
}
