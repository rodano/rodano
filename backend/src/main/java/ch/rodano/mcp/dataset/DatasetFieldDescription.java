package ch.rodano.mcp.dataset;

import java.util.List;

public record DatasetFieldDescription(
	String id,
	String name,
	String type,
	String dataType,
	List<DatasetPossibleValue> possibleValues
) {
}
