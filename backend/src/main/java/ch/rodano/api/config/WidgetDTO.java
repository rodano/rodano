package ch.rodano.api.config;

import java.util.Map;
import java.util.UUID;

public record WidgetDTO(
	UUID widgetId,
	String type,
	String width,
	int widgetOrder,
	String textBefore,
	String textAfter,
	UUID requiredFeatureId,
	String rightEntity,
	String rightValue,
	UUID rightTargetId,
	Map<String, String> parameters
) {
}
