package ch.rodano.core.services.bll.widget.chart;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.core.helpers.configuration.DateConverter;
import ch.rodano.core.model.chart.ChartDatasetDTO;
import ch.rodano.core.services.bll.study.StudyService;

import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS_AUDIT;

@Service
public class WorkflowStatusChartFactoryService {

	private final DSLContext create;
	private final StudyService studyService;
	private final DateConverter dateConverter;

	public WorkflowStatusChartFactoryService(
		final DSLContext create,
		final StudyService studyService
	) {
		this.create = create;
		this.studyService = studyService;
		this.dateConverter = new DateConverter();
	}

	public List<ChartDatasetDTO<ZonedDateTime, Integer>> buildChartDatasets(final Chart chart, final String[] languages) {
		//the first step is to select the most recent audit date for each workflow
		final var conditions = new ArrayList<Condition>();
		conditions.add(WORKFLOW_STATUS_AUDIT.WORKFLOW_ID.eq(chart.getWorkflowId()));
		conditions.add(SCOPE.REMOVED.isFalse());
		if(CollectionUtils.isNotEmpty(chart.getIncludedStateIds())) {
			conditions.add(WORKFLOW_STATUS_AUDIT.STATE_ID.in(chart.getIncludedStateIds()));
		}
		if(CollectionUtils.isNotEmpty(chart.getExcludedStateIds())) {
			conditions.add(WORKFLOW_STATUS_AUDIT.STATE_ID.notIn(chart.getExcludedStateIds()));
		}
		//truncate audit trail date to the day (remove time)
		final var auditDate = DSL.function("date", SQLDataType.LOCALDATETIME, DSL.max(WORKFLOW_STATUS_AUDIT.AUDIT_DATETIME)).as("audit_date");
		final var auditDates = create.select(auditDate)
			.from(WORKFLOW_STATUS_AUDIT)
			.innerJoin(SCOPE).on(WORKFLOW_STATUS_AUDIT.SCOPE_FK.eq(SCOPE.PK))
			.where(DSL.and(conditions))
			.groupBy(WORKFLOW_STATUS_AUDIT.AUDIT_OBJECT_FK)
			.orderBy(WORKFLOW_STATUS_AUDIT.AUDIT_DATETIME).asTable("audit_dates");

		//count number of audit per date, based on the previous query
		final var dateField = auditDates.field("audit_date", SQLDataType.LOCALDATETIME.asConvertedDataType(this.dateConverter));
		final var countField = DSL.count(auditDates.field("audit_date"));
		final var query = create.select(dateField, countField)
			.from(auditDates)
			.groupBy(auditDates.fields(0));

		final var results = new TreeMap<>(query.fetchMap(dateField, countField));
		final var cumulatedResults = ChartHelpers.cumulate(results);
		final var downsampledResults = ChartHelpers.downsample(cumulatedResults, ChartHelpers.DEFAULT_MAX_POINTS);
		final var timepoints = ChartHelpers.processTimepoints(downsampledResults);

		final var workflow = studyService.getStudy().getWorkflow(chart.getWorkflowId());
		return List.of(new ChartDatasetDTO<ZonedDateTime, Integer>(workflow.getLocalizedShortname(languages), timepoints));
	}
}
