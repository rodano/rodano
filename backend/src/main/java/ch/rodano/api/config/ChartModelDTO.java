package ch.rodano.api.config;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.chart.ChartType;

public class ChartModelDTO {
	@NotNull
	private UUID chartId;
	@NotNull
	private String id;
	@NotNull
	private ChartType type;

	@NotNull
	private final String title;

	private final String legendX;
	private final String legendY;

	@NotNull
	private List<String> colors;

	@NotNull
	private final boolean withStatistics;

	public ChartModelDTO(final Chart chart, final String[] languages) {
		this.chartId = chart.getChartId();
		this.id = chart.getId();
		this.type = chart.getType();
		this.title = chart.getLocalizedTitle(languages);
		this.legendX = chart.getLocalizedLegendX(languages);
		this.legendY = chart.getLocalizedLegendY(languages);
		this.colors = chart.getColors();
		this.withStatistics = chart.isWithStatistics();
	}

	public UUID getChartId() {
		return chartId;
	}

	public void setChartId(final UUID chartId) {
		this.chartId = chartId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public ChartType getType() {
		return type;
	}

	public void setType(final ChartType type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public String getLegendX() {
		return legendX;
	}

	public String getLegendY() {
		return legendY;
	}

	public List<String> getColors() {
		return colors;
	}

	public void setColors(final List<String> colors) {
		this.colors = colors;
	}

	public boolean isWithStatistics() {
		return withStatistics;
	}
}
