package ch.rodano.core.services.plugin.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.plugins.validators.ValidatorPlugin;
import ch.rodano.core.services.bll.study.StudyService;

@Service
public class ValidatorPluginServiceImpl implements ValidatorPluginService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Optional<ValidatorPlugin> validatorPlugin;

	private final Map<String, Method> methods;

	/**
	 * Here is injected the validator plugin
	 * If no bean is found, the validator plugin is not set
	 */
	public ValidatorPluginServiceImpl(final StudyService studyService, final Optional<ValidatorPlugin> validatorPlugin) {
		this.validatorPlugin = validatorPlugin;
		methods = new TreeMap<>();

		final var study = studyService.getStudy();

		// Retrieve list of scripted validators
		final var validators = study.getValidators().stream().filter(Validator::isScript)
			.toList();

		if(!validators.isEmpty()) {
			if(this.validatorPlugin.isPresent()) {
				validators.forEach(validator -> {
					try {
						final var method = this.validatorPlugin.get().getClass().getMethod(validator.getId(), Scope.class, Optional.class, Dataset.class, Field.class);
						methods.put(validator.getId(), method);
					}
					catch(@SuppressWarnings("unused") final NoSuchMethodException e) {
						logger.error("Validator plugin's method {} not found", validator.getId());
					}
				});

				logger.info("Validator plugin loaded with the methods {}", methods.keySet());
			}
			else {
				logger.error("Validator plugin is missing");
			}
		}
	}

	/**
	 * Validate a value
	 *
	 * @param validator The validator to use
	 * @param field     The value to validate
	 * @return True of the value is valid and false otherwise
	 * @throws IllegalAccessException    Thrown if an error occurred while calling a method of the validator
	 * @throws IllegalArgumentException  Thrown if an argument is invalid
	 * @throws InvocationTargetException Thrown if an error occurred while calling a method of the validator
	 */
	@Override
	public Boolean validate(final Validator validator, final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
		if(validatorPlugin.isEmpty()) {
			logger.warn("No validator plugin registered");
			return true;
		}

		final var validatorId = validator.getId();
		if(!methods.containsKey(validatorId)) {
			logger.warn("Unable to validate value {} against validator {} because associated method is missing", field.getId(), validatorId);
			return true;
		}

		try {
			return (Boolean) methods.get(validatorId).invoke(validatorPlugin.get(), scope, event, dataset, field);
		}
		catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error(e.getLocalizedMessage(), e);
			return true;
		}
	}
}
