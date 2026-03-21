package ch.rodano.api.config;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record WorkflowRightsDTO(
	Map<UUID, List<UUID>> workflowRights,
	Map<UUID, List<UUID>> actionRights
) {
}
