package ch.rodano.core.services.bll.database;

public record DenormalizationInconsistency(
	InconsistentEntity entity,
	String modelId,
	Long pk,
	InconsistencyStatus status
) {}
