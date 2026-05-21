package ch.rodano.core.services.bll.database;

import java.util.List;

public record ConfigurationInconsistencyGroup(
	InconsistentEntity entity,
	String modelId,
	List<Long> pks,
	ConfigurationInconsistencyType type,
	String missingEntityId,
	InconsistencyStatus status
) {
}
