package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Workflow {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private Map<String, String> message;

	private String aggregateWorkflowId;
	private String initialStateId;
	private Integer orderBy;
	private boolean mandatory;
	private boolean unique;
	private String icon;

	private List<WorkflowAction> actions;
	private List<WorkflowState> states;

	private List<Rule> rules;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public Map<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final Map<String, String> longname) {
		this.longname = longname;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public Map<String, String> getMessage() {
		return message;
	}

	public void setMessage(final Map<String, String> message) {
		this.message = message;
	}

	public String getAggregateWorkflowId() {
		return aggregateWorkflowId;
	}

	public void setAggregateWorkflowId(final String aggregateWorkflowId) {
		this.aggregateWorkflowId = aggregateWorkflowId;
	}

	public String getInitialStateId() {
		return initialStateId;
	}

	public void setInitialStateId(final String initialStateId) {
		this.initialStateId = initialStateId;
	}

	public Integer getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(final Integer orderBy) {
		this.orderBy = orderBy;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(final boolean unique) {
		this.unique = unique;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	public List<WorkflowAction> getActions() {
		return actions;
	}

	public void setActions(final List<WorkflowAction> actions) {
		this.actions = actions;
	}

	public List<WorkflowState> getStates() {
		return states;
	}

	public void setStates(final List<WorkflowState> states) {
		this.states = states;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public String toString() {
		return "Workflow{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", message=" + message +
			", aggregateWorkflowId='" + aggregateWorkflowId + '\'' +
			", initialStateId='" + initialStateId + '\'' +
			", orderBy=" + orderBy +
			", mandatory=" + mandatory +
			", unique=" + unique +
			", icon='" + icon + '\'' +
			", actions=" + actions +
			", states=" + states +
			", rules=" + rules +
			'}';
	}
}
