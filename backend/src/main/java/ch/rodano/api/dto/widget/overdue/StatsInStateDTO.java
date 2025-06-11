package ch.rodano.api.dto.widget.overdue;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import jakarta.validation.constraints.NotNull;

public class StatsInStateDTO {
	@NotNull private int total;
	@NotNull private int cumulative;
	@NotNull private double percent;
	@NotNull private double percentCumulative;
	@NotNull private Set<Long> leafScopes;

	private static DecimalFormat FORMATTER;

	/**
	 * Constructor
	 */
	public StatsInStateDTO() {
		leafScopes = new TreeSet<>();
	}

	private NumberFormat getFormatter() {
		if(FORMATTER == null) {
			FORMATTER = (DecimalFormat) NumberFormat.getInstance(Locale.US);
			FORMATTER.applyPattern("0.0");
		}

		return FORMATTER;
	}

	public int getTotal() {
		return total;
	}

	public int getCumulative() {
		return cumulative;
	}

	public Double getPercent() {
		return percent;
	}

	public String getPercentFormatted() {
		return getFormatter().format(percent);
	}

	public Double getPercentCumulative() {
		return percentCumulative;
	}

	public String getPercentCumulativeFormatted() {
		return getFormatter().format(percentCumulative);
	}

	public void setTotal(final int total) {
		this.total = total;
	}

	public void setCumulative(final int cumulative) {
		this.cumulative = cumulative;
	}

	public void setPercent(final double percent) {
		this.percent = percent;
	}

	public void setPercentCumulative(final double percentCumulative) {
		this.percentCumulative = percentCumulative;
	}

	public Set<Long> getLeafScopes() {
		return leafScopes;
	}

	public void setLeafScopes(final Set<Long> leafScopes) {
		this.leafScopes = leafScopes;
	}
}
