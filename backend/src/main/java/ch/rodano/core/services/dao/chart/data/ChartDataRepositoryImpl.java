package ch.rodano.core.services.dao.chart.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.chart.ChartDTO;


@Repository
public class ChartDataRepositoryImpl implements ChartDataRepository {

	private final ScopeService scopeService;
	private final EnrollmentStatusChartService enrollmentStatusChartService;
	private final EnrollmentByScopeChartService enrollmentByScopeChartService;
	private final StatisticsChartServiceNew statisticsChartService;
	private final WorkflowStatusChartService workflowStatusChartService;
	private final ObjectMapper objectMapper;

	public ChartDataRepositoryImpl(
		final ScopeService scopeService,
		final EnrollmentStatusChartService enrollmentStatusChartService,
		final EnrollmentByScopeChartService enrollmentByScopeChartService,
		final WorkflowStatusChartService workflowStatusChartService,
		final StatisticsChartServiceNew statisticsChartService,
		final ObjectMapper objectMapper
	) {
		this.scopeService = scopeService;
		this.enrollmentStatusChartService = enrollmentStatusChartService;
		this.enrollmentByScopeChartService = enrollmentByScopeChartService;
		this.statisticsChartService = statisticsChartService;
		this.workflowStatusChartService = workflowStatusChartService;
		this.objectMapper = objectMapper;
	}

	@Override
	public List<ChartDTO.ChartDataSeries> getEnrollmentStatusData(final Map<String, Object> requestParams) {
		final var ctx = extractChartContext(requestParams);
		final var scopes = Collections.singletonList(scopeService.getRootScope());

		return enrollmentStatusChartService.generateValues(scopes, ctx.leafScopeModelId());
	}

	@Override
	public List<ChartDTO.ChartDataSeries> getEnrollmentByScopeData(final Map<String, Object> requestParams) {
		final var ctx = extractChartContext(requestParams);

		return enrollmentByScopeChartService.generateValues(ctx.leafScopeModelId(), ctx.scopeModelId());
	}

	public List<ChartDTO.ChartDataSeries> getStatisticsData(
		final Map<String, Object> requestParams,
		final List<String> selectedRootScopeIds,
		final List<FieldModelCriterion> criteria
	) {
		final var ctx = extractChartContext(requestParams);

		return statisticsChartService.generateValues(
			ctx.scopeModelId(),
			ctx.leafScopeModelId(),
			ctx.datasetModelId(),
			ctx.fieldModelId(),
			ctx.eventModelId(),
			ctx.showOtherCategory(),
			ctx.categories(),
			selectedRootScopeIds != null ? selectedRootScopeIds : List.of(),
			criteria != null ? criteria : List.of()
		);
	}


	@Override
	public List<ChartDTO.ChartDataSeries> getWorkflowStatusData(final Map<String, Object> requestParams) {
		final var ctx = extractChartContext(requestParams);

		return workflowStatusChartService.generateValues(ctx.workflowId(), ctx.stateIds());
	}

	/**
	 * Safely casts the given object to a list of strings if possible.
	 * Filters out any elements that are not of type String and returns a new list containing only strings.
	 *
	 * @param obj the object to be cast, which may or may not be a list
	 * @return a list of strings if the object is a list containing string elements;
	 *         otherwise, an empty list
	 */
	private List<String> safeCastList(final Object obj) {
		if (obj instanceof List<?> list) {
			return list.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.collect(Collectors.toList());
		}
		return List.of();
	}

	/**
	 * Parses the given raw category object and converts it into a list of ChartDTO.Category objects.
	 * Handles different data formats, including a list of ChartDTO.Category objects or a map representation.
	 *
	 * @param rawCats the raw input object representing categories, which can be a list of ChartDTO.Category objects
	 *                or a list of maps that can be converted into ChartDTO.Category instances
	 * @return a list of ChartDTO.Category objects parsed and converted from the input object
	 */
	private List<ChartDTO.Category> parseCategories(final Object rawCats) {
		final List<ChartDTO.Category> cats = new ArrayList<>();
		if (rawCats instanceof List<?> catList) {
			for (var o : catList) {
				if (o instanceof ChartDTO.Category c) {
					cats.add(c);
				}
				else if (o instanceof Map<?, ?> m) {
					cats.add(objectMapper.convertValue(m, ChartDTO.Category.class));
				}
			}
		}
		return cats;
	}

	public record ChartRequestContext(
		String scopeModelId,
		String leafScopeModelId,
		String datasetModelId,
		String fieldModelId,
		String workflowId,
		String eventModelId,
		List<String> stateIds,
		List<ChartDTO.Category> categories,
		boolean showOtherCategory,
		boolean ignoreUserRights,
		boolean fillDataGaps
	) {}

	private ChartRequestContext extractChartContext(final Map<String, Object> requestParams) {
		final var scopeModelId     = (String) requestParams.get("scopeModelId");
		final var leafScopeModelId = (String) requestParams.get("leafScopeModelId");
		final var datasetModelId   = (String) requestParams.get("datasetModelId");
		final var fieldModelId     = (String) requestParams.get("fieldModelId");
		final var workflowId       = (String) requestParams.get("workflowId");
		final var eventModelId     = (String) requestParams.get("eventModelId");

		final var stateIds = safeCastList(requestParams.get("stateIds"));

		final var showOtherCategory = Boolean.parseBoolean(
			String.valueOf(requestParams.getOrDefault("showOtherCategory", "false"))
		);

		final var ignoreUserRights = Boolean.parseBoolean(
			String.valueOf(requestParams.getOrDefault("ignoreUserRights", "false"))
		);

		final var rawCats = requestParams.getOrDefault("categories", List.of());
		final var categories = parseCategories(rawCats);

		final var fillDataGaps = Boolean.TRUE.equals(requestParams.get("fillDataGaps"));

		return new ChartRequestContext(
			scopeModelId,
			leafScopeModelId,
			datasetModelId,
			fieldModelId,
			workflowId,
			eventModelId,
			stateIds,
			categories,
			showOtherCategory,
			ignoreUserRights,
			fillDataGaps
		);
	}

	//TODO: Make a function that returns the correct scopes of the current actor to use it inside the repository methods
	private List<Scope> determineRootScopes(final boolean ignoreUserRights) {
		return List.of();
	}
}
