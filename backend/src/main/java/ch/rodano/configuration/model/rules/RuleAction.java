package ch.rodano.configuration.model.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.utils.DisplayableUtils;


public class RuleAction implements Node {
	private static final long serialVersionUID = 8950905524257402197L;

	//some actions are optional up to user in UI
	private String id;
	private SortedMap<String, String> label;
	private boolean optional;

	//trigger an action from configuration
	private String configurationWorkflowId;
	private String configurationActionId;

	//static action
	private String staticActionId;

	//entity action
	private RulableEntity rulableEntity;
	private String conditionId;
	private String actionId;

	//parameters, only for static and entity actions
	private List<RuleActionParameter> parameters;

	public RuleAction() {
		label = new TreeMap<>();
		parameters = new ArrayList<>();
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final SortedMap<String, String> getLabel() {
		return label;
	}

	public final void setLabel(final SortedMap<String, String> label) {
		this.label = label;
	}

	public final boolean isOptional() {
		return optional;
	}

	public final void setOptional(final boolean optional) {
		this.optional = optional;
	}

	public final String getConfigurationWorkflowId() {
		return configurationWorkflowId;
	}

	public final void setConfigurationWorkflowId(final String configurationWorkflowId) {
		this.configurationWorkflowId = configurationWorkflowId;
	}

	public final String getConfigurationActionId() {
		return configurationActionId;
	}

	public final void setConfigurationActionId(final String configurationActionId) {
		this.configurationActionId = configurationActionId;
	}

	public final String getStaticActionId() {
		return staticActionId;
	}

	public final void setStaticActionId(final String staticActionId) {
		this.staticActionId = staticActionId;
	}

	public final RulableEntity getRulableEntity() {
		return rulableEntity;
	}

	public final void setRulableEntity(final RulableEntity rulableEntity) {
		this.rulableEntity = rulableEntity;
	}

	public final String getConditionId() {
		return conditionId;
	}

	public final void setConditionId(final String conditionId) {
		this.conditionId = conditionId;
	}

	public final String getActionId() {
		return actionId;
	}

	public final void setActionId(final String actionId) {
		this.actionId = actionId;
	}

	public final List<RuleActionParameter> getParameters() {
		return parameters;
	}

	public final void setParameters(final List<RuleActionParameter> parameters) {
		this.parameters = parameters;
	}

	public final String getLocalizedLabel(final String... languages) {
		return DisplayableUtils.getLocalizedMap(label, languages);
	}

	@Override
	public final Entity getEntity() {
		return Entity.RULE_ACTION;
	}

	@Override
	public String toString() {
		final var string = new StringBuilder();
		if(configurationActionId != null) {
			string.append("Configured action ");
			string.append(configurationActionId);
			string.append(" - ");
		}
		else if(staticActionId != null) {
			string.append("Static action ");
			string.append(staticActionId);
			string.append(" - ");
		}
		else {
			string.append("Condition ");
			string.append(conditionId);
			string.append(" - Action ");
			string.append(actionId);
			string.append(" - ");
		}

		string.append(parameters.toString());
		return string.toString();
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case RULE_ACTION_PARAMETER:
				return Collections.unmodifiableList(parameters);
			default:
				return Collections.emptyList();
		}
	}
}
