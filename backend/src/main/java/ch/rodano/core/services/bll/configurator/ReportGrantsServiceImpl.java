package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.services.dao.configurator.ReportGrantsDAOService;

@Service
@Transactional
public class ReportGrantsServiceImpl implements ReportGrantsService {

	private final ReportGrantsDAOService reportGrantsDAOService;

	public ReportGrantsServiceImpl(final ReportGrantsDAOService reportGrantsDAOService) {
		this.reportGrantsDAOService = reportGrantsDAOService;
	}

	@Override
	public Map<UUID, List<UUID>> getReportGrants(final UUID projectId) {
		return reportGrantsDAOService.getReportGrants(projectId);
	}

	@Override
	public void saveReportGrants(final UUID projectId, final Map<UUID, List<UUID>> profileReportMap) {
		reportGrantsDAOService.saveReportGrants(projectId, profileReportMap);
	}
}
