package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.TimelineGraphDTO;

public interface TimelineGraphService {

	List<TimelineGraphDTO> getTimelineGraphs(UUID projectId);

	TimelineGraphDTO getTimelineGraph(UUID projectId, UUID timelineGraphId);

	TimelineGraphDTO createTimelineGraph(UUID projectId, TimelineGraphDTO timelineGraph);

	TimelineGraphDTO updateTimelineGraph(UUID projectId, UUID timelineGraphId, TimelineGraphDTO timelineGraph);

	void deleteTimelineGraph(UUID projectId, UUID timelineGraphId);
}
