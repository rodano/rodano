package ch.rodano.configuration.model.layout;

public enum VisibilityCriterionAction {
	SHOW("Show"), HIDE("Hide");

	public final String label;

	VisibilityCriterionAction(final String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
