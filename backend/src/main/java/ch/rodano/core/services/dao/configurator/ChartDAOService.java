package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ChartModelDTO;


public interface ChartDAOService {
	
	List<ChartModelDTO> getCharts(UUID projectId, String view);

	List<ChartModelDTO> getChartsSummary(UUID projectId);

	List<ChartModelDTO> getChartsFull(UUID projectId);

	ChartModelDTO getChart(UUID projectId, UUID chartId);

	ChartModelDTO createChart(UUID projectId, ChartModelDTO chart);

	ChartModelDTO updateChart(UUID projectId, UUID chartId, ChartModelDTO chart);

	void deleteChart(UUID projectId, UUID chartId);
}
