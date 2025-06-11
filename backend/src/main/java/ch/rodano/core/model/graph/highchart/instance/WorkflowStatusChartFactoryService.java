package ch.rodano.core.model.graph.highchart.instance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.core.services.bll.study.StudyService;

import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS_AUDIT;

@Service
public class WorkflowStatusChartFactoryService {

	private final DSLContext create;
	private final StudyService studyService;

	public WorkflowStatusChartFactoryService(
		final DSLContext create,
		final StudyService studyService
	) {
		this.create = create;
		this.studyService = studyService;
	}

	public Map<String, String> generateValues(final Chart configuration, final String language) {
		//select most recent audit date for each workflow
		final var conditions = new ArrayList<Condition>();
		conditions.add(WORKFLOW_STATUS_AUDIT.WORKFLOW_ID.eq(configuration.getWorkflowId()));
		conditions.add(SCOPE.DELETED.isFalse());
		if(CollectionUtils.isNotEmpty(configuration.getIncludedStateIds())) {
			conditions.add(WORKFLOW_STATUS_AUDIT.STATE_ID.in(configuration.getIncludedStateIds()));
		}
		if(CollectionUtils.isNotEmpty(configuration.getExcludedStateIds())) {
			conditions.add(WORKFLOW_STATUS_AUDIT.STATE_ID.notIn(configuration.getExcludedStateIds()));
		}
		//truncate audit trail date to the day (remove time)
		final var auditDate = DSL.function("date", SQLDataType.DATE, DSL.max(WORKFLOW_STATUS_AUDIT.AUDIT_DATETIME)).as("audit_date");
		final var auditDates = create.select(auditDate)
			.from(WORKFLOW_STATUS_AUDIT)
			.innerJoin(SCOPE).on(WORKFLOW_STATUS_AUDIT.SCOPE_FK.eq(SCOPE.PK))
			.where(DSL.and(conditions))
			.groupBy(WORKFLOW_STATUS_AUDIT.AUDIT_OBJECT_FK)
			.orderBy(WORKFLOW_STATUS_AUDIT.AUDIT_DATETIME).asTable("audit_dates");

		//count number of audit per date, using the previous query
		final var query = create.select(auditDates.field("audit_date", SQLDataType.DATE), DSL.count(auditDates.field("audit_date")))
			.from(auditDates)
			.groupBy(auditDates.fields(0));

		var total = 0;
		final SortedMap<Long, Integer> statusesByDate = new TreeMap<>();

		final var results = query.fetch();
		for(final var record : results) {
			total += record.value2();
			statusesByDate.put(record.value1().getTime(), total);
		}

		var series = "";

		if(!statusesByDate.isEmpty()) {
			//add 0 point
			final var calendar = Calendar.getInstance();
			calendar.setTimeInMillis(statusesByDate.firstKey());
			calendar.add(Calendar.DAY_OF_YEAR, -1);
			statusesByDate.put(calendar.getTime().getTime(), 0);

			//build series
			series = statusesByDate.entrySet()
				.stream()
				.map(e -> String.format("[%d, %d]", e.getKey(), e.getValue()))
				.collect(Collectors.joining(","));
		}

		final Map<String, String> values = new LinkedHashMap<>();
		values.put(studyService.getStudy().getWorkflow(configuration.getWorkflowId()).getLocalizedShortname(language), series);
		return values;
	}

	public WorkflowStatusChart createChart(final Chart configuration, final String language) {
		final var values = generateValues(configuration, language);
		return new WorkflowStatusChart(configuration, language, values);
	}
}
