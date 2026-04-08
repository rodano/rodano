package ch.rodano.core.services.bll.database;

public record DatabaseIssue(
	DatabaseIssueEntity entity,
	String modelId,
	Long pk,
	DatabaseIssueType type,
	String error,
	DatabaseIssueStatus status
) {}
