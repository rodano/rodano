package ch.rodano.api.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ch.rodano.api.config.FormModelDTO;
import ch.rodano.api.workflow.WorkflowDTOService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.workflowStatus.AggregateWorkflowDAOService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.utils.ACL;

@Service
public class FormDTOServiceImpl implements FormDTOService {

	private final ActorService actorService;
	private final EventService eventService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final AggregateWorkflowDAOService aggregateWorkflowDAOService;
	private final WorkflowDTOService workflowDTOService;

	public FormDTOServiceImpl(
		final WorkflowStatusDAOService workflowStatusDAOService,
		final AggregateWorkflowDAOService aggregateWorkflowDAOService,
		final WorkflowDTOService workflowDTOService,
		final EventService eventService,
		final ActorService actorService
	) {
		this.actorService = actorService;
		this.eventService = eventService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.aggregateWorkflowDAOService = aggregateWorkflowDAOService;
		this.workflowDTOService = workflowDTOService;
	}

	@Override
	public List<FormDTO> createDTOs(final Scope scope, final Optional<Event> event, final List<Form> forms, final ACL acl) {
		if(forms.isEmpty()) {
			return Collections.emptyList();
		}
		final var formComparator = event.isPresent() ? Form.getEventModelComparator(event.get().getEventModel()) : Form.getScopeModelComparator(scope.getScopeModel());
		final var formPks = forms.stream().map(Form::getPk).toList();
		final var workflowStatuses = new ArrayList<WorkflowStatus>();
		//retrieve all workflow statuses for the selected forms
		workflowStatuses.addAll(workflowStatusDAOService.getWorkflowStatusesByFormPks(formPks));
		//retrieve all aggregated workflow status for the selected forms
		workflowStatuses.addAll(aggregateWorkflowDAOService.getAggregateWorkflowStatusByForms(scope, event, forms));
		//group all workflow statuses by form pks
		final var workflowStatusesByFormPk = workflowStatuses.stream().collect(Collectors.groupingBy(WorkflowStatus::getFormFk));

		return forms.stream()
			.sorted(formComparator)
			.map(
				f -> createDTO(
					scope,
					event,
					f,
					acl,
					workflowStatusesByFormPk.getOrDefault(f.getPk(), Collections.emptyList())
				)
			)
			.toList();
	}

	@Override
	public FormDTO createDTO(final Scope scope, final Optional<Event> event, final Form form, final ACL acl) {
		final var workflowStatuses = new ArrayList<WorkflowStatus>();
		workflowStatuses.addAll(workflowStatusDAOService.getWorkflowStatusesByFormPk(form.getPk()));
		workflowStatuses.addAll(aggregateWorkflowDAOService.getAggregateWorkflowStatusByForm(scope, event, form));
		return createDTO(scope, event, form, acl, workflowStatuses);
	}

	@Override
	public FormDTO createDTO(final Scope scope, final Form form, final ACL acl) {
		return createDTO(scope, Optional.empty(), form, acl);
	}

	@Override
	public FormDTO createDTO(final Scope scope, final Event event, final Form form, final ACL acl) {
		return createDTO(scope, Optional.of(event), form, acl);
	}

	private FormDTO createDTO(
		final Scope scope,
		final Optional<Event> event,
		final Form form,
		final ACL acl,
		final List<WorkflowStatus> workflowStatuses
	) {
		final var languages = actorService.getLanguages(acl.actor());
		final var model = form.getFormModel();

		final var dto = new FormDTO();

		dto.scopePk = scope.getPk();
		dto.scopeId = scope.getId();
		dto.scopeCodeAndShortname = scope.getCodeAndShortname();

		//check if there is a event for this form
		//do not check if parameter event is null, because event parameter may be sent even if form is not linked to a event
		if(form.getEventFk() != null && event.isPresent()) {
			dto.eventPk = event.get().getPk();
			dto.eventId = event.get().getId();
			dto.eventShortname = eventService.getLabel(scope, event.get(), languages);
		}

		dto.pk = form.getPk();

		dto.creationTime = form.getCreationTime();
		dto.lastUpdateTime = form.getLastUpdateTime();

		dto.model = new FormModelDTO(model);
		dto.modelId = model.getId();

		dto.removed = form.getDeleted();
		dto.inRemoved = scope.getDeleted();
		event.ifPresent(e -> dto.inRemoved = dto.inRemoved || e.getDeleted());

		dto.inLocked = scope.getLocked() || event.isPresent() && event.get().getLocked();

		dto.canWrite = !dto.inRemoved && (event.isEmpty() || !event.get().getLocked()) && !scope.getLocked() && acl.hasRight(model, Rights.WRITE);
		dto.canBeRemoved = dto.canWrite && model.isOptional();

		dto.printable = StringUtils.isNotBlank(model.getXslTemplate());
		if(dto.printable) {
			dto.printButtonLabel = model.getPrintButtonLabel();
		}

		//workflow statuses
		dto.workflowStatuses = new ArrayList<>();
		dto.possibleWorkflows = new ArrayList<>();

		final var family = new DataFamily(scope, event, form);
		final var workflowComparator = Workflow.getWorkflowableComparator(model);
		workflowStatuses
			.stream()
			.filter(w -> acl.hasRight(w.getWorkflow()))
			.sorted(WorkflowStatus.proxyComparator(workflowComparator))
			.map(ws -> workflowDTOService.createWorkflowStatusDTO(family, ws, acl))
			.forEach(dto.workflowStatuses::add);

		//available workflow creation actions
		dto.possibleWorkflows = model.getWorkflows()
			.stream()
			.filter(w -> !w.isMandatory() && w.getActionId() != null)
			.filter(w -> !w.isUnique() || dto.workflowStatuses.stream().noneMatch(ws -> ws.getWorkflowId().equals(w.getId())))
			.filter(w -> acl.hasRight(w.getAction()))
			.sorted(workflowComparator)
			.map(w -> workflowDTOService.createWorkflowDTO(w, acl))
			.toList();

		return dto;
	}

}
