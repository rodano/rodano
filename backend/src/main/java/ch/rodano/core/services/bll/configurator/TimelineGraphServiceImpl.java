package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.TimelineGraphDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.TimelineGraphDAOService;

@Service
@Transactional
public class TimelineGraphServiceImpl implements TimelineGraphService {

	private final TimelineGraphDAOService timelineGraphDAOService;

	public TimelineGraphServiceImpl(final TimelineGraphDAOService timelineGraphDAOService) {
		this.timelineGraphDAOService = timelineGraphDAOService;
	}

	@Override
	public List<TimelineGraphDTO> getTimelineGraphs(final UUID projectId) {
		return timelineGraphDAOService.getTimelineGraphs(projectId);
	}

	@Override
	public TimelineGraphDTO getTimelineGraph(final UUID projectId, final UUID timelineGraphId) {
		final var timelineGraph = timelineGraphDAOService.getTimelineGraph(projectId, timelineGraphId);
		if(timelineGraph == null) {
			throw new NotFoundException("Timeline graph not found: " + timelineGraphId);
		}
		return timelineGraph;
	}

	@Override
	public TimelineGraphDTO createTimelineGraph(final UUID projectId, final TimelineGraphDTO timelineGraph) {
		return timelineGraphDAOService.createTimelineGraph(projectId, timelineGraph);
	}

	@Override
	public TimelineGraphDTO updateTimelineGraph(final UUID projectId, final UUID timelineGraphId, final TimelineGraphDTO timelineGraph) {
		final var existing = timelineGraphDAOService.getTimelineGraph(projectId, timelineGraphId);
		if(existing == null) {
			throw new NotFoundException("Timeline graph not found:  " + timelineGraphId);
		}
		return timelineGraphDAOService.updateTimelineGraph(projectId, timelineGraphId, timelineGraph);
	}

	@Override
	public void deleteTimelineGraph(final UUID projectId, final UUID timelineGraphId) {
		final var existing = timelineGraphDAOService.getTimelineGraph(projectId, timelineGraphId);
		if(existing == null) {
			throw new NotFoundException("Timeline graph not found:  " + timelineGraphId);
		}
		timelineGraphDAOService.deleteTimelineGraph(projectId, timelineGraphId);
	}
}
