package ch.rodano.core.services.bll.database;

import java.util.List;

public record DenormalizationInconsistencyGroup(
	InconsistentEntity entity,
	String modelId,
	List<Long> pks,
	InconsistencyStatus status
) {}
