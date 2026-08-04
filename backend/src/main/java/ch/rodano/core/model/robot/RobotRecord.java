package ch.rodano.core.model.robot;

public class RobotRecord {

	protected boolean removed;

	protected String name;
	protected String key;

	protected RobotRecord() {
		removed = false;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(final boolean removed) {
		this.removed = removed;
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
