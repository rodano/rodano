package ch.rodano.core.services.bll.widget.overdue;

import java.util.Collection;
import java.util.Optional;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.dto.widget.overdue.OverdueDTO;
import ch.rodano.core.model.scope.Scope;

public interface OverdueProvider {

	String getWidgetId();

	PagedResult<OverdueDTO> getData(
		Collection<Scope> scopes,
		Optional<String> fullText,
		Optional<String> sortBy,
		Optional<Boolean> orderAscending,
		Optional<Integer> pageSize,
		Optional<Integer> pageIndex
	);

	String getExportFilename();

	String getExportOverdueColumnName();

	Collection<OverdueDTO> getExport(Collection<Scope> scopes);

}
