package ch.rodano.core.services.plugin.export;

import java.util.Optional;

import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.plugin.PluginService;

public interface ExportPluginService extends PluginService {
	/**
	 * Calculate a plugin for a field
	 *
	 * @param scope The scope
	 * @param event The optional event
	 * @param dataset The dataset
	 * @param field The field
	 * @return A string returned by the calculation of the plugin (or the empty string if a problem occurs)
	 */
	String calculate(Scope scope, Optional<Event> event, Dataset dataset, Field field);
}
