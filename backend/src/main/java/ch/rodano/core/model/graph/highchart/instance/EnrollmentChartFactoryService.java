package ch.rodano.core.model.graph.highchart.instance;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.Utils;

import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;

@Service
public class EnrollmentChartFactoryService {

	private final DSLContext create;
	private final StudyService studyService;

	public EnrollmentChartFactoryService(
		final DSLContext create,
		final StudyService studyService
	) {
		this.create = create;
		this.studyService = studyService;
	}

	public Map<ZonedDateTime, Integer> getNewScopesByDay(final Chart configuration, final Scope scope, final ScopeModel leafScopeModel) {
		var query = create.select(SCOPE.START_DATE, DSL.countDistinct(SCOPE.PK))
			.from(SCOPE)
			.join(SCOPE_ANCESTOR).on(SCOPE.PK.eq(SCOPE_ANCESTOR.SCOPE_FK).and(SCOPE_ANCESTOR.DEFAULT.isTrue()));

		if(StringUtils.isNotEmpty(configuration.getEnrollmentWorkflowId()) && !configuration.getEnrollmentStateIds().isEmpty()) {
			query = query.innerJoin(WORKFLOW_STATUS)
				.on(
					SCOPE.PK.eq(WORKFLOW_STATUS.SCOPE_FK)
						.and(WORKFLOW_STATUS.WORKFLOW_ID.eq(configuration.getEnrollmentWorkflowId()))
						.and(WORKFLOW_STATUS.STATE_ID.in(configuration.getEnrollmentStateIds()))
				);
		}
		query.where(
			SCOPE.SCOPE_MODEL_ID.eq(leafScopeModel.getId())
				.and(SCOPE.DELETED.isFalse())
				.and(SCOPE_ANCESTOR.ANCESTOR_FK.eq(scope.getPk()))
		)
			.groupBy(SCOPE.START_DATE)
			.orderBy(SCOPE.START_DATE);

		return query.fetchMap(SCOPE.START_DATE, DSL.countDistinct(SCOPE.PK));
	}

	private Map<String, String> generateValues(final Chart configuration, final List<Scope> scopes) {
		final Map<String, String> values = new LinkedHashMap<>();
		final var leafScopeModel = StringUtils.isNotBlank(configuration.getLeafScopeModelId()) ? configuration.getLeafScopeModel() : studyService.getStudy().getLeafScopeModel();

		for(final var scope : scopes) {
			final var serie = new StringBuilder();

			var totalNewScopes = 0;
			final SortedMap<ZonedDateTime, Integer> allNewScopes = new TreeMap<>();
			final var newScopes = getNewScopesByDay(configuration, scope, leafScopeModel);

			for(final var date : newScopes.keySet()) {
				if(!allNewScopes.containsKey(date)) {
					totalNewScopes += newScopes.get(date);
				}
				else {
					totalNewScopes += newScopes.get(date) + allNewScopes.get(date);
				}
				allNewScopes.put(date, totalNewScopes);
			}

			if(!allNewScopes.isEmpty()) {
				//add 0 point
				var firstDay = allNewScopes.firstKey();
				firstDay = firstDay.minusDays(1);
				serie.append(String.format("[%d, 0],", firstDay.toInstant().toEpochMilli()));

				//add points
				var spaceOut = false;
				if(Utils.getDifferencesInDayBetweenDate(allNewScopes.firstKey(), allNewScopes.lastKey()) > 700) {
					spaceOut = true;
				}

				var first = true;
				final var size = allNewScopes.size();
				firstDay = firstDay.plusMonths(size > 500 ? 3 : 1);
				for(final var date : allNewScopes.keySet()) {
					//add point
					if(!spaceOut || !date.isBefore(firstDay)) {
						if(!first) {
							serie.append(",");
						}
						else {
							first = false;
						}
						serie.append(String.format("[%d, %d]", date.toInstant().toEpochMilli(), allNewScopes.get(date)));
						firstDay = firstDay.plusMonths(size > 500 ? 3 : 1);
					}
				}

				serie.append(String.format(",[%d, %d]", allNewScopes.lastKey().toInstant().toEpochMilli(), allNewScopes.get(allNewScopes.lastKey())));
				values.put(scope.getCode(), serie.toString());
			}

			//expected data
			if(configuration.isDisplayExpected() && !scope.getData().getEnrollmentTargets().isEmpty()) {
				final var serieExpected = new StringBuilder();
				serieExpected.append(String.format("[%d, 0],", scope.getData().getEnrollmentStart().toInstant().toEpochMilli()));

				//middle points
				scope.getData().getEnrollmentTargets()
					.stream()
					.map(target -> String.format("[%d, %d],", target.getDate().toInstant().toEpochMilli(), target.getExpectedNumber()))
					.forEach(serieExpected::append);

				//last point
				serieExpected.append(String.format("[%d, %d]", scope.getData().getEnrollmentStop().toInstant().toEpochMilli()));
				values.put(String.format("%s expected", scope.getCode()), serieExpected.toString());
			}
		}
		return values;
	}

	public EnrollmentChart createChart(final Chart configuration, final String language, final List<Scope> scopes) {
		final var values = generateValues(configuration, scopes);
		return new EnrollmentChart(configuration, language, values);
	}
}
