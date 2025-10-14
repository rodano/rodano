package ch.rodano.api.search;

import java.util.List;

import ch.rodano.api.scope.ScopeDTO;

import java.util.Map;

public class ExtendedScopeSearchResultDTO {
	private ScopeDTO scope;

	private List<Map<String, String>> workflowStatuses;
	private Map<String, Map<String, String>> fieldValues;

	public ExtendedScopeSearchResultDTO() {}

	public ExtendedScopeSearchResultDTO(ScopeDTO scope, List<Map<String, String>> workflowStatuses, Map<String, Map<String, String>> fieldValues) {
		this.workflowStatuses = workflowStatuses;
		this.fieldValues = fieldValues;
		this.scope = scope;
	}

	public ScopeDTO getScope() {
		return scope;
	}

	public void setScope(final ScopeDTO scope) {
		this.scope = scope;
	}

	public List<Map<String, String>> getWorkflowStatuses() {
		return workflowStatuses;
	}

	public void setWorkflowStatuses(List<Map<String, String>> workflowStatuses) {
		this.workflowStatuses = workflowStatuses;
	}

	public Map<String, Map<String, String>> getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(Map<String, Map<String, String>> fieldValues) {
		this.fieldValues = fieldValues;
	}
}
