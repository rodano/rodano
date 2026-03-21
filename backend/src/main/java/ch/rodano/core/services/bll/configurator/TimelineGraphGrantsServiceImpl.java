package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.services.dao.configurator.TimelineGraphGrantsDAOService;

@Service
@Transactional
public class TimelineGraphGrantsServiceImpl implements TimelineGraphGrantsService {

	private final TimelineGraphGrantsDAOService timelineGraphGrantsDAOService;

	public TimelineGraphGrantsServiceImpl(final TimelineGraphGrantsDAOService timelineGraphGrantsDAOService) {
		this.timelineGraphGrantsDAOService = timelineGraphGrantsDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, List<UUID>> getTimelineGraphGrants(final UUID projectId) {
		return timelineGraphGrantsDAOService.getTimelineGraphGrants(projectId);
	}

	@Override
	public void saveTimelineGraphGrants(final UUID projectId, final Map<UUID, List<UUID>> profileTimelineGraphMap) {
		timelineGraphGrantsDAOService.saveTimelineGraphGrants(projectId, profileTimelineGraphMap);
	}
}
