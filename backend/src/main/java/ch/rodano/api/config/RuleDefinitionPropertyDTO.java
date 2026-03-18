package ch.rodano.api.config;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RuleDefinitionPropertyDTO {

	@NotNull
	private UUID ruleDefinitionPropertyId;
	@NotBlank
	private String id;

	@NotNull
	private String label;
	@NotNull
	private String entity;
	@NotNull
	private String target;
	@NotNull
	private String type;
	@NotNull
	private String configurationEntity;

	private String options;

	public UUID getRuleDefinitionPropertyId() {
		return ruleDefinitionPropertyId;
	}

	public void setRuleDefinitionPropertyId(final UUID ruleDefinitionPropertyId) {
		this.ruleDefinitionPropertyId = ruleDefinitionPropertyId;
	}

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

	public String getEntity() {
		return entity;
	}

	public void setEntity(final String entity) {
		this.entity = entity;
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
}
