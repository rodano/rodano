package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TimelineGraphGrantsDAOService {

	Map<UUID, List<UUID>> getTimelineGraphGrants(UUID projectId);

	void saveTimelineGraphGrants(UUID projectId, Map<UUID, List<UUID>> profileTimelineGraphMap);
}
