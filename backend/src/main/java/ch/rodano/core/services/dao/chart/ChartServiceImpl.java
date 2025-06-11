package ch.rodano.core.services.dao.chart;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.services.dao.chart.data.ChartDataRepositoryImpl;

@Service
public class ChartServiceImpl implements ChartService {

	private final ChartRepositoryImpl chartRepositoryImpl;
	private final ChartDataRepositoryImpl chartDataRepository;
	private static final Logger LOGGER = LoggerFactory.getLogger(ChartServiceImpl.class);
	private final ObjectMapper objectMapper;

	public ChartServiceImpl(
		final ChartRepositoryImpl chartRepositoryImpl,
		final ChartDataRepositoryImpl chartDataRepository,
		final ObjectMapper objectMapper
	) {
		this.chartRepositoryImpl = chartRepositoryImpl;
		this.chartDataRepository = chartDataRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	public ChartDTO getChartById(final String chartId) {

		// Getting the configuration data of the chart
		final var chart = chartRepositoryImpl.findByChartId(chartId);
		if(chart == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chart with ID '" + chartId + "' not found");
		}

		// Getting the actual data of the chart
		final var params = objectMapper.convertValue(
		    chart.getRequestParams(),
		    new TypeReference<Map<String,Object>>() {}
		);
		params.put("chartType", chart.getChartType());

		// Fetch dynamic data as ChartDTO.Data
		final var data = this.getDynamicData(params);

		// Set data into chart DTO
		chart.setData(data);

		return chart;
	}

	@Override
	public List<ChartDTO> listCharts() {
		return chartRepositoryImpl.findAll();
	}

	@Override
	public ChartDTO createChart(final ChartDTO chart) {
		return chartRepositoryImpl.save(chart);
	}

	@Override
	public ChartDTO updateChart(final ChartDTO chart) {
		return chartRepositoryImpl.update(chart);
	}

	@Override
	public void deleteChart(final String chartId) {
		chartRepositoryImpl.delete(chartId);
	}

	public ChartDTO.Data getDynamicData(final Map<String, Object> requestParams) {
		return getDynamicData(requestParams, List.of(), List.of());
	}

	public ChartDTO.Data getDynamicData(
		final Map<String, Object> requestParams,
		final List<String> selectedRootScopeIds,
		final List<FieldModelCriterion> criteria
	) {// Try to manually parse categories if it's a JSON string
		final var rawCategories = requestParams.get("categories");
        if (rawCategories instanceof String jsonCats) {
            try {
                final List<Map<String, Object>> categoriesList = objectMapper.readValue(
                    jsonCats, new TypeReference<>() {}
                );
                requestParams.put("categories", categoriesList);
            }
			catch (Exception e) {
                LOGGER.error("Failed to parse categories param: {}", jsonCats, e);
                throw new IllegalArgumentException("Malformed categories param", e);
            }
        }

		// Normalize stateIds to always be List<String>
		final var rawStates = requestParams.get("stateIds");
		switch(rawStates) {
			case String singleState -> requestParams.put("stateIds", List.of(singleState));
			case String[] stateArray -> requestParams.put("stateIds", Arrays.asList(stateArray));
			case Collection<?> col -> requestParams.put("stateIds", col.stream().map(Object::toString).toList());
			case null -> requestParams.put("stateIds", List.of()); // Default empty list
			default -> {
			}
		}

		final var chartTypeString = requestParams.get("chartType");
		final var chartType = ChartType.valueOf(chartTypeString.toString());
		final List<ChartDTO.ChartDataSeries> series;

		switch(chartType) {
			case ENROLLMENT_STATUS -> series = chartDataRepository.getEnrollmentStatusData(requestParams);
			case ENROLLMENT_BY_SCOPE -> series = chartDataRepository.getEnrollmentByScopeData(requestParams);
			case STATISTICS -> series = chartDataRepository.getStatisticsData(requestParams, selectedRootScopeIds, criteria);
			case WORKFLOW_STATUS -> series = chartDataRepository.getWorkflowStatusData(requestParams);
			default -> throw new IllegalArgumentException("Unsupported chart type: " + chartTypeString);
		}

		final var data = new ChartDTO.Data();
		data.setSeries(series);
		return data;
	}
}
