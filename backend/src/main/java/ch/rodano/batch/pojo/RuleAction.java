package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleAction {

	private String id;
	private Map<String, String> label;
	private boolean optional;
	private String rulableEntity;
	private String actionId;
	private String staticActionId;
	private String conditionId;
	private List<RuleActionParameter> parameters;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getLabel() {
		return label;
	}

	public void setLabel(final Map<String, String> label) {
		this.label = label;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(final boolean optional) {
		this.optional = optional;
	}

	public String getRulableEntity() {
		return rulableEntity;
	}

	public void setRulableEntity(final String rulableEntity) {
		this.rulableEntity = rulableEntity;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(final String actionId) {
		this.actionId = actionId;
	}

	public String getStaticActionId() {
		return staticActionId;
	}

	public void setStaticActionId(final String staticActionId) {
		this.staticActionId = staticActionId;
	}

	public String getConditionId() {
		return conditionId;
	}

	public void setConditionId(final String conditionId) {
		this.conditionId = conditionId;
	}

	public List<RuleActionParameter> getParameters() {
		return parameters;
	}

	public void setParameters(final List<RuleActionParameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "RuleAction{" +
			"id='" + id + '\'' +
			", label=" + label +
			", optional=" + optional +
			", rulableEntity='" + rulableEntity + '\'' +
			", actionId='" + actionId + '\'' +
			", staticActionId='" + staticActionId + '\'' +
			", conditionId='" + conditionId + '\'' +
			", parameters=" + parameters +
			'}';
	}
}

