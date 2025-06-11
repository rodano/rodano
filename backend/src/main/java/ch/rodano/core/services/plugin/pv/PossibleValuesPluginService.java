package ch.rodano.core.services.plugin.pv;

import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.field.PossibleValue;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.plugin.PluginService;

public interface PossibleValuesPluginService extends PluginService {
	/**
	 * Provide a list of possible values for a field
	 *
	 * @param scope The scope
	 * @param event The optional event
	 * @param dataset The dataset
	 * @param field The field
	 * @return A list of possible values returned by the provider associated to the field model
	 */
	List<PossibleValue> provide(Scope scope, Optional<Event> event, Dataset dataset, Field field);
}
