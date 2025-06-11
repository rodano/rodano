package ch.rodano.api.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.utils.URLConsistencyUtils;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.file.File;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.file.FileService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "File management", description = "Upload/Download for files on fields")
@RestController
@Validated
@Transactional(readOnly = true)
public class FileController extends AbstractSecuredController {

	private final FileService fileService;
	private final UtilsService utilsService;
	private final ScopeDAOService scopeDAOService;
	private final EventDAOService eventDAOService;
	private final FieldDAOService fieldDAOService;
	private final DatasetDAOService datasetDAOService;

	public FileController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final FileService fileService,
		final UtilsService utilsService,
		final ScopeDAOService scopeDAOService,
		final EventDAOService eventDAOService,
		final FieldDAOService fieldDAOService,
		final DatasetDAOService datasetDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.fileService = fileService;
		this.utilsService = utilsService;
		this.scopeDAOService = scopeDAOService;
		this.eventDAOService = eventDAOService;
		this.fieldDAOService = fieldDAOService;
		this.datasetDAOService = datasetDAOService;
	}

	@Operation(summary = "Upload a temporary scope file that can then be linked to a scope field")
	@PostMapping(path = "/scopes/{scopePk}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public FileDTO createScopeFieldFile(
		@PathVariable final Long scopePk,
		@RequestParam("file") final MultipartFile multipartFile
	) throws IOException {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = Optional.<Event> empty();

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final User user = (User) currentActor();
		final var currentRoles = currentActiveRoles();

		rightsService.checkRight(user, currentRoles, scope.getScopeModel(), Rights.READ);

		final var fileName = multipartFile.getOriginalFilename();
		try(final var fileStream = multipartFile.getInputStream()) {
			final File file = fileService.create(user, scope, event, fileName, fileStream, currentContext(), "File upload");
			return new FileDTO(file);
		}
	}

	@Operation(summary = "Upload a temporary event file that can then be linked to a event field")
	@PostMapping(path = "/scopes/{scopePk}/events/{eventPk}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public FileDTO createEventFieldFile(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@RequestParam("file") final MultipartFile multipartFile
	) throws IOException {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);

		URLConsistencyUtils.checkConsistency(scope, event);

		final User user = (User) currentActor();
		final var currentRoles = currentActiveRoles();

		rightsService.checkRight(user, currentRoles, scope.getScopeModel(), Rights.READ);
		rightsService.checkRight(user, currentRoles, event.getEventModel(), Rights.READ);

		final var fileName = multipartFile.getOriginalFilename();
		try(final var fileStream = multipartFile.getInputStream()) {
			final File file = fileService.create(user, scope, Optional.of(event), fileName, fileStream, currentContext(), "File upload");
			return new FileDTO(file);
		}
	}

	//this method can not be used at the moment because of multiple datasets that may not be exist in the database when the file is uploaded
	//in this case, datasetPk and fieldPk are unknown and can not be set in the URL
	@Operation(summary = "Upload a file on a field in a dataset on a scope")
	@PostMapping(path = "/scopes/{scopePk}/datasets/{datasetPk}/fields/{fieldPk}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public FileDTO createScopeFieldFile(
		@PathVariable final Long scopePk,
		@PathVariable final Long datasetPk,
		@PathVariable final Long fieldPk,
		@RequestParam("file") final MultipartFile multipartFile
	) throws IOException {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = Optional.<Event> empty();
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);
		final var field = fieldDAOService.getFieldByPk(fieldPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);
		utilsService.checkNotNull(Field.class, field, fieldPk);

		URLConsistencyUtils.checkConsistency(scope, event, dataset, field);

		final User user = (User) currentActor();
		final var currentRoles = currentActiveRoles();

		rightsService.checkRight(user, currentRoles, scope.getScopeModel(), Rights.READ);
		rightsService.checkRight(user, currentRoles, dataset.getDatasetModel(), Rights.WRITE);

		final var fileName = multipartFile.getOriginalFilename();
		try(final var fileStream = multipartFile.getInputStream()) {
			final File file = fileService.create(user, scope, event, dataset, field, fileName, fileStream, currentContext(), "File upload");
			return new FileDTO(file);
		}
	}

	//this method can not be used at the moment because of multiple datasets that may not be exist in the database when the file is uploaded
	//in this case, datasetPk and fieldPk are unknown and can not be set in the URL
	@Operation(summary = "Upload a file on a field in a dataset on an event")
	@PostMapping(path = "/scopes/{scopePk}/events/{eventPk}/datasets/{datasetPk}/fields/{fieldPk}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public FileDTO createEventFieldFile(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@PathVariable final Long datasetPk,
		@PathVariable final Long fieldPk,
		@RequestParam("file") final MultipartFile multipartFile
	) throws IOException {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);
		final var field = fieldDAOService.getFieldByPk(fieldPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);
		utilsService.checkNotNull(Field.class, field, fieldPk);

		URLConsistencyUtils.checkConsistency(scope, Optional.of(event), dataset, field);

		final User user = (User) currentActor();
		final var currentRoles = currentActiveRoles();

		rightsService.checkRight(user, currentRoles, scope.getScopeModel(), Rights.READ);
		rightsService.checkRight(user, currentRoles, event.getEventModel(), Rights.READ);
		rightsService.checkRight(user, currentRoles, dataset.getDatasetModel(), Rights.WRITE);

		final var fileName = multipartFile.getOriginalFilename();
		try(final var fileStream = multipartFile.getInputStream()) {
			final File file = fileService.create(user, scope, Optional.of(event), dataset, field, fileName, fileStream, currentContext(), "File upload");
			return new FileDTO(file);
		}
	}

	@Operation(summary = "Download a file")
	@GetMapping("/files/{filePk}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> getFile(
		@PathVariable final Long filePk
	) {
		final var file = fileService.getFileByPk(filePk);
		final var scope = scopeDAOService.getScopeByPk(file.getScopeFk());
		final var event = Optional.ofNullable(file.getEventFk()).map(eventDAOService::getEventByPk);

		utilsService.checkNotNull(File.class, file, filePk);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> rightsService.checkRight(currentActor, currentRoles, e.getEventModel(), Rights.READ));

		//send response
		final StreamingResponseBody stream = os -> {
			try(var is = new FileInputStream(fileService.getStoredFile(file))) {
				is.transferTo(os);
			}
		};
		return fileResponse(stream, MediaType.APPLICATION_OCTET_STREAM, file.getName());
	}

	//this method can not be used at the moment because files are not necessarily linked to a dataset and a field (think about a file attached to a field in a multiple dataset just created
	@Operation(summary = "Download a file")
	@GetMapping("/scopes/{scopePk}/datasets/{datasetPk}/fields/{fieldPk}/files/{filePk}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> getFileOnScope(
		@PathVariable final Long scopePk,
		@PathVariable final Long datasetPk,
		@PathVariable final Long fieldPk,
		@PathVariable final Long filePk
	) {
		return downloadFile(scopePk, Optional.empty(), datasetPk, fieldPk, filePk);
	}

	//this method can not be used at the moment because files are not necessarily linked to a dataset and a field (think about a file attached to a field in a multiple dataset just created
	@Operation(summary = "Download a file")
	@GetMapping("/scopes/{scopePk}/events/{eventPk}/datasets/{datasetPk}/fields/{fieldPk}/files/{filePk}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> getFileOnEvent(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@PathVariable final Long datasetPk,
		@PathVariable final Long fieldPk,
		@PathVariable final Long filePk
	) {
		return downloadFile(scopePk, Optional.of(eventPk), datasetPk, fieldPk, filePk);
	}

	private ResponseEntity<StreamingResponseBody> downloadFile(final Long scopePk, final Optional<Long> eventPk, final Long datasetPk, final Long fieldPk, final Long filePk) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);
		final var field = fieldDAOService.getFieldByPk(fieldPk);
		final var file = fileService.getFileByPk(filePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		event.ifPresent(e -> utilsService.checkNotNull(Event.class, e, eventPk.get()));
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);
		utilsService.checkNotNull(Field.class, field, fieldPk);

		URLConsistencyUtils.checkConsistency(scope, event, dataset, field, file);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> rightsService.checkRight(currentActor, currentRoles, e.getEventModel(), Rights.READ));
		rightsService.checkRight(currentActor, currentRoles, dataset.getDatasetModel(), Rights.READ);

		//send response
		final StreamingResponseBody stream = os -> {
			try(var is = new FileInputStream(fileService.getStoredFile(file))) {
				is.transferTo(os);
			}
		};
		return fileResponse(stream, MediaType.APPLICATION_OCTET_STREAM, file.getName());
	}

	@Operation(summary = "Download all files attached to a scope and its descendant")
	@GetMapping("/scopes/{scopePk}/files")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> downloadAllFiles(
		@PathVariable final Long scopePk,
		@RequestParam final Optional<String> scopeModelId
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.READ);
		rightsService.checkRight(currentActor(), currentRoles, FeatureStatic.EXPORT);

		final var study = studyService.getStudy();
		final var scopeModel = scopeModelId.map(study::getScopeModel);

		//send response
		final StreamingResponseBody stream = os -> fileService.writeScopeFiles(scope, scopeModel, os);
		final var filename = study.generateFilename("files", ExportFormat.ZIP);
		return exportResponse(ExportFormat.ZIP, stream, filename);
	}
}
