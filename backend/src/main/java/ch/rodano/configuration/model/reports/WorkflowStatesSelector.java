package ch.rodano.configuration.model.reports;


import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.study.Study;

import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class WorkflowStatesSelector implements Node {
	@Serial
	private static final long serialVersionUID = -1703578816489405753L;

	private String workflowId;
	private Set<String> stateIds;

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public Set<String> getStateIds() {
		return stateIds;
	}

	public void setStateIds(final Set<String> stateIds) {
		this.stateIds = stateIds;
	}

	@Override
	public Entity getEntity() {
		return Entity.WORKFLOW_STATE_SELECTOR;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

	@JsonIgnore
	public UUID getWorkflowUuid(final Study study) {
		return this.workflowId == null || this.workflowId.isBlank()
			? null
			: deterministic(study.getProjectId(), "WORKFLOW", this.workflowId);
	}

	@JsonIgnore
	public Set<UUID> getStateUuids(final Study study) {
		return this.stateIds == null
			? Set.of()
			: this.stateIds.stream()
			.map(id -> deterministic(study.getProjectId(), "WORKFLOW_STATE", this.workflowId + "|" + id))
			.collect(Collectors.toUnmodifiableSet());
	}
}
