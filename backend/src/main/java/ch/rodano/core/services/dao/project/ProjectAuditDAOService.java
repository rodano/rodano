package ch.rodano.core.services.dao.project;

import java.util.UUID;

import ch.rodano.core.model.audit.DatabaseActionContext;

public interface ProjectAuditDAOService {

	/**
	 * Create a project audit to track status changes on projects
	 */
	void createAudit(UUID projectId, DatabaseActionContext context);
}
