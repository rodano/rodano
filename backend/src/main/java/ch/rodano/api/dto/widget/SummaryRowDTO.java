package ch.rodano.api.dto.widget;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ch.rodano.api.scope.ScopeTinyDTO;

public record SummaryRowDTO(
	@NotNull ScopeTinyDTO scope,
	@NotEmpty Map<String, Long> values
) {

	@NotNull
	public Long getTotal() {
		return values.values().stream().reduce(0L, Long::sum);
	}
}
