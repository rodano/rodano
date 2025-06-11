package ch.rodano.core.services.dao.chart;

import java.util.List;

public interface ChartService {
	ChartDTO getChartById(String chartId);
	List<ChartDTO> listCharts();
	ChartDTO createChart(ChartDTO chart);
	ChartDTO updateChart(ChartDTO chart);
	void deleteChart(String chartId);
}
