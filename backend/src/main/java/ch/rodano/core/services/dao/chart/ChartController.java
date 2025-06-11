package ch.rodano.core.services.dao.chart;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.core.model.scope.FieldModelCriterion;

@Transactional(readOnly = true)
@RestController
@RequestMapping("/charts")
public class ChartController {

	private final ChartServiceImpl chartService;
	private final Logger logger = LoggerFactory.getLogger(ChartController.class);

	public ChartController(final ChartServiceImpl chartService) {
		this.chartService = chartService;
	}

	/** GET /api/charts/{chartId} */
	@GetMapping("/{chartId}")
	public ResponseEntity<ChartDTO> getChart(final @PathVariable String chartId) {
		return ResponseEntity.ok(chartService.getChartById(chartId));
	}

	/** GET /api/charts */
	@GetMapping
	public ResponseEntity<List<ChartDTO>> listCharts() {
		return ResponseEntity.ok(chartService.listCharts());
	}

	/** POST /api/charts */
	@Transactional
	@PostMapping
	public ResponseEntity<ChartDTO> createChart(final @RequestBody ChartDTO chartDTO) {
		logger.info("Create Chart request with params: {}", chartDTO);
		return ResponseEntity.ok(chartService.createChart(chartDTO));
	}

	/** PATCH /api/charts/{chartId} */
	@Transactional
	@PatchMapping("/{chartId}")
	public ResponseEntity<ChartDTO> updateChart(final @RequestBody ChartDTO chartDTO) {
		return ResponseEntity.ok(chartService.updateChart(chartDTO));
	}

	/** DELETE /api/charts/{chartId} */
	@Transactional
	@DeleteMapping("/{chartId}")
	public ResponseEntity<Void> deleteChart(final @PathVariable String chartId) {
		chartService.deleteChart(chartId);
		return ResponseEntity.noContent().build();
	}

	/** GET /api/charts/data */
	@GetMapping("/data")
	public ResponseEntity<ChartDTO.Data> getData(final @RequestParam MultiValueMap<String, String> params) {
		logger.info("Chart data request with params: {}", params);
		final Map<String, Object> flatParams = new HashMap<>();
		params.forEach((key, values) -> flatParams.put(key, values.size() == 1 ? values.getFirst() : values));
		return ResponseEntity.ok(chartService.getDynamicData(flatParams));
	}

	/** GET /api/charts/benchmark-data */
	@GetMapping("/benchmark-data")
	public ResponseEntity<ChartDTO.Data> getBenchmarkData(
		final @RequestParam Map<String, Object> params,
		final @RequestParam(name = "selectedRootScopeIds") List<String> selectedRootScopeIds
	) {
		logger.info("Benchmark data request with params: {}", params);
		logger.info("Selected root scope IDs: {}", selectedRootScopeIds);

		final var criteria = parseCriteriaFromParams(params);
		return ResponseEntity.ok(chartService.getDynamicData(params, selectedRootScopeIds, criteria));
	}

	private List<FieldModelCriterion> parseCriteriaFromParams(final Map<String, Object> params) {
		final Map<Integer, FieldModelCriterion.Builder> temp = new LinkedHashMap<>();

		for (var entry : params.entrySet()) {
			final var key = entry.getKey();
			if (key.startsWith("criteria[") && key.contains("].")) {
				// Extract index and field name
				final var start = key.indexOf('[') + 1;
				final var end = key.indexOf(']');
				final var index = Integer.parseInt(key.substring(start, end));
				final var field = key.substring(key.indexOf("].") + 2);

				// Ensure builder exists
				temp.putIfAbsent(index, new FieldModelCriterion.Builder());
				final var builder = temp.get(index);

				final var value = String.valueOf(entry.getValue());

				switch (field) {
					case "datasetModelId" -> builder.datasetModelId(value);
					case "fieldModelId" -> builder.fieldModelId(value);
					case "value" -> builder.value(value);
					case "operator" -> builder.operator(Operator.valueOf(value));
					default -> logger.warn("Unknown field '{}' in chart criteria", field);
				}
			}
		}

		// Build all valid criteria
		return temp.values().stream()
			.map(FieldModelCriterion.Builder::build)
			.filter(FieldModelCriterion::isValid)
			.toList();
	}

}
