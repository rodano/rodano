package ch.rodano.api.configurator.dto;

import java.util.Arrays;
import java.util.stream.Collectors;

import ch.rodano.core.model.rules.formula.FormulaFunction;

public class FormulaFunctionDTO {

	private String id;
	private String label;
	private String value;
	private String category;

	public FormulaFunctionDTO() {
	}

	public FormulaFunctionDTO(final FormulaFunction function) {
		this.id = function.name();
		this.label = formatLabel(function.name());
		this.value = "=" + function.name() + "(";
		this.category = determineCategory(function.name());
	}

	private String formatLabel(final String name) {
		final String[] words = name.replace("_", " ").toLowerCase().split("\\s+");
		return Arrays.stream(words)
			.map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
			.collect(Collectors.joining(" "));
	}

	private String determineCategory(final String name) {
		if("DATE".contains(name) || "TODAY".equals(name) || "ADD_".startsWith(name) || "DIFFERENCE_".startsWith(name)) {
			return "Date Function";
		}
		else if(name.startsWith("IS_")) {
			return "Condition Function";
		}
		else if("STRING".contains(name) || "CONCAT".equals(name) || "UPPERCASE".equals(name) || "LOWERCASE".equals(name)) {
			return "String Function";
		}
		else {
			return "Number Function";
		}
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

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}
}
