package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleDefinitionProperty {

	private String id;
	private String label;
	private String entityId;
	private String target;
	private String type;
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

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(final String entityId) {
		this.entityId = entityId;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(final String target) {
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
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
		return "RuleDefinitionProperty{" +
			"id='" + id + '\'' +
			", label='" + label + '\'' +
			", entityId='" + entityId + '\'' +
			", target='" + target + '\'' +
			", type='" + type + '\'' +
			", configurationEntity='" + configurationEntity + '\'' +
			", options='" + options + '\'' +
			'}';
	}
}
