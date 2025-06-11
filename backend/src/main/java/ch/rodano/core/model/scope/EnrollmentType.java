package ch.rodano.core.model.scope;

public enum EnrollmentType {
	AUTOMATIC("Automatic"), MANUAL("Manual");

	public final String label;

	EnrollmentType(final String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
