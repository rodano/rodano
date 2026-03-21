package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TimelineGraphGrantsService {

	Map<UUID, List<UUID>> getTimelineGraphGrants(UUID projectId);

	void saveTimelineGraphGrants(UUID projectId, Map<UUID, List<UUID>> profileTimelineGraphMap);
}
