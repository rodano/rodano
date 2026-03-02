package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.ChartModelDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.ChartDAOService;

@Service
@Transactional
public class ChartServiceImpl implements ChartService {

	private final ChartDAOService chartDAOService;

	public ChartServiceImpl(final ChartDAOService chartDAOService) {
		this.chartDAOService = chartDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ChartModelDTO> getCharts(final UUID projectId, final String view) {
		return chartDAOService.getCharts(projectId, view);
	}

	@Override
	@Transactional(readOnly = true)
	public ChartModelDTO getChart(final UUID projectId, final UUID chartId) {
		final var chart = chartDAOService.getChart(projectId, chartId);
		if(chart == null) {
			throw new NotFoundException("Chart not found: " + chartId);
		}
		return chart;
	}

	@Override
	public ChartModelDTO createChart(final UUID projectId, final ChartModelDTO chart) {
		return chartDAOService.createChart(projectId, chart);
	}

	@Override
	public ChartModelDTO updateChart(final UUID projectId, final UUID chartId, final ChartModelDTO chart) {
		final var existing = chartDAOService.getChart(projectId, chartId);
		if(existing == null) {
			throw new NotFoundException("Chart not found: " + chartId);
		}
		return chartDAOService.updateChart(projectId, chartId, chart);
	}

	@Override
	public void deleteChart(final UUID projectId, final UUID chartId) {
		final var existing = chartDAOService.getChart(projectId, chartId);
		if(existing == null) {
			throw new NotFoundException("Chart not found: " + chartId);
		}
		chartDAOService.deleteChart(projectId, chartId);
	}
}
