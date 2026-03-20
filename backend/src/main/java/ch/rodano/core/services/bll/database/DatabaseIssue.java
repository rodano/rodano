package ch.rodano.core.services.bll.database;

public record DatabaseIssue(
	String entity,
	Long pk,
	String error,
	DatabaseIssueStatus status
) {}
