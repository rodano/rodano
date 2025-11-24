
package ch.rodano.core.services.bll.widget.chart;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.rodano.core.model.chart.ChartDatasetPoint;

public class ChartHelpers {

	public static int DEFAULT_MAX_POINTS = 50;

	public static SortedMap<ZonedDateTime, Integer> cumulate(final SortedMap<ZonedDateTime, Integer> input) {
		final var result = new TreeMap<ZonedDateTime, Integer>();
		var total = 0;
		for(final var entry : input.entrySet()) {
			total += entry.getValue();
			result.put(entry.getKey(), total);
		}
		return result;
	}

	public static List<ChartDatasetPoint<ZonedDateTime, Integer>> processTimepoints(final SortedMap<ZonedDateTime, Integer> input) {
		final var timepoints = new ArrayList<ChartDatasetPoint<ZonedDateTime, Integer>>();

		if(!input.isEmpty()) {
			//add the 0 point one day before the first result
			//otherwise the chart will start at 1
			final var firstKey = input.firstKey();
			timepoints.add(new ChartDatasetPoint<ZonedDateTime, Integer>(firstKey.minusDays(1), 0));

			for(final var entry : input.entrySet()) {
				timepoints.add(new ChartDatasetPoint<ZonedDateTime, Integer>(entry.getKey(), entry.getValue()));
			}
		}

		return timepoints;
	}

	//TODO downsampling with consideration of dates would be much more interesting
	public static SortedMap<ZonedDateTime, Integer> downsample(final SortedMap<ZonedDateTime, Integer> input, final int maxPoints) {
		final int inputSize = input.size();
		if(inputSize <= maxPoints) {
			return input;
		}

		//calculate step between each point
		final double step = (double) inputSize / maxPoints;

		final var existingEntries = new ArrayList<>(input.entrySet());
		final var result = new TreeMap<ZonedDateTime, Integer>();

		for(int i = 0; i < maxPoints; i++) {
			int index = (int) Math.round(i * step);
			if(index >= inputSize) {
				index = inputSize - 1;
			}
			//copy the point at this index
			final var entry = existingEntries.get(index);
			result.put(entry.getKey(), entry.getValue());
		}

		//be sure to add the final point
		if(!input.isEmpty()) {
			final var lastEntry = input.lastEntry();
			result.put(lastEntry.getKey(), lastEntry.getValue());
		}
		return result;
	}

}
