package ch.rodano.api.configurator.dto;

public class FormulaConditionDTO {

	private String id;
	private String label;
	private String value;

	public FormulaConditionDTO() {
	}

	public FormulaConditionDTO(final String id, final String label, final String value) {
		this.id = id;
		this.label = label;
		this.value = "=" + value + ":";
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
}
