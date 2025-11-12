package ch.rodano.core.model.robot;

import java.util.UUID;

public class RobotRecord {

	protected boolean deleted;

	protected UUID projectId;
	protected String name;
	protected String key;

	protected RobotRecord() {
		deleted = false;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(final boolean deleted) {
		this.deleted = deleted;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}
}
