package ch.rodano.api.cms;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A layout contains of a list of sections")
public class CMSLayoutDTO {
	@Schema(description = "Layout sections")
	@NotNull
	List<CMSSectionDTO> sections;

	public List<CMSSectionDTO> getSections() {
		return sections;
	}

	public void setSections(final List<CMSSectionDTO> sections) {
		this.sections = sections;
	}
}
