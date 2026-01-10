package ch.rodano.api.configurator;

import java.time.ZonedDateTime;
import java.util.List;

public record ConfigSnapshotDTO(
	List<SnapshotEntry> snapshots,
	Integer currentIndex
) {

	public record SnapshotEntry(
		ZonedDateTime timestamp,
		String summary,
		ConfiguratorProjectDTO data
	) {
	}
}
