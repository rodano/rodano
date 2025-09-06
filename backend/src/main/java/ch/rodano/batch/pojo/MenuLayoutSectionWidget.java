package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuLayoutSectionWidget {

	private String type;
	private String textBefore;
	private String width;
	private ScopeCriterionRight requiredRight;
	private String requiredFeature;
	private MenuLayoutSectionWidgetParameter parameters;

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getTextBefore() {
		return textBefore;
	}

	public void setTextBefore(final String textBefore) {
		this.textBefore = textBefore;
	}

	public MenuLayoutSectionWidgetParameter getParameters() {
		return parameters;
	}

	public void setParameters(final MenuLayoutSectionWidgetParameter parameters) {
		this.parameters = parameters;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(final String width) {
		this.width = width;
	}

	public ScopeCriterionRight getRequiredRight() {
		return requiredRight;
	}

	public void setRequiredRight(final ScopeCriterionRight requiredRight) {
		this.requiredRight = requiredRight;
	}

	public String getRequiredFeature() {
		return requiredFeature;
	}

	public void setRequiredFeature(final String requiredFeature) {
		this.requiredFeature = requiredFeature;
	}

	@Override
	public String toString() {
		return "MenuLayoutSectionWidget{" +
			"type='" + type + '\'' +
			", textBefore='" + textBefore + '\'' +
			", parameters=" + parameters +
			", width='" + width + '\'' +
			", requiredRight=" + requiredRight +
			", requiredFeature='" + requiredFeature + '\'' +
			'}';
	}
}
