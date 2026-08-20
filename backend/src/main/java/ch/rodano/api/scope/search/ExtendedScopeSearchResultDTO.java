package ch.rodano.api.scope.search;


import java.util.List;
import java.util.Map;

import ch.rodano.api.scope.ScopeDTO;
import ch.rodano.api.workflow.WorkflowStatusDTO;

public class ExtendedScopeSearchResultDTO extends ScopeDTO {
	private List<WorkflowStatusDTO> workflowStatuses;
	private Map<String, Map<String, String>> fieldValues;

	public List<WorkflowStatusDTO> getWorkflowStatuses() {
		return workflowStatuses;
	}

	public void setWorkflowStatuses(final List<WorkflowStatusDTO> workflowStatuses) {
		this.workflowStatuses = workflowStatuses;
	}


	public Map<String, Map<String, String>> getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(final Map<String, Map<String, String>> fieldValues) {
		this.fieldValues = fieldValues;
	}
}
