package ch.rodano.core.services.plugin.export;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.plugins.export.ExportPlugin;
import ch.rodano.core.services.bll.study.StudyService;

@Service
public class ExportPluginServiceImpl implements ExportPluginService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Optional<ExportPlugin> exportPlugin;

	private final Map<String, Method> methods;

	/**
	 * Here is injected the export plugin
	 * If no bean is found, the export plugin is not set
	 */
	public ExportPluginServiceImpl(final StudyService studyService, final Optional<ExportPlugin> exportPlugin) {
		this.exportPlugin = exportPlugin;
		methods = new TreeMap<>();

		final var study = studyService.getStudy();

		// Retrieve list of plugin field models
		final var fieldModels = study.getFieldModels().stream()
			.filter(FieldModel::isPlugin)
			.filter(f -> !f.hasValueFormula())
			.toList();

		if(!fieldModels.isEmpty()) {
			if(this.exportPlugin.isPresent()) {
				fieldModels.forEach(fieldModel -> {
					try {
						final var method = this.exportPlugin.get().getClass().getMethod(fieldModel.getId(), Scope.class, Optional.class, Dataset.class, Field.class);
						methods.put(fieldModel.getId(), method);
					}
					catch(@SuppressWarnings("unused") final NoSuchMethodException e) {
						logger.error("Export plugin's method {} not found", fieldModel.getId());
					}
				});

				logger.info("Export plugin loaded with the methods {}", methods.keySet());
			}
			else {
				logger.error("Export plugin is missing");
			}
		}
	}

	@Override
	public String calculate(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
		if(exportPlugin.isEmpty()) {
			logger.warn("No export plugin registered");
			return "";
		}

		final var fieldId = field.getId();
		if(!methods.containsKey(fieldId)) {
			logger.warn("Unable to calculate value for plugin {} because associated method is missing", fieldId);
			return "";
		}

		try {
			final String value = (String) methods.get(fieldId).invoke(exportPlugin.get(), scope, event, dataset, field);
			return value != null ? value : "";
		}
		catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error(e.getLocalizedMessage(), e);
			return "";
		}
	}
}
