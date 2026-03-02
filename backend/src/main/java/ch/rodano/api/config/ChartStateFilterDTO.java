package ch.rodano.api.config;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class ChartStateFilterDTO {

	public enum Kind {
		INCLUDED,
		EXCLUDED,
		ENROLLMENT
	}

	@NotNull
	private UUID workflowStateId;

	@NotNull
	private Kind kind;

	public UUID getWorkflowStateId() {
		return workflowStateId;
	}

	public void setWorkflowStateId(final UUID workflowStateId) {
		this.workflowStateId = workflowStateId;
	}

	public Kind getKind() {
		return kind;
	}

	public void setKind(final Kind kind) {
		this.kind = kind;
	}
}
