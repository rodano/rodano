package ch.rodano.core.services.bll.widget.overdue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.dto.widget.overdue.OverdueDTO;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.UtilsService;

@Service
public class OverdueWidgetServiceImpl implements OverdueWidgetService {
	private final StudyService studyService;
	private final Map<String, OverdueProvider> providers;

	public OverdueWidgetServiceImpl(
		final StudyService studyService,
		final List<OverdueProvider> providers
	) {
		this.studyService = studyService;
		this.providers = providers.stream()
			.collect(Collectors.toMap(OverdueProvider::getWidgetId, Function.identity()));
	}

	private OverdueProvider getProvider(final String widgetId) {
		final var provider = providers.get(widgetId);
		if(provider == null) {
			throw new UnsupportedOperationException(String.format("Widget %s not supported", widgetId));
		}
		return provider;
	}

	@Override
	public PagedResult<OverdueDTO> getData(
		final String widgetId,
		final Collection<Scope> scopes,
		final String[] languages,
		final Optional<String> fullText,
		final Optional<String> sortBy,
		final Optional<Boolean> orderAscending,
		final Optional<Integer> pageSize,
		final Optional<Integer> pageIndex
	) {
		final var provider = getProvider(widgetId);
		return provider.getData(
			scopes,
			fullText,
			sortBy,
			orderAscending,
			pageSize,
			pageIndex
		);
	}

	@Override
	public void export(
		final String widgetId,
		final OutputStream os,
		final Collection<Scope> scopes,
		final String[] languages
	) throws IOException {
		//perform the export using the data
		final var provider = getProvider(widgetId);
		final var overdueScopes = provider.getExport(scopes);

		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();

		try(final var writer = new CSVWriter(new OutputStreamWriter(os))) {
			//header
			final var header = Arrays.asList(
				leafScopeModel.getDefaultParent().getDefaultLocalizedShortname(),
				leafScopeModel.getDefaultLocalizedShortname(),
				provider.getExportOverdueColumnName(),
				"Days overdue"
			);

			writer.writeNext(header.toArray(new String[0]));

			//content
			for(final var overdueScope : overdueScopes) {
				final var line = new ArrayList<String>(header.size());
				line.add(overdueScope.parentScopeCode());
				line.add(overdueScope.scopeCode());
				line.add(overdueScope.lastDate() != null ? overdueScope.lastDate().format(UtilsService.HUMAN_READABLE_DATE_TIME) : "Has never been performed");
				line.add(overdueScope.daysOverdue() != null ? Long.toString(overdueScope.daysOverdue()) : "NA");

				writer.writeNext(line.toArray(new String[0]));
			}
		}
	}

	@Override
	public String getExportFilename(final String widgetId) {
		return getProvider(widgetId).getExportFilename();
	}

}
