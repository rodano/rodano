package ch.rodano.api.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
		return fields.stream()
			.map(
				f -> createDTO(
					scope,
					event,
					dataset,
					f,
					acl,
					Optional.ofNullable(filesByFieldPk.get(f.getPk())),
					workflowStatusesByFieldPk.getOrDefault(f.getPk(), Collections.emptyList())
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
		return createDTO(scope, event, dataset, field, acl, file, workflowStatuses);
	}

	private FieldDTO createDTO(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final ACL acl,
		final Optional<File> file,
		final List<WorkflowStatus> workflowStatuses
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

		final var timeframe = acl.getTimeframe(dataset.getDatasetModel(), Rights.READ);
		final var possibleValues = fieldService.getPossibleValues(scope, event, dataset, field);
		final var value = fieldService.getInterpretedValue(scope, event, dataset, field, timeframe.flatMap(Timeframe::stopDate));
		dto.possibleValues = possibleValues.stream().map(PossibleValueDTO::new).toList();
		dto.value = value;
		dto.valueLabel = field.getFieldModel().valueToLabel(possibleValues, value, languages);

		//workflow statuses
		dto.workflowStatuses = new ArrayList<>();
		dto.possibleWorkflows = new ArrayList<>();

		final var family = new DataFamily(scope, event, dataset, field);
		final var workflowComparator = Workflow.getWorkflowableComparator(model);
		workflowStatuses
			.stream()
			.filter(w -> acl.hasRight(w.getWorkflow()))
			.sorted(WorkflowStatus.proxyComparator(workflowComparator))
			.map(ws -> workflowDTOService.createWorkflowStatusDTO(family, ws, acl))
			.forEach(dto.workflowStatuses::add);

		//workflow that can be created
		dto.possibleWorkflows = model.getWorkflows()
			.stream()
			.filter(w -> !w.isMandatory() && w.getActionId() != null)
			.filter(w -> !w.isUnique() || dto.workflowStatuses.stream().noneMatch(ws -> ws.getWorkflowId().equals(w.getId())))
			.filter(w -> acl.hasRight(w.getAction()))
			.sorted(workflowComparator)
			.map(w -> workflowDTOService.createWorkflowDTO(w, acl))
			.toList();

		final var entries = fieldDAOService.getAuditTrailsForProperty(field, timeframe, FieldRecord::getValue);
		if(entries.size() > 1) {
			dto.newContent = entries.stream()
				.filter(e -> e.getValue() != null && !Objects.equals(e.getValue(), ""))
				.count() > 1;
		}

		dto.inRemoved = scope.getDeleted() || dataset.getDeleted();
		event.ifPresent(e -> dto.inRemoved = dto.inRemoved || e.getDeleted());

		dto.inLocked = scope.getLocked() || event.isPresent() && event.get().getLocked();

		return dto;
	}

}
