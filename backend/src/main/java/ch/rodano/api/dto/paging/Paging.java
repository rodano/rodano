package ch.rodano.api.dto.paging;

import jakarta.validation.constraints.NotNull;

public record Paging(
	@NotNull int pageSize,
	@NotNull int pageIndex,
	@NotNull int total
) {
	//nothing to do here
}
