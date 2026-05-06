package ch.rodano.core.services.bll.database;

public record ConfigurationInconsistency(
	InconsistentEntity entity,
	String modelId,
	Long pk,
	ConfigurationInconsistencyType type,
	String missingEntityId,
	InconsistencyStatus status
) {}
