package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.ReportDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.ReportDAOService;

@Service
@Transactional
public class ConfiguratorReportServiceImpl implements ConfiguratorReportService {

	private final ReportDAOService reportDAOService;

	public ConfiguratorReportServiceImpl(final ReportDAOService reportDAOService) {
		this.reportDAOService = reportDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReportDTO> getReports(final UUID projectId) {
		return reportDAOService.getReports(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public ReportDTO getReport(final UUID projectId, final UUID reportId) {
		final var report = reportDAOService.getReport(projectId, reportId);
		if(report == null) {
			throw new NotFoundException("Report not found: " + reportId);
		}
		return report;
	}

	@Override
	public ReportDTO createReport(final UUID projectId, final ReportDTO report) {
		return reportDAOService.createReport(projectId, report);
	}

	@Override
	public ReportDTO updateReport(final UUID projectId, final UUID reportId, final ReportDTO report) {
		final var existing = reportDAOService.getReport(projectId, reportId);
		if(existing == null) {
			throw new NotFoundException("Report not found: " + reportId);
		}
		return reportDAOService.updateReport(projectId, reportId, report);
	}

	@Override
	public void deleteReport(final UUID projectId, final UUID reportId) {
		final var existing = reportDAOService.getReport(projectId, reportId);
		if(existing == null) {
			throw new NotFoundException("Report not found: " + reportId);
		}
		reportDAOService.deleteReport(projectId, reportId);
	}
}
