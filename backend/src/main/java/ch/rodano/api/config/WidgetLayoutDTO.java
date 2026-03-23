package ch.rodano.api.config;

import java.util.List;

public record WidgetLayoutDTO(
	List<SectionDTO> sections
) {
}
