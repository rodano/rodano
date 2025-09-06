package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormModelLayoutLine {

	private List<FormModelLayoutCell> cells;

	public List<FormModelLayoutCell> getCells() {
		return cells;
	}

	public void setCells(final List<FormModelLayoutCell> cells) {
		this.cells = cells;
	}

	@Override
	public String toString() {
		return "FormModelLayoutLine{" +
			"cells=" + cells +
			'}';
	}
}
