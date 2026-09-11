package ch.rodano.mcp.dataset;

import java.util.List;

public record DatasetDescription(
	String id,
	String name,
	boolean scopeDocumentation,
	boolean multiple,
	List<DatasetFieldDescription> fields
) {
}
