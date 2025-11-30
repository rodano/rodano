package ch.rodano.api.cms;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A section contains labels and widgets")
public class CMSSectionDTO {
	@NotNull
	UUID menuSectionId;
	@Schema(description = "Unique ID of the section")
	@NotBlank
	String id;
	@Schema(description = "Section labels")
	@NotNull
	Map<String, String> labels;

	@Schema(description = "Section widgets")
	@NotNull
	List<CMSWidgetDTO> widgets;

	public UUID getMenuSectionId() {
		return menuSectionId;
	}

	public void setMenuSectionId(final UUID menuSectionId) {
		this.menuSectionId = menuSectionId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(final Map<String, String> labels) {
		this.labels = labels;
	}

	public List<CMSWidgetDTO> getWidgets() {
		return widgets;
	}

	public void setWidgets(final List<CMSWidgetDTO> widgets) {
		this.widgets = widgets;
	}
}
