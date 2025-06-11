package ch.rodano.core.services.dao.workflow;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.function.Function;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.workflow.WorkflowStatusSearch;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.WorkflowStatusAuditTrail;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.workflow.WorkflowStatus;

public interface WorkflowStatusDAOService {

	WorkflowStatus getWorkflowStatusByPk(Long pk);

	List<WorkflowStatus> getWorkflowStatusesByScopePk(Long scopePk);

	List<WorkflowStatus> getWorkflowStatusesByScopePk(Long scopePk, String workflowId);

	List<WorkflowStatus> getWorkflowStatusesByScopePks(Collection<Long> scopePks);

	List<WorkflowStatus> getWorkflowStatusesByEventPk(Long eventPk);

	List<WorkflowStatus> getWorkflowStatusesByEventPk(Long eventPk, String workflowId);

	List<WorkflowStatus> getWorkflowStatusesByEventPks(Collection<Long> eventPks);

	List<WorkflowStatus> getWorkflowStatusesByFormPk(Long formPk);

	List<WorkflowStatus> getWorkflowStatusesByFormPk(Long formPk, String workflowId);

	List<WorkflowStatus> getWorkflowStatusesByFormPks(Collection<Long> formPks);

	List<WorkflowStatus> getWorkflowStatusesByFieldPk(Long fieldPk);

	List<WorkflowStatus> getWorkflowStatusesByFieldPk(Long fieldPk, String workflowId);

	List<WorkflowStatus> getWorkflowStatusesByFieldPks(Collection<Long> fieldPks);

	void saveWorkflowStatus(WorkflowStatus ws, DatabaseActionContext context, String rationale);

	void deleteWorkflowStatus(WorkflowStatus ws, DatabaseActionContext context, String rationale);

	NavigableSet<WorkflowStatusAuditTrail> getAuditTrails(WorkflowStatus workflowStatus, Optional<Timeframe> timeframe, Optional<Long> actorPk);

	NavigableSet<WorkflowStatusAuditTrail> getAuditTrailsForProperty(WorkflowStatus workflowStatus, Optional<Timeframe> timeframe, Function<WorkflowStatusAuditTrail, Object> property);

	NavigableSet<WorkflowStatusAuditTrail> getAuditTrailsForProperties(WorkflowStatus workflowStatus, Optional<Timeframe> timeframe, List<Function<WorkflowStatusAuditTrail, Object>> properties);

	PagedResult<WorkflowStatus> search(WorkflowStatusSearch search);
}
