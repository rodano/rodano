package ch.rodano.core.model.role;

public enum RoleStatus {
	PENDING("Pending"),
	ENABLED("Enabled"),
	DISABLED("Disabled");

	private final String label;

	RoleStatus(final String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
