package ch.rodano.core.services.bll.widget.workflow;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.configuration.model.reports.WorkflowWidget;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatusInfo;

public interface WorkflowWidgetService {

	PagedResult<WorkflowStatusInfo> getData(
		WorkflowWidget widget,
		Collection<Scope> scopes,
		String[] languages,
		Optional<String> fullText,
		Optional<String> sortBy,
		Optional<Boolean> orderAscending,
		Optional<Integer> pageSize,
		Optional<Integer> pageIndex
	);

	void getExport(OutputStream out, WorkflowWidget widget, Collection<Scope> scopes, String[] languages) throws IOException;
}
