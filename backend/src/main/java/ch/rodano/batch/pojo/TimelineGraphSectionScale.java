package ch.rodano.batch.pojo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.rodano.core.model.jooq.enums.TimelineGraphSectionScalePosition;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineGraphSectionScale {

	private BigDecimal min;
	private BigDecimal max;
	private Integer decimal;
	private BigDecimal markInterval;
	private BigDecimal labelInterval;
	private TimelineGraphSectionScalePosition position;

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(final BigDecimal min) {
		this.min = min;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(final BigDecimal max) {
		this.max = max;
	}

	public Integer getDecimal() {
		return decimal;
	}

	public void setDecimal(final Integer decimal) {
		this.decimal = decimal;
	}

	public BigDecimal getMarkInterval() {
		return markInterval;
	}

	public void setMarkInterval(final BigDecimal markInterval) {
		this.markInterval = markInterval;
	}

	public BigDecimal getLabelInterval() {
		return labelInterval;
	}

	public void setLabelInterval(final BigDecimal labelInterval) {
		this.labelInterval = labelInterval;
	}

	public TimelineGraphSectionScalePosition getPosition() {
		return position;
	}

	public void setPosition(final TimelineGraphSectionScalePosition position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return "TimelineGraphSectionScale{" +
			"min=" + min +
			", max=" + max +
			", decimal=" + decimal +
			", markInterval=" + markInterval +
			", labelInterval=" + labelInterval +
			", position='" + position + '\'' +
			'}';
	}
}
