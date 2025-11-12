package ch.rodano.core.services.bll.widget.chart;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.core.model.chart.ChartDatasetDTO;
import ch.rodano.core.model.chart.ChartDatasetPoint;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;

@Service
public class EnrollmentByScopeFactoryService {

	private final ScopeService scopeService;
	private final ScopeRelationService scopeRelationService;

	public EnrollmentByScopeFactoryService(final ScopeService scopeService, final ScopeRelationService scopeRelationService) {
		this.scopeService = scopeService;
		this.scopeRelationService = scopeRelationService;
	}

	public List<ChartDatasetDTO<String, Integer>> buildChartDatasets(final Chart chart, final String[] languages, final Collection<Scope> rootScopes) {
		//find all the scopes matching the scope model that has been requested in the root scopes
		final Set<Scope> containers = new HashSet<Scope>();
		for(final Scope scope : rootScopes) {
			if(scope.getScopeModelId().equals(chart.getScopeModelUuid())) {
				containers.add(scope);
			}
			else {
				containers.addAll(scopeRelationService.getEnabledDescendants(scope, chart.getScopeModel()));
			}
		}

		//calculate counts
		final var counts = new TreeMap<>(scopeService.getLeafCount(containers));
		final var data = counts.entrySet().stream().map(e -> new ChartDatasetPoint<String, Integer>(e.getKey().getCode(), e.getValue())).toList();
		final var dataset = new ChartDatasetDTO<String, Integer>(chart.getLeafScopeModel().getLocalizedPluralShortname(languages), data);
		return List.of(dataset);
	}
}
