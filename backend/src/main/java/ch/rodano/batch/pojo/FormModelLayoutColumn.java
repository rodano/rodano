package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormModelLayoutColumn {

	private String cssCode;

	public String getCssCode() {
		return cssCode;
	}

	public void setCssCode(final String cssCode) {
		this.cssCode = cssCode;
	}

	@Override
	public String toString() {
		return "FormModelLayoutColumn{" +
			"cssCode='" + cssCode + '\'' +
			'}';
	}
}
