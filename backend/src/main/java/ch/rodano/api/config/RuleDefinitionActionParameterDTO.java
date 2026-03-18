package ch.rodano.api.config;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RuleDefinitionActionParameterDTO {

	@NotBlank
	private String id;

	@NotNull
	private String label;

	private UUID ruleDefinitionActionId;

	private String dataEntity;
	private String configurationEntity;
	private String options;

	private int sortOrder;

	public UUID getRuleDefinitionActionId() {
		return ruleDefinitionActionId;
	}

	public void setRuleDefinitionActionId(final UUID ruleDefinitionActionId) {
		this.ruleDefinitionActionId = ruleDefinitionActionId;
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

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(final int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
