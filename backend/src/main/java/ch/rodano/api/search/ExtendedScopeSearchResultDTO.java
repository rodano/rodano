package ch.rodano.api.search;

import java.util.List;
import java.util.Map;

import ch.rodano.api.scope.ScopeDTO;

public class ExtendedScopeSearchResultDTO {
	private ScopeDTO scope;

	private List<Map<String, String>> workflowStatuses;
	private Map<String, Map<String, String>> fieldValues;

	public ExtendedScopeSearchResultDTO() {}

	public ExtendedScopeSearchResultDTO(final ScopeDTO scope, final List<Map<String, String>> workflowStatuses, final Map<String, Map<String, String>> fieldValues) {
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

	public void setWorkflowStatuses(final List<Map<String, String>> workflowStatuses) {
		this.workflowStatuses = workflowStatuses;
	}

	public Map<String, Map<String, String>> getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(final Map<String, Map<String, String>> fieldValues) {
		this.fieldValues = fieldValues;
	}
}
