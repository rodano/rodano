package ch.rodano.core.services.dao.chart.data;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import ch.rodano.core.services.dao.chart.ChartDTO;

import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS_AUDIT;

@Service
public class WorkflowStatusChartService {

	private final DSLContext dsl;

	public record DataPoint(long timestamp, int value) {}

	public WorkflowStatusChartService(final DSLContext dsl) {
		this.dsl = dsl;
	}

	/**
	 * @param workflowId the workflow to chart (e.g. "SIGNATURE")
	 * @param stateIds the list of states you want separate series for (e.g. ["SIGNED","UNSIGNED"])
	 */
	public List<ChartDTO.ChartDataSeries> generateValues(
		final String workflowId,
		final List<String> stateIds
	) {
		// 1) Fetch all audit events for this workflow, oldest → newest
		final var events = fetchAuditEvents(workflowId);

		// 2) If no explicit states requested, discover them from events
		final Set<String> toTrack = (stateIds != null && !stateIds.isEmpty())
			? new LinkedHashSet<>(stateIds)
			: events.stream().map(e -> e.state).collect(Collectors.toCollection(LinkedHashSet::new));

		// 3) Build one time series per state
		final var result = buildTimeSeries(events, toTrack);

		// 4) Convert to List<ChartDTO.ChartDataSeries>
		final List<ChartDTO.ChartDataSeries> seriesList = new ArrayList<>();
		for (var entry : result.entrySet()) {
			final var values = entry.getValue().stream()
				.map(dp -> List.<Object>of(dp.timestamp(), dp.value()))
				.toList();
			seriesList.add(new ChartDTO.ChartDataSeries(entry.getKey(), values));
		}

		return seriesList;
	}

	// --- Step 1: Fetch raw audit events ------------------------------------------------

	private List<AuditEvent> fetchAuditEvents(final String workflowId) {
		return dsl
			.select(
				WORKFLOW_STATUS_AUDIT.AUDIT_OBJECT_FK,
				WORKFLOW_STATUS_AUDIT.STATE_ID,
				WORKFLOW_STATUS_AUDIT.AUDIT_DATETIME
			)
			.from(WORKFLOW_STATUS_AUDIT)
			.where(WORKFLOW_STATUS_AUDIT.WORKFLOW_ID.eq(workflowId))
			.orderBy(WORKFLOW_STATUS_AUDIT.AUDIT_DATETIME)
			.fetch(record -> new AuditEvent(
				record.get(WORKFLOW_STATUS_AUDIT.AUDIT_OBJECT_FK),
				record.get(WORKFLOW_STATUS_AUDIT.STATE_ID),
				record.get(WORKFLOW_STATUS_AUDIT.AUDIT_DATETIME).toInstant()
					.atZone(ZoneOffset.UTC).toLocalDate()
			));
	}

	/**
	 * @param date we bucket by date only
	 */
	private record AuditEvent(long objectFk, String state, LocalDate date) {
	}

	// --- Step 2 & 3: Build per‐state time series -----------------------------------------

	private Map<String,List<DataPoint>> buildTimeSeries(
		final List<AuditEvent> events,
		final Set<String> statesToTrack
	) {
		// group events by date
		final Map<LocalDate,List<AuditEvent>> byDate = events.stream()
			.collect(Collectors.groupingBy(e -> e.date, TreeMap::new, Collectors.toList()));

		// keep track of each object’s current state
		final Map<Long,String> currentStateByObject = new HashMap<>();
		// keep running counts per state
		final Map<String,Integer> counts = new HashMap<>();
		statesToTrack.forEach(s -> counts.put(s, 0));

		// prepare output lists
		final Map<String,List<DataPoint>> result = new LinkedHashMap<>();
		statesToTrack.forEach(state ->
			result.put(state, new ArrayList<>())
		);

		// if we have at least one date, prepend a zero‐point the day before
		if (!byDate.isEmpty()) {
			final var first = byDate.keySet().iterator().next();
			final var ts0 = first.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
			for (var state : statesToTrack) {
				result.get(state).add(new DataPoint(ts0, 0));
			}
		}

		// now process day by day
		for (var entry : byDate.entrySet()) {
			final var day = entry.getKey();
			final var ts = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

			// apply each event for that day, updating counts
			for (var ev : entry.getValue()) {
				final var previous = currentStateByObject.put(ev.objectFk, ev.state);
				// decrement old
				if (previous != null && counts.containsKey(previous)) {
					counts.put(previous, counts.get(previous) - 1);
				}
				// increment new
				if (counts.containsKey(ev.state)) {
					counts.put(ev.state, counts.get(ev.state) + 1);
				}
			}

			// emit a point for each tracked state
			for (var state : statesToTrack) {
				result.get(state).add(new DataPoint(ts, counts.get(state)));
			}
		}

		return result;
	}
}


