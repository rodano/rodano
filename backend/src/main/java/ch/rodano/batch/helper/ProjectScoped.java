package ch.rodano.batch.helper;

import java.util.UUID;

public class ProjectScoped<T> {

	private UUID projectId;
	private T payload;

	public ProjectScoped() {
	}

	public ProjectScoped(final UUID projectId, final T payload) {
		this.projectId = projectId;
		this.payload = payload;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public T getPayload() {
		return payload;
	}

	public void setPayload(final T payload) {
		this.payload = payload;
	}
}
