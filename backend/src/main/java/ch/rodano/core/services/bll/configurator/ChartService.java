package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ChartModelDTO;

public interface ChartService {

	List<ChartModelDTO> getCharts(UUID projectId, String view);

	default List<ChartModelDTO> getCharts(final UUID projectId) {
		return getCharts(projectId, "summary");
	}

	ChartModelDTO getChart(UUID projectId, UUID chartId);

	ChartModelDTO createChart(UUID projectId, ChartModelDTO chart);

	ChartModelDTO updateChart(UUID projectId, UUID chartId, ChartModelDTO chart);

	void deleteChart(UUID projectId, UUID chartId);
}
