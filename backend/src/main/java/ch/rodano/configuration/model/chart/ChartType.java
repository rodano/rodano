package ch.rodano.configuration.model.chart;

public enum ChartType {
	ENROLLMENT_BY_SCOPE("Enrollment by scope"),
	ENROLLMENT("Enrollment"),
	STATISTICS("Statistics"),
	WORKFLOW_STATUS("Workflow status");

	public final String label;

	ChartType(final String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
