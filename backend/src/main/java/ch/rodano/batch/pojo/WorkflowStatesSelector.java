package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowStatesSelector {

	private String workflowId;
	private List<String> stateIds;

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public List<String> getStateIds() {
		return stateIds;
	}

	public void setStateIds(final List<String> stateIds) {
		this.stateIds = stateIds;
	}

	@Override
	public String toString() {
		return "WorkflowStatesSelector{" +
			"workflowId='" + workflowId + '\'' +
			", stateIds=" + stateIds +
			'}';
	}
}
