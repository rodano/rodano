package ch.rodano.mcp.dataset;

import java.util.List;

import ch.rodano.core.services.bll.export.extract.DataExtractRow;

public record DatasetQueryResult(
	String datasetModelId,
	List<DataExtractRow> rows,
	boolean truncated
) {
}
