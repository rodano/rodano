package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.CronDTO;

public interface CronService {

	List<CronDTO> getCrons(UUID projectId);

	CronDTO getCron(UUID projectId, UUID cronId);

	CronDTO createCron(UUID projectId, CronDTO dto);

	CronDTO updateCron(UUID projectId, UUID cronId, CronDTO dto);

	void deleteCron(UUID projectId, UUID cronId);
}
