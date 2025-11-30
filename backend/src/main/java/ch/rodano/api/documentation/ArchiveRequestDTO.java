package ch.rodano.api.documentation;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

public record ArchiveRequestDTO(
	@NotEmpty UUID scopeModelId,
	@NotEmpty List<Long> scopePks,
	boolean auditTrails) {
}
