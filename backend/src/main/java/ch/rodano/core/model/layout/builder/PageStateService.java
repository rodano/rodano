package ch.rodano.core.model.layout.builder;

import java.time.ZonedDateTime;
import java.util.Optional;

import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;

public interface PageStateService {

	PageState createPageState(Scope scope, Optional<Event> event, Form form);

	void initPage(PageState pageState, Optional<ZonedDateTime> date, boolean clean);

}
