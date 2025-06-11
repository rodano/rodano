package ch.rodano.api.cms;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.configuration.model.cms.CMSWidget;
import ch.rodano.configuration.model.cms.WidgetWidth;

@Schema(description = "Widgets that are displayed in the layouts")
public class CMSWidgetDTO {
	@Schema(description = "Type of widget")
	@NotBlank
	String type;

	String textBefore;
	String textAfter;
	@Schema(description = "Width of the widget. Can only be HALF or FULL.")
	@NotNull
	WidgetWidth width;

	@Schema(description = "A map of widget parameters")
	@NotNull
	Map<String, Object> parameters;

	public CMSWidgetDTO(final CMSWidget cmsWidget) {
		this.type = cmsWidget.getType();

		this.textBefore = cmsWidget.getTextBefore();
		this.textAfter = cmsWidget.getTextAfter();
		this.width = cmsWidget.getWidth();

		this.parameters = cmsWidget.getParameters();
	}

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

	public String getTextAfter() {
		return textAfter;
	}

	public void setTextAfter(final String textAfter) {
		this.textAfter = textAfter;
	}

	public WidgetWidth getWidth() {
		return width;
	}

	public void setWidth(final WidgetWidth width) {
		this.width = width;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(final Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}
