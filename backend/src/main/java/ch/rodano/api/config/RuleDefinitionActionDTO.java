package ch.rodano.api.config;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RuleDefinitionActionDTO {

	@NotNull
	private UUID ruleDefinitionActionId;
	@NotBlank
	private String id;

	@NotNull
	private String label;

	@NotNull
	private String entity;

	@NotNull
	private List<RuleDefinitionActionParameterDTO> parameters;

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

	public String getEntity() {
		return entity;
	}

	public void setEntity(final String entity) {
		this.entity = entity;
	}

	public List<RuleDefinitionActionParameterDTO> getParameters() {
		return parameters;
	}

	public void setParameters(final List<RuleDefinitionActionParameterDTO> parameters) {
		this.parameters = parameters;
	}
}
