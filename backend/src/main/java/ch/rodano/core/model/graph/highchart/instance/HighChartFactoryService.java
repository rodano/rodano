package ch.rodano.core.model.graph.highchart.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.predicate.ValueSource;
import ch.rodano.core.model.graph.highchart.HighChartSerie;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeService;

@Service
public class HighChartFactoryService {

	private final ScopeService scopeService;
	private final EnrollmentChartFactoryService enrollmentChartFactoryService;
	private final EnrollmentByScopeFactoryService enrollmentByScopeFactoryService;
	private final WorkflowStatusChartFactoryService workflowStatusChartFactoryService;
	private final StatisticsChartFactoryService statisticsChartFactoryService;

	public HighChartFactoryService(
		final ScopeService scopeService,
		final EnrollmentChartFactoryService enrollmentChartFactoryService,
		final EnrollmentByScopeFactoryService enrollmentByScopeFactoryService,
		final WorkflowStatusChartFactoryService workflowStatusChartFactoryService,
		final StatisticsChartFactoryService statisticsChartFactoryService
	) {
		this.scopeService = scopeService;
		this.enrollmentChartFactoryService = enrollmentChartFactoryService;
		this.enrollmentByScopeFactoryService = enrollmentByScopeFactoryService;
		this.workflowStatusChartFactoryService = workflowStatusChartFactoryService;
		this.statisticsChartFactoryService = statisticsChartFactoryService;
	}

	private HighGraph createChart(final Chart configuration, final String language, final List<Scope> scopes, final List<ValueSource> criteria) {
		switch(configuration.getType()) {
			case STATISTICS:
				return statisticsChartFactoryService.createChart(configuration, language, scopes, criteria);
			case WORKFLOW_STATUS:
				return workflowStatusChartFactoryService.createChart(configuration, language);
			case ENROLLMENT_BY_SCOPE: {
				final var chartScopes = configuration.getOverrideUserRights() ? Collections.singletonList(scopeService.getRootScope()) : scopes;
				return enrollmentByScopeFactoryService.createChart(configuration, language, chartScopes);
			}
			case ENROLLMENT: {
				final var chartScopes = configuration.getOverrideUserRights() ? Collections.singletonList(scopeService.getRootScope()) : scopes;
				return enrollmentChartFactoryService.createChart(configuration, language, chartScopes);
			}
			default:
				throw new UnsupportedOperationException();
		}
	}

	public HighGraph getChart(final Chart configuration, final String language, final List<Scope> scopes, final List<ValueSource> criteria) {
		final var chart = createChart(configuration, language, scopes, criteria);
		//transform values into series
		final var values = chart.getValues();
		final List<HighChartSerie> series = new ArrayList<>();
		values.keySet().forEach(key -> {
			final var serie = new HighChartSerie();
			serie.setName(key);
			serie.setData(String.format("[%s]", values.get(key)));
			series.add(serie);
		});
		chart.setSeries(series);
		return chart;
	}
}
