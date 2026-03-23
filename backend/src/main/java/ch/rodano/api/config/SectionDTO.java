package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

public record SectionDTO(
	UUID sectionId,
	String id,
	SortedMap<String, String> label,
	int sortOrder,
	UUID requiredFeatureId,
	String rightEntity,
	String rightValue,
	UUID rightTargetId,
	List<WidgetDTO> widgets
) {
}
