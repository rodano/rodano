package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.TimelineGraphSectionDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.TimelineGraphSectionDAOService;

@Service
@Transactional
public class TimelineGraphSectionServiceImpl implements TimelineGraphSectionService {

	private final TimelineGraphSectionDAOService timelineGraphSectionDAOService;

	public TimelineGraphSectionServiceImpl(final TimelineGraphSectionDAOService timelineGraphSectionDAOService) {
		this.timelineGraphSectionDAOService = timelineGraphSectionDAOService;
	}

	@Override
	public List<TimelineGraphSectionDTO> getSections(final UUID projectId, final UUID timelineGraphId) {
		return timelineGraphSectionDAOService.getSections(projectId, timelineGraphId);
	}

	@Override
	public TimelineGraphSectionDTO getSection(final UUID projectId, final UUID timelineGraphId, final UUID sectionId) {
		final var timelineGraphSection = timelineGraphSectionDAOService.getSection(projectId, timelineGraphId, sectionId);
		if(timelineGraphSection == null) {
			throw new NotFoundException("Timeline graph section not found: " + sectionId);
		}
		return timelineGraphSection;
	}

	@Override
	public TimelineGraphSectionDTO createSection(final UUID projectId, final UUID timelineGraphId, final TimelineGraphSectionDTO dto) {
		return timelineGraphSectionDAOService.createSection(projectId, timelineGraphId, dto);
	}

	@Override
	public TimelineGraphSectionDTO updateSection(final UUID projectId, final UUID timelineGraphId, final UUID sectionId, final TimelineGraphSectionDTO dto) {
		final var existing = timelineGraphSectionDAOService.getSection(projectId, timelineGraphId, sectionId);
		if(existing == null) {
			throw new NotFoundException("Timeline graph section not found:  " + sectionId);
		}
		return timelineGraphSectionDAOService.updateSection(projectId, timelineGraphId, sectionId, dto);
	}

	@Override
	public void deleteSection(final UUID projectId, final UUID timelineGraphId, final UUID sectionId) {
		final var existing = timelineGraphSectionDAOService.getSection(projectId, timelineGraphId, sectionId);
		if(existing == null) {
			throw new NotFoundException("Timeline graph section not found:  " + sectionId);
		}
		timelineGraphSectionDAOService.deleteSection(projectId, timelineGraphId, sectionId);
	}
}
