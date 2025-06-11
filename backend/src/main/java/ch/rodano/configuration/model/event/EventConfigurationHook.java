package ch.rodano.configuration.model.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.form.FormModel;

public class EventConfigurationHook implements Serializable {
	private static final long serialVersionUID = -3556215335256366005L;

	private static final Logger LOGGER = LoggerFactory.getLogger(EventConfigurationHook.class);

	private String eventModelId;
	private List<DatasetModel> datasetModels;
	private List<FormModel> formModels;

	public EventConfigurationHook() {
		datasetModels = new ArrayList<>();
		formModels = new ArrayList<>();
	}

	public final String getEventModelId() {
		return eventModelId;
	}

	public final void setEventModelId(final String eventId) {
		this.eventModelId = eventId;
	}

	public final List<DatasetModel> getDatasetModels() {
		return datasetModels;
	}

	public final void setDatasetModels(final List<DatasetModel> datasetModels) {
		this.datasetModels = datasetModels;
	}

	@JsonIgnore
	public DatasetModel getDatasetModel(final String datasetModelId) {
		return datasetModels.stream()
			.filter(d -> d.getId().equalsIgnoreCase(datasetModelId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(Entity.DATASET_MODEL, datasetModelId));
	}

	public final List<FormModel> getFormModels() {
		return formModels;
	}

	public final void setFormModels(final List<FormModel> formModels) {
		this.formModels = formModels;
	}

	public final String getEntity() {
		return "EVENT_CONFIGURATION_HOOK";
	}

	@JsonAnySetter
	public final void setAnySetter(final String key, final Object value) {
		if(!"entity".equals(key)) {
			final var message = new StringBuilder("EventConfigurationHook - Unknown property : ");
			message.append(key);
			message.append(" - ");
			message.append(value);
			LOGGER.error(message.toString());
		}
	}
}
