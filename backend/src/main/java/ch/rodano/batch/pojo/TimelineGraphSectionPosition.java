package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineGraphSectionPosition {

	private Integer start;
	private Integer stop;

	public Integer getStart() {
		return start;
	}

	public void setStart(final Integer start) {
		this.start = start;
	}

	public Integer getStop() {
		return stop;
	}

	public void setStop(final Integer stop) {
		this.stop = stop;
	}

	@Override
	public String toString() {
		return "TimelineGraphSectionPosition{" +
			"start=" + start +
			", stop=" + stop +
			'}';
	}
}
