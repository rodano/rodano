package ch.rodano.api.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ch.rodano.api.config.ConfigDTOService;
import ch.rodano.api.field.FieldDTOService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.utils.ACL;

@Service
public class DatasetDTOServiceImpl implements DatasetDTOService {

	private final ActorService actorService;
	private final EventService eventService;
	private final FieldService fieldService;
	private final FieldDTOService fieldDTOService;
	private final ConfigDTOService configDTOService;

	public DatasetDTOServiceImpl(
		final ActorService actorService,
		final EventService eventService,
		final FieldService fieldService,
		final FieldDTOService fieldDTOService,
		final ConfigDTOService configDTOService
	) {
		this.actorService = actorService;
		this.eventService = eventService;
		this.fieldService = fieldService;
		this.fieldDTOService = fieldDTOService;
		this.configDTOService = configDTOService;
	}

	private DatasetDTO createDTO(
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final List<Field> fields,
		final ACL acl
	) {
		final var languages = actorService.getLanguages(acl.actor());
		final var model = dataset.getDatasetModel();

		final var dto = new DatasetDTO();

		dto.scopePk = scope.getPk();
		dto.scopeId = scope.getId();
		dto.scopeCodeAndShortname = scope.getCodeAndShortname();

		//check if there is a event for this dataset
		//do not check if parameter event is null, because event parameter may be sent even if dataset is not linked to a event
		if(dataset.getEventFk() != null && event.isPresent()) {
			dto.eventPk = event.get().getPk();
			dto.eventId = event.get().getId();
			dto.eventShortname = eventService.getLabel(scope, event.get(), languages);
		}

		dto.pk = dataset.getPk();
		dto.id = dataset.getId();

		dto.creationTime = dataset.getCreationTime();
		dto.lastUpdateTime = dataset.getLastUpdateTime();

		dto.model = configDTOService.createDatasetModelDTO(model, acl);
		dto.modelId = model.getId();

		dto.removed = dataset.getDeleted();
		dto.inRemoved = scope.getDeleted();
		event.ifPresent(e -> dto.inRemoved = dto.inRemoved || e.getDeleted());

		dto.inLocked = scope.getLocked() || event.isPresent() && event.get().getLocked();

		dto.canWrite = !dto.inRemoved && (event.isEmpty() || !event.get().getLocked()) && !scope.getLocked() && acl.hasRight(model, Rights.WRITE);
		dto.canBeRemoved = dto.canWrite;

		dto.fields = fieldDTOService.createDTOs(scope, event, dataset, fields, acl)
			.stream()
			.toList();

		return dto;
	}

	@Override
	public DatasetDTO createDTO(final Scope scope, final Optional<Event> event, final Dataset dataset, final ACL acl) {
		final var fields = fieldService.getAll(dataset);
		return createDTO(scope, event, dataset, fields, acl);
	}

	@Override
	public List<DatasetDTO> createDTOs(final Scope scope, final Optional<Event> event, final Form form, final Collection<Dataset> datasets, final ACL acl) {
		//retrieve fields used in the provided form
		final var fieldModels = form.getFormModel().getFieldModels();

		final var dtos = new ArrayList<DatasetDTO>();
		for(final Dataset dataset : datasets) {
			final var fields = fieldService.getAll(dataset, fieldModels);
			if(!fields.isEmpty()) {
				dtos.add(createDTO(scope, event, dataset, fields, acl));
			}
		}

		return dtos;
	}

}
