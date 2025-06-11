package ch.rodano.core.services.dao.chart.data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.dao.chart.ChartDTO;
import ch.rodano.core.utils.Utils;

import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;

@Service
public class EnrollmentStatusChartService {

	public static final int INT = 700;
	public static final int INT1 = 500;

	private final DSLContext dsl;

	public EnrollmentStatusChartService(final DSLContext dsl) {
		this.dsl = dsl;
	}

	public List<ChartDTO.ChartDataSeries> generateValues(
		final List<Scope> scopes,
		final String leafScopeModelId
	) {
		final SortedMap<ZonedDateTime, Integer> allNewScopes = new TreeMap<>();

		for (var scope : scopes) {
			final var scopedCounts = getNewScopesByDay(scope, leafScopeModelId);

			for (var entry : scopedCounts.entrySet()) {
				allNewScopes.merge(entry.getKey(), entry.getValue(), Integer::sum);
			}
		}

		// Compute cumulative values
		final List<List<Object>> values = new ArrayList<>();
		if (!allNewScopes.isEmpty()) {
			var total = 0;

			final var firstDay = allNewScopes.firstKey().minusDays(1);
			values.add(List.of(firstDay.toInstant().toString(), 0));

			final var spaceOut = Utils.getDifferencesInDayBetweenDate(allNewScopes.firstKey(), allNewScopes.lastKey()) > INT;
			final var size = allNewScopes.size();
			var nextSample = firstDay.plusMonths(size > INT1 ? 3 : 1);

			for (var entry : allNewScopes.entrySet()) {
				total += entry.getValue();

				if (!spaceOut || !entry.getKey().isBefore(nextSample)) {
					values.add(List.of(entry.getKey().toInstant().toString(), total));
					nextSample = nextSample.plusMonths(size > INT1 ? 3 : 1);
				}
			}

			// Add the final point (if not already added)
			final var lastKey = allNewScopes.lastKey();
			final var lastTimestamp = lastKey.toInstant().toString();
			if (values.isEmpty() || !values.getLast().getFirst().equals(lastTimestamp)) {
				values.add(List.of(lastTimestamp, total));
			}
		}

		return List.of(new ChartDTO.ChartDataSeries("ENROLLMENT_STATUS", values));
	}


	private Map<ZonedDateTime, Integer> getNewScopesByDay(
		final Scope scope,
		final String leafScopeModelId
	) {
		final var query = dsl.select(SCOPE.START_DATE, DSL.countDistinct(SCOPE.PK))
			.from(SCOPE)
			.join(SCOPE_ANCESTOR)
			.on(SCOPE.PK.eq(SCOPE_ANCESTOR.SCOPE_FK))
			.and(SCOPE_ANCESTOR.DEFAULT.isTrue());

		query.where(
				SCOPE.SCOPE_MODEL_ID.eq(leafScopeModelId)
					.and(SCOPE.DELETED.isFalse())
					.and(SCOPE_ANCESTOR.ANCESTOR_FK.eq(scope.getPk()))
			)
			.groupBy(SCOPE.START_DATE)
			.orderBy(SCOPE.START_DATE);

		return query.fetchMap(SCOPE.START_DATE, DSL.countDistinct(SCOPE.PK));
	}
}
