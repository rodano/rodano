package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ReportDTO;


public interface ReportDAOService {

	List<ReportDTO> getReports(UUID projectId);

	ReportDTO getReport(UUID projectId, UUID reportId);

	ReportDTO createReport(UUID projectId, ReportDTO report);

	ReportDTO updateReport(UUID projectId, UUID reportId, ReportDTO report);

	void deleteReport(UUID projectId, UUID reportId);
}
