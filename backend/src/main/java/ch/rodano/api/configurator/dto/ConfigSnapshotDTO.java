package ch.rodano.api.configurator.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public record ConfigSnapshotDTO(
	List<SnapshotEntry> snapshots,
	Integer currentIndex
) {

	public record SnapshotEntry(
		ZonedDateTime timestamp,
		String summary,
		Map<String, String> tables
	) {
	}
}
