package ch.rodano.core.services.plugin.pv;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.PossibleValue;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.plugins.pv.PossibleValuesPlugin;
import ch.rodano.core.services.bll.study.StudyService;

@Service
public class PossibleValuesPluginServiceImpl implements PossibleValuesPluginService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Optional<PossibleValuesPlugin> possibleValuePlugin;

	private final Map<String, Method> methods;

	/**
	 * Here is injected the possible values plugin
	 * If no bean is found, the possible values plugin is not set
	 */
	public PossibleValuesPluginServiceImpl(final StudyService studyService, final Optional<PossibleValuesPlugin> possibleValuePlugin) {
		this.possibleValuePlugin = possibleValuePlugin;
		methods = new TreeMap<>();

		final var study = studyService.getStudy();

		// Retrieve list of possible values providers
		final var possibleValuesProviders = study.getFieldModels().stream()
			.filter(FieldModel::hasPossibleValuesProvider)
			.map(FieldModel::getPossibleValuesProvider)
			.distinct()
			.toList();

		if(!possibleValuesProviders.isEmpty()) {
			if(this.possibleValuePlugin.isPresent()) {
				possibleValuesProviders.forEach(provider -> {
					try {
						final var method = this.possibleValuePlugin.get().getClass().getMethod(provider, Scope.class, Optional.class, Dataset.class, Field.class);
						methods.put(provider, method);
					}
					catch(@SuppressWarnings("unused") final NoSuchMethodException e) {
						logger.error("Possible values plugin's method {} not found", provider);
					}
				});

				logger.info("Possible values plugin loaded with the methods {}", methods.keySet());
			}
			else {
				logger.error("Possible values plugin is missing");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PossibleValue> provide(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
		if(possibleValuePlugin.isEmpty()) {
			logger.warn("No possible values plugin registered");
			return Collections.emptyList();
		}

		final var provider = field.getFieldModel().getPossibleValuesProvider();
		if(!methods.containsKey(provider)) {
			logger.warn("Unable to provide a list of possible values for field model {} because its possible values provider method is missing", field.getId(), provider);
			return Collections.emptyList();
		}

		try {
			final List<PossibleValue> possibleValues = (List<PossibleValue>) methods.get(provider).invoke(possibleValuePlugin.get(), scope, event, dataset, field);
			return possibleValues != null ? possibleValues : Collections.emptyList();
		}
		catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error(e.getLocalizedMessage(), e);
			return Collections.emptyList();
		}
	}
}
