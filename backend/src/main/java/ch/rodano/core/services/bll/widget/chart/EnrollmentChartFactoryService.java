package ch.rodano.core.services.bll.widget.chart;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.core.helpers.configuration.DateConverter;
import ch.rodano.core.model.chart.ChartDatasetDTO;
import ch.rodano.core.model.chart.ChartDatasetPoint;
import ch.rodano.core.model.scope.Scope;

import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;

@Service
public class EnrollmentChartFactoryService {

	private final DSLContext create;

	private final DateConverter dateConverter;

	public EnrollmentChartFactoryService(
		final DSLContext create
	) {
		this.create = create;
		this.dateConverter = new DateConverter();
	}

	private Map<ZonedDateTime, Integer> getEnrolledScopesPerDate(final Chart chart, final Scope scope) {
		//truncate date to the day (remove time)
		final var scopeDate = DSL.function("date", SQLDataType.LOCALDATETIME.asConvertedDataType(this.dateConverter), SCOPE.START_DATE);
		var query = create.select(scopeDate, DSL.countDistinct(SCOPE.PK))
			.from(SCOPE)
			.join(SCOPE_ANCESTOR).on(SCOPE.PK.eq(SCOPE_ANCESTOR.SCOPE_FK).and(SCOPE_ANCESTOR.DEFAULT.isTrue()));

		if(StringUtils.isNotEmpty(chart.getEnrollmentWorkflowId()) && !chart.getEnrollmentStateIds().isEmpty()) {
			query = query.innerJoin(WORKFLOW_STATUS)
				.on(
					SCOPE.PK.eq(WORKFLOW_STATUS.SCOPE_FK)
						.and(WORKFLOW_STATUS.WORKFLOW_ID.eq(chart.getEnrollmentWorkflowId()))
						.and(WORKFLOW_STATUS.STATE_ID.in(chart.getEnrollmentStateIds()))
						.and(WORKFLOW_STATUS.DELETED.isFalse())
				);
		}
		query.where(
				SCOPE.SCOPE_MODEL_ID.eq(chart.getLeafScopeModelId())
					.and(SCOPE.DELETED.isFalse())
					.and(SCOPE_ANCESTOR.ANCESTOR_FK.eq(scope.getPk()))
			)
			.groupBy(scopeDate)
			.orderBy(scopeDate);

		return query.fetchMap(scopeDate, DSL.countDistinct(SCOPE.PK));
	}

	public List<ChartDatasetDTO<ZonedDateTime, Integer>> buildChartDatasets(final Chart chart, final Collection<Scope> scopes) {
		final List<ChartDatasetDTO<ZonedDateTime, Integer>> datasets = new ArrayList<>();

		for(final var scope : scopes) {
			final var results = new TreeMap<>(getEnrolledScopesPerDate(chart, scope));
			final var cumulatedResults = ChartHelpers.cumulate(results);
			final var downsampledResults = ChartHelpers.downsample(cumulatedResults, ChartHelpers.DEFAULT_MAX_POINTS);
			final var timepoints = ChartHelpers.processTimepoints(downsampledResults);

			datasets.add(new ChartDatasetDTO<ZonedDateTime, Integer>(scope.getCode(), timepoints));

			//expected data
			final var expectedTimepoints = new ArrayList<ChartDatasetPoint<ZonedDateTime, Integer>>();
			if(chart.isDisplayExpected() && !scope.getData().getEnrollmentTargets().isEmpty()) {
				expectedTimepoints.add(new ChartDatasetPoint<ZonedDateTime, Integer>(scope.getData().getEnrollmentStart(), 0));

				//middle points
				scope.getData().getEnrollmentTargets()
					.stream()
					.map(target -> new ChartDatasetPoint<ZonedDateTime, Integer>(target.getDate(), target.getExpectedNumber()))
					.forEach(expectedTimepoints::add);

				datasets.add(new ChartDatasetDTO<ZonedDateTime, Integer>(String.format("%s (expected)", scope.getCode()), expectedTimepoints));
			}
		}
		return datasets;
	}
}
