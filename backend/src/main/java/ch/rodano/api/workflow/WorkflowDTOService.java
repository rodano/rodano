package ch.rodano.api.workflow;

import java.util.List;

import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.utils.ACL;

public interface WorkflowDTOService {

	WorkflowDTO createWorkflowDTO(Workflow workflow, ACL acl);

	/**
	 * Take care, does not check right on state.possibleActions
	 *
	 * @param state the model state
	 */
	WorkflowStateDTO createWorkflowStateDTO(WorkflowState state, ACL acl);

	/**
	 * @param status the model state
	 */
	WorkflowStateDTO createWorkflowStateDTO(WorkflowStatus status, ACL acl);

	List<WorkflowStateDTO> createWorkflowStateDTOs(List<WorkflowState> states, ACL acl);

	WorkflowStatusDTO createWorkflowStatusDTO(WorkflowStatus workflowStatus, ACL acl);

	WorkflowStatusDTO createWorkflowStatusDTO(Workflow workflow, WorkflowState state, ACL acl);

	WorkflowStatusDTO createWorkflowStatusDTO(DataFamily family, WorkflowStatus workflowStatus, ACL acl);
}
