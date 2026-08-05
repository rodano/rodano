package ch.rodano.api.field;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ch.rodano.api.config.FieldModelDTO;
import ch.rodano.api.config.PossibleValueDTO;
import ch.rodano.api.workflow.WorkflowDTOService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.core.model.audit.models.FieldAuditTrail;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.field.FieldRecord;
import ch.rodano.core.model.file.File;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.file.FileService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.file.FileDAOService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.utils.ACL;

@Service
public class FieldDTOServiceImpl implements FieldDTOService {

	private final ActorService actorService;
	private final EventService eventService;
	private final FieldService fieldService;
	private final FieldDAOService fieldDAOService;
	private final FileService fileService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final FileDAOService fileDAOService;
	private final WorkflowDTOService workflowDTOService;

	public FieldDTOServiceImpl(
		final ActorService actorService,
		final EventService eventService,
		final FieldService fieldService,
		final FieldDAOService fieldDAOService,
		final FileService fileService,
		final WorkflowStatusDAOService workflowStatusDAOService,
		final FileDAOService fileDAOService,
		final WorkflowDTOService workflowDTOService
	) {
		this.actorService = actorService;
		this.eventService = eventService;
		this.fieldService = fieldService;
		this.fieldDAOService = fieldDAOService;
		this.fileService = fileService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.fileDAOService = fileDAOService;
		this.workflowDTOService = workflowDTOService;
	}

	@Override
	public List<FieldDTO> createDTOs(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Collection<Field> fields,
		final ACL acl
	) {
		if(fields.isEmpty()) {
			return Collections.emptyList();
		}
		final var fieldPks = fields.stream().map(Field::getPk).toList();
		//retrieve all workflow status for the selected fields
		final var workflowStatusesByFieldPk = workflowStatusDAOService.getWorkflowStatusesByFieldPks(fieldPks)
			.stream()
			.collect(Collectors.groupingBy(WorkflowStatus::getFieldFk));
		//retrieve all submitted files for the selected fields
		final var filesByFieldPk = fileDAOService.getFileByFieldPks(fieldPks)
			.stream()
			.collect(Collectors.toMap(File::getFieldFk, Function.identity()));
		//timeframe is the same for every field of the batch, since it only depends on the dataset model and the ACL
		final var timeframe = acl.getTimeframe(dataset.getDatasetModel(), Rights.READ);
		//retrieve all audit trails for the selected fields
		final var auditTrailsByFieldPk = fieldDAOService.getAuditTrailsForProperty(fields, timeframe, FieldRecord::getValue);
		return fields.stream()
			.map(
				f -> createDTO(
					scope,
					event,
					dataset,
					f,
					acl,
					Optional.ofNullable(filesByFieldPk.get(f.getPk())),
					workflowStatusesByFieldPk.getOrDefault(f.getPk(), Collections.emptyList()),
					timeframe,
					auditTrailsByFieldPk.getOrDefault(f.getPk(), Collections.emptyNavigableSet())
				)
			)
			.toList();
	}

	@Override
	public FieldDTO createDTO(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final ACL acl
	) {
		final var file = Optional.ofNullable(fileService.getFile(field));
		final var workflowStatuses = workflowStatusDAOService.getWorkflowStatusesByFieldPk(field.getPk());
		final var timeframe = acl.getTimeframe(dataset.getDatasetModel(), Rights.READ);
		final var auditTrails = fieldDAOService.getAuditTrailsForProperty(field, timeframe, FieldRecord::getValue);
		return createDTO(scope, event, dataset, field, acl, file, workflowStatuses, timeframe, auditTrails);
	}

	private FieldDTO createDTO(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final ACL acl,
		final Optional<File> file,
		final List<WorkflowStatus> workflowStatuses,
		final Optional<Timeframe> timeframe,
		final NavigableSet<FieldAuditTrail> auditTrails
	) {
		final var languages = actorService.getLanguages(acl.actor());
		final var model = field.getFieldModel();

		final var dto = new FieldDTO();

		dto.scopePk = scope.getPk();
		dto.scopeId = scope.getId();
		dto.scopeCodeAndShortname = scope.getCodeAndShortname();

		//check if there is a event for this dataset
		//do not check if parameter event is null, because event parameter may be sent even if dataset is not linked to a event
		if(dataset.getEventFk() != null) {
			dto.eventPk = event.get().getPk();
			dto.eventId = event.get().getId();
			dto.eventShortname = eventService.getLabel(scope, event.get(), languages);
		}

		dto.datasetPk = dataset.getPk();
		dto.datasetId = dataset.getId();
		dto.datasetModelId = dataset.getDatasetModel().getId();

		dto.pk = field.getPk();

		dto.creationTime = field.getCreationTime();
		dto.lastUpdateTime = field.getLastUpdateTime();

		if(file.isPresent()) {
			dto.filePk = file.get().getPk();
			dto.fileName = file.get().getName();
		}

		dto.model = new FieldModelDTO(model, languages);
		dto.modelId = model.getId();

		final var possibleValues = fieldService.getPossibleValues(scope, event, dataset, field);
		final var value = fieldService.getInterpretedValue(scope, event, dataset, field, timeframe.flatMap(Timeframe::stopDate));
		dto.possibleValues = possibleValues.stream().map(PossibleValueDTO::new).toList();
		dto.value = value;
		dto.valueLabel = field.getFieldModel().valueToLabel(possibleValues, value, languages);

		dto.inRemoved = scope.isRemoved() || dataset.isRemoved();
		event.ifPresent(e -> dto.inRemoved = dto.inRemoved || e.isRemoved());

		dto.inLocked = scope.getLocked() || event.isPresent() && event.get().getLocked();

		//workflows
		final var family = new DataFamily(scope, event, dataset, field);
		final var workflowComparator = Workflow.getWorkflowableComparator(model);
		dto.workflowStatuses = workflowStatuses
			.stream()
			.filter(w -> acl.hasRight(w.getWorkflow()))
			.sorted(WorkflowStatus.proxyComparator(workflowComparator))
			.map(ws -> workflowDTOService.createWorkflowStatusDTO(family, ws, acl))
			.toList();

		//workflows that can be created
		if(!dto.isInRemoved() && !dto.isInLocked()) {
			dto.possibleWorkflows = model.getWorkflows()
				.stream()
				.filter(w -> !w.isMandatory() && w.getActionId() != null)
				.filter(w -> !w.isUnique() || dto.workflowStatuses.stream().noneMatch(ws -> ws.getWorkflowId().equals(w.getId())))
				.filter(w -> acl.hasRight(w.getAction()))
				.sorted(workflowComparator)
				.map(w -> workflowDTOService.createWorkflowDTO(w, acl))
				.toList();
		}
		else {
			dto.possibleWorkflows = Collections.emptyList();
		}

		if(auditTrails.size() > 1) {
			dto.newContent = auditTrails.stream()
				.filter(e -> e.getValue() != null && !Objects.equals(e.getValue(), ""))
				.count() > 1;
		}

		return dto;
	}

}
