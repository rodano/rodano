package ch.rodano.core.services.dao.chart;

import java.util.List;

public interface ChartRepository {
	ChartDTO findByChartId(String chartId);
	List<ChartDTO> findAll();
	ChartDTO save(ChartDTO chart);
	ChartDTO update(ChartDTO chart);
	void delete(String chartId);
}
