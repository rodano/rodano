package ch.rodano.core.services.bll.widget.chart;

import java.util.List;

import org.springframework.stereotype.Service;

import ch.rodano.api.config.ChartModelDTO;
import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.chart.ChartDTO;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;

@Service
public class ChartFactoryService {

	private final ActorService actorService;
	private final EnrollmentChartFactoryService enrollmentChartFactoryService;
	private final EnrollmentByScopeFactoryService enrollmentByScopeFactoryService;
	private final WorkflowStatusChartFactoryService workflowStatusChartFactoryService;
	private final StatisticsChartFactoryService statisticsChartFactoryService;

	public ChartFactoryService(
		final EnrollmentChartFactoryService enrollmentChartFactoryService,
		final EnrollmentByScopeFactoryService enrollmentByScopeFactoryService,
		final WorkflowStatusChartFactoryService workflowStatusChartFactoryService,
		final StatisticsChartFactoryService statisticsChartFactoryService,
		final ActorService actorService
	) {
		this.actorService = actorService;
		this.enrollmentChartFactoryService = enrollmentChartFactoryService;
		this.enrollmentByScopeFactoryService = enrollmentByScopeFactoryService;
		this.workflowStatusChartFactoryService = workflowStatusChartFactoryService;
		this.statisticsChartFactoryService = statisticsChartFactoryService;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ChartDTO<?, ?> getChart(final Chart chart, final Actor actor, final List<Scope> scopes, final List<FieldModelCriterion> criteria) {
		final var languages = actorService.getLanguages(actor);
		final var datasets = switch(chart.getType()) {
			case STATISTICS -> statisticsChartFactoryService.buildChartDatasets(chart, languages, scopes, criteria);
			case WORKFLOW_STATUS -> workflowStatusChartFactoryService.buildChartDatasets(chart, languages);
			case ENROLLMENT_BY_SCOPE -> enrollmentByScopeFactoryService.buildChartDatasets(chart, languages, scopes);
			case ENROLLMENT -> enrollmentChartFactoryService.buildChartDatasets(chart, scopes);
		};
		final var model = new ChartModelDTO(chart, languages);
		return new ChartDTO(model, datasets);
	}
}
