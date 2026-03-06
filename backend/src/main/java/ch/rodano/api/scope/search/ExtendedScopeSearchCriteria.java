package ch.rodano.api.scope.search;

import java.util.List;
import java.util.Map;

public class ExtendedScopeSearchCriteria {

	private List<Map<String, String>> workflowStatuses;
	private Map<String, Object> fieldValues;

	public ExtendedScopeSearchCriteria() {
	}

	public ExtendedScopeSearchCriteria(final List<Map<String, String>> workflowStatuses, final Map<String, Object> fieldValues) {
		this.workflowStatuses = workflowStatuses;
		this.fieldValues = fieldValues;
	}

	public List<Map<String, String>> getWorkflowStatuses() {
		return workflowStatuses;
	}

	public void setWorkflowStatuses(final List<Map<String, String>> workflowStatuses) {
		this.workflowStatuses = workflowStatuses;
	}

	public Map<String, Object> getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(final Map<String, Object> fieldValues) {
		this.fieldValues = fieldValues;
	}
}
