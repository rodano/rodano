package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReportGrantsService {

	Map<UUID, List<UUID>> getReportGrants(UUID projectId);

	void saveReportGrants(UUID projectId, Map<UUID, List<UUID>> profileReportMap);
}
