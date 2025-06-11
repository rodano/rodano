package ch.rodano.core.model.graph.highchart.instance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.Hidden;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.chart.ChartRange;
import ch.rodano.configuration.model.chart.ChartType;
import ch.rodano.core.model.graph.highchart.HighChartSerie;

// This object should be hidden in the API until it is transformed to a proper DTO
@Hidden
public abstract class HighGraph {
	protected static final List<String> DEFAULT_COLORS = Arrays.asList("#ff9307", "#e3e3e3", "#66ccff", "#336699", "#db4c3c", "#bd8d46", "#7F8da9", "#fec514", "#daf0fd");

	protected final Chart configuration;
	protected final String language;

	protected final Map<String, String> values;
	protected List<HighChartSerie> series;

	public HighGraph(final Chart configuration, final String language, final Map<String, String> values) {
		this.configuration = configuration;
		this.language = language;
		this.values = values;
	}

	public Chart getConfiguration() {
		return configuration;
	}

	public abstract String getDefaultTitle();

	public String getTitle() {
		return configuration.getTitle() != null ? configuration.getLocalizedTitle(language) : getDefaultTitle();
	}

	public abstract String getType();

	public boolean hasRange() {
		return !getRanges().isEmpty();
	}

	public List<ChartRange> getRanges() {
		return configuration.getRanges();
	}

	public List<String> getColors() {
		if(CollectionUtils.isNotEmpty(configuration.getColors())) {
			return configuration.getColors();
		}
		return DEFAULT_COLORS;
	}

	public String getLegendXLabel() {
		return configuration.getLegendX() != null ? configuration.getLegendX().getLocalizedLabel(language) : "";
	}

	public String getLegendYLabel() {
		return configuration.getLegendY() != null ? configuration.getLegendY().getLocalizedLabel(language) : "";
	}

	public String getLegendXColor() {
		return getConfiguration().getLegendX() != null ? getConfiguration().getLegendX().getColor() : "#666666";
	}

	public String getLegendYColor() {
		return getConfiguration().getLegendY() != null ? getConfiguration().getLegendY().getColor() : "#666666";
	}

	public boolean getStatisticsUsePercentile() {
		return configuration.isUsePercentile();
	}

	public boolean getDisplayStatistics() {
		return ChartType.STATISTICS.equals(configuration.getType()) && configuration.isWithStatistics();
	}

	public boolean isStacked() {
		return false;
	}

	public boolean isInPercent() {
		return false;
	}

	public boolean isInverted() {
		return false;
	}

	public Map<String, Double> getMin() {
		return Collections.emptyMap();
	}

	public Map<String, Double> getMax() {
		return Collections.emptyMap();
	}

	public Map<String, Double> getPercentile5() {
		return Collections.emptyMap();
	}

	public Map<String, Double> getPercentile95() {
		return Collections.emptyMap();
	}

	public Map<String, Double> getCount() {
		return Collections.emptyMap();
	}

	public Map<String, Double> getMean() {
		return Collections.emptyMap();
	}

	public Map<String, Double> getSd() {
		return Collections.emptyMap();
	}

	public Map<String, Double> getMedian() {
		return Collections.emptyMap();
	}

	public void setSeries(final List<HighChartSerie> series) {
		this.series = series;
	}

	public List<HighChartSerie> getSeries() {
		return series;
	}

	// key is the scope code, value is the buffer splitted by string
	@JsonIgnore
	public Map<String, String> getValues() {
		return values;
	}

	public boolean getShowMarkers() {
		return false;
	}
}
