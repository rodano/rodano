package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleDefinitionActionParameter {

	private String id;
	private String label;
	private String dataEntity;
	private String configurationEntity;
	private String options;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public String getDataEntity() {
		return dataEntity;
	}

	public void setDataEntity(final String dataEntity) {
		this.dataEntity = dataEntity;
	}

	public String getConfigurationEntity() {
		return configurationEntity;
	}

	public void setConfigurationEntity(final String configurationEntity) {
		this.configurationEntity = configurationEntity;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(final String options) {
		this.options = options;
	}

	@Override
	public String toString() {
		return "RuleDefinitionActionParameter{" +
			"id='" + id + '\'' +
			", label='" + label + '\'' +
			", dataEntity='" + dataEntity + '\'' +
			", configurationEntity='" + configurationEntity + '\'' +
			", options='" + options + '\'' +
			'}';
	}
}
