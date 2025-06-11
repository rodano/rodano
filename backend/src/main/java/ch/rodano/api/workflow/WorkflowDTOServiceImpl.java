package ch.rodano.api.workflow;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.workflow.Action;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.ACL;

@Service
public class WorkflowDTOServiceImpl implements WorkflowDTOService {

	private final ScopeDAOService scopeDAOService;
	private final EventDAOService eventDAOService;
	private final FieldDAOService fieldDAOService;
	private final WorkflowStatusService workflowStatusService;

	public WorkflowDTOServiceImpl(
		final WorkflowStatusService workflowStatusService,
		final ScopeDAOService scopeDAOService,
		final EventDAOService eventDAOService,
		final FieldDAOService fieldDAOService
	) {
		this.scopeDAOService = scopeDAOService;
		this.eventDAOService = eventDAOService;
		this.fieldDAOService = fieldDAOService;
		this.workflowStatusService = workflowStatusService;
	}

	@Override
	public WorkflowDTO createWorkflowDTO(final Workflow workflow, final ACL acl) {
		final var dto = new WorkflowDTO();
		dto.id = workflow.getId();

		dto.shortname = workflow.getShortname();
		dto.longname = workflow.getLongname();
		dto.description = workflow.getDescription();

		dto.aggregator = workflow.isAggregator();
		dto.aggregatedWorkflowId = workflow.getAggregateWorkflowId();
		dto.mandatory = workflow.isMandatory();
		dto.actionId = workflow.getActionId();

		dto.message = workflow.getMessage();
		dto.icon = workflow.getIcon();

		dto.states = createWorkflowStateDTOs(workflow.getStates(), acl);
		dto.actions = WorkflowActionDTO.fromActions(workflow.getActions());

		return dto;
	}

	private WorkflowStateDTO createWorkflowStateDTO(final WorkflowState state, final List<Action> actions) {
		final var dto = new WorkflowStateDTO();
		dto.icon = StringUtils.defaultIfBlank(state.getIcon(), state.getWorkflow().getIcon());
		dto.color = state.getColor();
		dto.id = state.getId();
		dto.important = state.isImportant();
		dto.shortname = state.getShortname();
		dto.longname = state.getLongname();
		dto.description = state.getDescription();
		dto.possibleActions = actions.stream().map(WorkflowActionDTO::new).toList();
		return dto;
	}

	@Override
	public WorkflowStateDTO createWorkflowStateDTO(final WorkflowState state, final ACL acl) {
		final var actions = state.getPossibleActions().stream()
			.filter(a -> acl.hasRight(a))
			.toList();
		return createWorkflowStateDTO(state, actions);
	}

	@Override
	public WorkflowStateDTO createWorkflowStateDTO(final WorkflowStatus status, final ACL acl) {
		final var state = status.getState();
		//retrieve workflow creator
		final var creatorProfile = workflowStatusService.getCreatorProfile(status);
		final var actions = state.getPossibleActions().stream()
			.filter(a -> acl.hasRight(a, creatorProfile))
			.toList();
		return createWorkflowStateDTO(state, actions);
	}

	@Override
	public List<WorkflowStateDTO> createWorkflowStateDTOs(final List<WorkflowState> states, final ACL acl) {
		return states.stream()
			.map(s -> createWorkflowStateDTO(s, acl))
			.toList();
	}

	/**
	* Build a DTO for a workflow status
	* @deprecated
	* This method has performance issues because it needs to fetch the entity associated to the workflow
	* <p> Use {@link WorkflowDTOServiceImpl#createWorkflowStatusDTO(DataFamily,WorkflowStatus,ACL)} instead.
	*/
	@Override
	@Deprecated
	public WorkflowStatusDTO createWorkflowStatusDTO(final WorkflowStatus workflowStatus, final ACL acl) {
		final var workflow = workflowStatus.getWorkflow();

		final var dto = new WorkflowStatusDTO();
		dto.pk = workflowStatus.getPk();
		dto.workflowId = workflow.getId();
		dto.statusId = workflowStatus.getStateId();
		dto.orderBy = workflow.getOrderBy();
		dto.date = workflowStatus.getLastUpdateTime();
		dto.triggerMessage = workflowStatus.getTriggerMessage();
		dto.hasCreationAction = workflow.getActionId() != null;
		dto.workflow = createWorkflowDTO(workflow, acl);
		dto.state = createWorkflowStateDTO(workflowStatus, acl);

		if(workflowStatus.getScopeFk() != null) {
			final var scope = scopeDAOService.getScopeByPk(workflowStatus.getScopeFk());
			dto.scopeFk = scope.getPk();
			dto.scopeCode = scope.getCode();
			dto.scopeShortname = scope.getShortname();
		}

		if(workflowStatus.getEventFk() != null) {
			final var event = eventDAOService.getEventByPk(workflowStatus.getEventFk());
			dto.eventFk = event.getPk();
			dto.eventShortname = event.getEventModel().getDefaultLocalizedShortname();
			dto.eventDate = event.getDate();
		}

		if(workflowStatus.getFieldFk() != null) {
			final var field = fieldDAOService.getFieldByPk(workflowStatus.getFieldFk());
			dto.fieldFk = field.getPk();
			dto.fieldShortname = field.getFieldModel().getDefaultLocalizedShortname();
		}

		return dto;
	}

	@Override
	public WorkflowStatusDTO createWorkflowStatusDTO(final DataFamily family, final WorkflowStatus workflowStatus, final ACL acl) {
		final var workflow = workflowStatus.getWorkflow();

		final var dto = new WorkflowStatusDTO();
		dto.pk = workflowStatus.getPk();
		dto.workflowId = workflow.getId();
		dto.statusId = workflowStatus.getStateId();
		dto.orderBy = workflow.getOrderBy();
		dto.date = workflowStatus.getLastUpdateTime();
		dto.triggerMessage = workflowStatus.getTriggerMessage();
		dto.hasCreationAction = workflow.getActionId() != null;
		dto.workflow = createWorkflowDTO(workflow, acl);
		dto.state = createWorkflowStateDTO(workflowStatus, acl);

		dto.scopeFk = family.scope().getPk();
		dto.scopeCode = family.scope().getCode();
		dto.scopeShortname = family.scope().getShortname();

		family.event().ifPresent(event -> {
			dto.eventFk = event.getPk();
			dto.eventShortname = event.getEventModel().getDefaultLocalizedShortname();
			dto.eventDate = event.getDate();
		});

		family.field().ifPresent(field -> {
			dto.fieldFk = field.getPk();
			dto.fieldShortname = field.getFieldModel().getDefaultLocalizedShortname();
		});

		return dto;
	}

	@Override
	public WorkflowStatusDTO createWorkflowStatusDTO(final Workflow workflow, final WorkflowState state, final ACL acl) {
		final var dto = new WorkflowStatusDTO();
		dto.workflowId = workflow.getId();
		dto.statusId = state.getId();
		dto.orderBy = workflow.getOrderBy();
		dto.hasCreationAction = workflow.getActionId() != null;
		dto.workflow = createWorkflowDTO(workflow, acl);
		dto.state = createWorkflowStateDTO(state, acl);

		return dto;
	}

}
