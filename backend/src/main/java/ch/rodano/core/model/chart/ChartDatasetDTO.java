package ch.rodano.core.model.chart;

import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChartDatasetDTO<X, Y>(
	@NotBlank String label,
	@NotNull List<ChartDatasetPoint<X, Y>> data,
	Statistics statistics
) {
	public ChartDatasetDTO(final String label, final List<ChartDatasetPoint<X, Y>> data) {
		this(label, data, null);
	}

	public Optional<Y> getValue(final X key) {
		return data.stream().filter(p -> p.x().equals(key)).map(ChartDatasetPoint::y).findFirst();
	}
}
