package ch.rodano.mcp.study;

import java.util.List;

public record StudySummary(
	String id,
	String name,
	String defaultLanguageId,
	List<String> scopeModelIds,
	List<String> datasetModelIds
) {
}
