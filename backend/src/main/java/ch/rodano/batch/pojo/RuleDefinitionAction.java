package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleDefinitionAction {

	private String id;
	private String label;
	private String entityId;

	private List<RuleDefinitionActionParameter> parameters;

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

	public List<RuleDefinitionActionParameter> getParameters() {
		return parameters;
	}

	public void setParameters(final List<RuleDefinitionActionParameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "RuleDefinitionAction{" +
			"id='" + id + '\'' +
			", label='" + label + '\'' +
			", entityId='" + entityId + '\'' +
			", parameters=" + parameters +
			'}';
	}
}
