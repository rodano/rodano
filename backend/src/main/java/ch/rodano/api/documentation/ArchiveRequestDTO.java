package ch.rodano.api.documentation;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record ArchiveRequestDTO(
	@NotEmpty String scopeModelId,
	@NotEmpty List<Long> scopePks,
	boolean auditTrails) {
}
