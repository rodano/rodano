package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.TimelineGraphSectionDTO;

public interface TimelineGraphSectionDAOService {

	List<TimelineGraphSectionDTO> getSections(UUID projectId, UUID timelineGraphId);

	TimelineGraphSectionDTO getSection(UUID projectId, UUID timelineGraphId, UUID sectionId);

	TimelineGraphSectionDTO createSection(UUID projectId, UUID timelineGraphId, TimelineGraphSectionDTO dto);

	TimelineGraphSectionDTO updateSection(UUID projectId, UUID timelineGraphId, UUID sectionId, TimelineGraphSectionDTO dto);

	void deleteSection(UUID projectId, UUID timelineGraphId, UUID sectionId);
}
