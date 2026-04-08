package ch.rodano.core.services.bll.database;

import java.util.List;

public record DatabaseIssueGroup(
	DatabaseIssueEntity entity,
	String modelId,
	List<Long> pks,
	DatabaseIssueType type,
	String missingEntityId,
	DatabaseIssueStatus status
) {}
