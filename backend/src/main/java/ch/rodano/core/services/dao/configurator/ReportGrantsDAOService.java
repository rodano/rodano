package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReportGrantsDAOService {

	Map<UUID, List<UUID>> getReportGrants(UUID projectId);

	void saveReportGrants(UUID projectId, Map<UUID, List<UUID>> profileReportMap);
}
