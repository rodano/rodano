package ch.rodano.core.services.bll.export.extract;

import java.util.Map;

public record DataExtractRow(
	Long datasetPk,
	Long scopePk,
	String scopeCode,
	Long eventPk,
	String eventModelId,
	Map<String, String> values
) {
}
