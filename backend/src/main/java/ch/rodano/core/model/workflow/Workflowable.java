package ch.rodano.core.model.workflow;

import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.configuration.model.workflow.WorkflowableModel;

public interface Workflowable {

	Long getPk();

	String getId();

	WorkflowableEntity getWorkflowableEntity();

	WorkflowableModel getWorkflowableModel();
}
