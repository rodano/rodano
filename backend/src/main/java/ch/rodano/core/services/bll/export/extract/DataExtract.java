package ch.rodano.core.services.bll.export.extract;

import java.util.List;

public record DataExtract(
	List<DataExtractRow> rows,
	boolean truncated
) {
}
