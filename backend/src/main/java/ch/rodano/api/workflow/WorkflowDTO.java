package ch.rodano.api.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The workflow model")
public class WorkflowDTO {
	@NotNull
	public UUID workflowId;
	@Schema(description = "The unique ID of the workflow")
	@NotBlank
	public String id;

	@NotNull
	public SortedMap<String, String> shortname = new TreeMap<>();
	public SortedMap<String, String> longname = new TreeMap<>();
	public SortedMap<String, String> description = new TreeMap<>();

	public Integer order;

	@Schema(description = "Is the workflow an aggregate workflow?")
	@NotNull
	public boolean aggregator;

	@Schema(description = "Aggregated workflow Id")
	public UUID aggregatedWorkflowId;
	@Schema(description = "Initial state Id")
	public UUID initialStateId;

	@Schema(description = "Is the workflow mandatory?")
	@NotNull
	public boolean mandatory;
	@Schema(description = "Is the workflow unique?")
	@NotNull
	public boolean unique;

	public UUID actionId;

	@Schema(description = "Message attached to the workflow")
	@NotNull
	public SortedMap<String, String> message = new TreeMap<>();
	public String icon;

	@Schema(description = "Possible states of the workflow")
	@NotNull
	public List<WorkflowStateDTO> states = new ArrayList<>();
	@Schema(description = "Possible actions that can be performed on the workflow")
	@NotNull
	public List<WorkflowActionDTO> actions = new ArrayList<>();

	public UUID getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final UUID workflowId) {
		this.workflowId = workflowId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(final Integer order) {
		this.order = order;
	}

	public boolean isAggregator() {
		return aggregator;
	}

	public void setAggregator(final boolean aggregator) {
		this.aggregator = aggregator;
	}

	public UUID getAggregatedWorkflowId() {
		return aggregatedWorkflowId;
	}

	public void setAggregatedWorkflowId(final UUID aggregatedWorkflowId) {
		this.aggregatedWorkflowId = aggregatedWorkflowId;
	}

	public UUID getInitialStateId() {
		return initialStateId;
	}

	public void setInitialStateId(final UUID initialStateId) {
		this.initialStateId = initialStateId;
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

	public UUID getActionId() {
		return actionId;
	}

	public void setActionId(final UUID actionId) {
		this.actionId = actionId;
	}

	public SortedMap<String, String> getMessage() {
		return message;
	}

	public void setMessage(final SortedMap<String, String> message) {
		this.message = message;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	public List<WorkflowStateDTO> getStates() {
		return states;
	}

	public void setStates(final List<WorkflowStateDTO> states) {
		this.states = states;
	}

	public List<WorkflowActionDTO> getActions() {
		return actions;
	}

	public void setActions(final List<WorkflowActionDTO> actions) {
		this.actions = actions;
	}
}
