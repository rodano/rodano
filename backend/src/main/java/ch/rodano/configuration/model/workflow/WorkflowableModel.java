package ch.rodano.configuration.model.workflow;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.common.Entity;

public interface WorkflowableModel extends Displayable {
	List<String> getWorkflowIds();

	List<Workflow> getWorkflows();

	@Override
	String getId();

	Entity getEntity();

	@JsonIgnore
	default List<WorkflowState> getImportantStates() {
		final List<WorkflowState> states = new ArrayList<>();
		this.getWorkflows().stream().map(Workflow::getStatesImportant).forEach(states::addAll);

		return states;
	}
}
