package ch.rodano.core.services.bll.widget.overdue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.dto.widget.overdue.OverdueDTO;
import ch.rodano.core.model.scope.Scope;

public interface OverdueWidgetService {
	PagedResult<OverdueDTO> getData(
		String widgetId,
		Collection<Scope> scopes,
		String[] languages,
		Optional<String> fullText,
		Optional<String> sortBy,
		Optional<Boolean> orderAscending,
		Optional<Integer> pageSize,
		Optional<Integer> pageIndex
	);

	void export(
		String widgetId,
		OutputStream os,
		Collection<Scope> scopes,
		String[] languages
	) throws IOException;

	String getExportFilename(String widgetId);
}
