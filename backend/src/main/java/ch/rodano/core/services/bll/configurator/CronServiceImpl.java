package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.CronDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.CronDAOService;

@Service
@Transactional
public class CronServiceImpl implements CronService {

	private final CronDAOService cronDAOService;

	public CronServiceImpl(final CronDAOService cronDAOService) {
		this.cronDAOService = cronDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CronDTO> getCrons(final UUID projectId) {
		return cronDAOService.getCrons(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public CronDTO getCron(final UUID projectId, final UUID cronId) {
		final var cron = cronDAOService.getCron(projectId, cronId);
		if(cron == null) {
			throw new NotFoundException("Cron not found: " + cronId);
		}
		return cron;
	}

	@Override
	public CronDTO createCron(final UUID projectId, final CronDTO dto) {
		return cronDAOService.createCron(projectId, dto);
	}

	@Override
	public CronDTO updateCron(final UUID projectId, final UUID cronId, final CronDTO dto) {
		final var existing = cronDAOService.getCron(projectId, cronId);
		if(existing == null) {
			throw new NotFoundException("Cron not found: " + cronId);
		}
		return cronDAOService.updateCron(projectId, cronId, dto);
	}

	@Override
	public void deleteCron(final UUID projectId, final UUID cronId) {
		final var existing = cronDAOService.getCron(projectId, cronId);
		if(existing == null) {
			throw new NotFoundException("Cron not found: " + cronId);
		}
		cronDAOService.deleteCron(projectId, cronId);
	}
}
