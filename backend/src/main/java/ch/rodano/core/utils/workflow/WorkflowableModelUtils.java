package ch.rodano.core.utils.workflow;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowableModel;
import ch.rodano.core.model.role.Role;

public class WorkflowableModelUtils {

	public static Set<Workflow> getWorkflows(final WorkflowableModel workflowableModel, final Collection<Role> roles) {
		final Set<Workflow> workflows = new LinkedHashSet<>();
		roles.forEach(role -> workflowableModel.getWorkflows().stream().filter(workflow -> role.getProfile().hasRight(workflow)).forEach(workflows::add));
		return workflows;
	}
}
