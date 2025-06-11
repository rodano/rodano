package ch.rodano.core.model.graph.highchart.instance;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.chart.ChartRange;
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

	private Set<Scope> getContainers(final Chart configuration, final List<Scope> rootScopes) {
		//find container scopes
		final Set<Scope> containers = new HashSet<Scope>();
		for(final Scope scope : rootScopes) {
			if(scope.getScopeModelId().equals(configuration.getScopeModelId())) {
				containers.add(scope);
			}
			else {
				containers.addAll(scopeRelationService.getEnabledDescendants(scope, configuration.getScopeModel()));
			}
		}
		return containers;
	}

	private SortedMap<Scope, Integer> calculateCounts(final Set<Scope> containers) {
		return new TreeMap<>(scopeService.getLeafCount(containers));
	}

	private ChartRange generateRange(final String language, final Scope scope) {
		final ChartRange range = new ChartRange();
		range.setId(scope.getId());

		final Map<String, String> labels = new HashMap<>();
		labels.put(language, scope.getCode());

		range.setLabels(labels);
		range.setShow(true);
		return range;
	}

	private List<ChartRange> generateRanges(final String language, final SortedMap<Scope, Integer> counts) {
		return counts.entrySet().stream()
			.filter(e -> e.getValue() > 0)
			.map(Entry::getKey)
			.map(k -> generateRange(language, k))
			.toList();
	}

	private Map<String, String> generateValues(final Chart configuration, final SortedMap<Scope, Integer> counts) {
		final List<String> values = counts.values().stream()
			.filter(v -> v > 0)
			.map(Object::toString)
			.toList();
		return Collections.singletonMap(configuration.getScopeModelId(), StringUtils.join(values, ','));
	}

	public EnrollmentByScopeChart createChart(final Chart configuration, final String language, final List<Scope> scopes) {
		final var containers = getContainers(configuration, scopes);
		final var counts = calculateCounts(containers);
		final var ranges = generateRanges(language, counts);
		final var values = generateValues(configuration, counts);
		return new EnrollmentByScopeChart(configuration, language, ranges, values);
	}
}
