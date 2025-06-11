package ch.rodano.api.dataset;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.controller.form.exception.DatasetSubmissionException;
import ch.rodano.api.form.BlockingErrorsDTO;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.utils.URLConsistencyUtils;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.dataset.DatasetSubmissionService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Profile("!migration")
@Tag(name = "Dataset")
@RestController
@RequestMapping("/scopes/{scopePk}")
@Validated
@Transactional(readOnly = true)
public class DatasetController extends AbstractSecuredController {
	private final ScopeDAOService scopeDAOService;
	private final EventDAOService eventDAOService;
	private final FormDAOService formDAOService;
	private final DatasetService datasetService;
	private final DatasetDAOService datasetDAOService;
	private final DatasetDTOService datasetDTOService;
	private final DatasetSubmissionService datasetSubmissionService;
	private final UtilsService utilsService;
	private final PlatformTransactionManager transactionManager;

	public DatasetController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ScopeDAOService scopeDAOService,
		final EventDAOService eventDAOService,
		final FormDAOService formDAOService,
		final DatasetService datasetService,
		final DatasetDAOService datasetDAOService,
		final DatasetDTOService datasetDTOService,
		final DatasetSubmissionService datasetSubmissionService,
		final UtilsService utilsService,
		final PlatformTransactionManager transactionManager
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.scopeDAOService = scopeDAOService;
		this.eventDAOService = eventDAOService;
		this.formDAOService = formDAOService;
		this.datasetService = datasetService;
		this.datasetDAOService = datasetDAOService;
		this.datasetDTOService = datasetDTOService;
		this.datasetSubmissionService = datasetSubmissionService;
		this.utilsService = utilsService;
		this.transactionManager = transactionManager;
	}

	@Operation(summary = "Get datasets")
	@GetMapping({ "datasets", "events/{eventPk}/datasets" })
	@ResponseStatus(HttpStatus.OK)
	public List<DatasetDTO> getDatasets(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@RequestParam final Optional<Collection<String>> datasetModelIds
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));

		final var datasetModels = datasetModelIds
			.map(d -> (Collection<DatasetModel>) d.stream().map(i -> studyService.getStudy().getDatasetModel(i)).collect(Collectors.toList()));
		final var datasets = datasetService.search(scope, event, datasetModels, acl);

		return datasets
			.stream()
			.map(d -> datasetDTOService.createDTO(scope, event, d, acl))
			.toList();
	}

	@Operation(summary = "Get datasets for a from")
	@GetMapping({ "forms/{formPk}/datasets", "events/{eventPk}/forms/{formPk}/datasets" })
	@ResponseStatus(HttpStatus.OK)
	public List<DatasetDTO> getDatasets(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long formPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var form = formDAOService.getFormByPk(formPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Form.class, form, formPk);
		URLConsistencyUtils.checkConsistency(scope, event, form);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));
		acl.checkRight(form.getFormModel(), Rights.READ);

		//retrieve form dataset models
		final var datasetModels = form.getFormModel().getDatasetModels();
		final var datasets = datasetService.search(scope, event, Optional.of(datasetModels), acl);

		return datasetDTOService.createDTOs(scope, event, form, datasets, acl);
	}

	@Operation(summary = "Get dataset")
	@GetMapping({ "events/{eventPk}/datasets/{datasetPk}", "datasets/{datasetPk}" })
	@ResponseStatus(HttpStatus.OK)
	public DatasetDTO getDataset(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long datasetPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);
		URLConsistencyUtils.checkConsistency(scope, event, dataset);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));
		acl.checkRight(dataset.getDatasetModel(), Rights.READ);

		return datasetDTOService.createDTO(scope, event, dataset, acl);
	}

	/**
	 * This function programmatically crates its own isolated transaction and sets it to "rollback only"
	 * in order to roll back the transaction.
	 */
	@Operation(summary = "Create a candidate dataset on a scope", description = "Provides a dataset skeleton from which an actual dataset can be created")
	@GetMapping("datasets/candidate")
	@ResponseStatus(HttpStatus.OK)
	public DatasetDTO createCandidateScopeDataset(
		@PathVariable final Long scopePk,
		@RequestParam final String datasetModelId
	) {
		final var transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		return transactionTemplate.execute(status -> {
			status.setRollbackOnly();

			final var scope = scopeDAOService.getScopeByPk(scopePk);
			utilsService.checkNotNull(Scope.class, scope, scopePk);

			final var datasetModel = studyService.getStudy().getDatasetModel(datasetModelId);

			//check rights
			final var acl = rightsService.getACL(currentActor(), scope);
			acl.checkRight(datasetModel, Rights.WRITE);

			final var candidateDataset = datasetService.createCandidate(scope, Optional.empty(), datasetModel, acl.actor());
			final var datasetDTO = datasetDTOService.createDTO(scope, Optional.empty(), candidateDataset, acl);
			cleanPksFromDataset(datasetDTO);
			return datasetDTO;
		});
	}

	/**
	 * This function programmatically crates its own isolated transaction and sets it to "rollback only"
	 * in order to roll back the transaction.
	 */
	@Operation(summary = "Create a candidate dataset on a event", description = "Provides a dataset skeleton from which an actual dataset can be created")
	@GetMapping("events/{eventPk}/datasets/candidate")
	@ResponseStatus(HttpStatus.OK)
	public DatasetDTO createCandidateEventDataset(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@RequestParam final String datasetModelId
	) {
		final var transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		return transactionTemplate.execute(status -> {
			status.setRollbackOnly();

			final var scope = scopeDAOService.getScopeByPk(scopePk);
			final var event = eventDAOService.getEventByPk(eventPk);

			utilsService.checkNotNull(Scope.class, scope, scopePk);
			utilsService.checkNotNull(Event.class, event, eventPk);
			URLConsistencyUtils.checkConsistency(scope, event);

			final var datasetModel = studyService.getStudy().getDatasetModel(datasetModelId);

			//check rights
			final var acl = rightsService.getACL(currentActor(), scope);
			acl.checkRight(datasetModel, Rights.WRITE);

			final var candidateDataset = datasetService.createCandidate(scope, Optional.of(event), datasetModel, acl.actor());
			final var datasetDTO = datasetDTOService.createDTO(scope, Optional.of(event), candidateDataset, acl);
			cleanPksFromDataset(datasetDTO);
			return datasetDTO;
		});
	}

	@Operation(summary = "Create a dataset")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "400", description = "A blocking error has occurred", content = {
			@Content(schema = @Schema(implementation = BlockingErrorsDTO.class))
		})
	})
	@PostMapping({ "events/{eventPk}/datasets", "datasets" })
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional(rollbackFor = DatasetSubmissionException.class)
	public DatasetDTO createDataset(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@Valid @RequestBody final DatasetCreationDTO datasetDTO,
		@RequestHeader("X-Rationale") final Optional<String> rationale
	) throws DatasetSubmissionException {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		final var acl = rightsService.getACL(currentActor(), scope);

		//rights will be checked when saving datasets
		final var datasets = datasetSubmissionService.createAndSaveDatasets(acl, scope, event, Collections.singleton(datasetDTO), currentContext(), rationale);
		//as only one dataset has been submitted, only one dataset is returned
		final var dataset = datasets.iterator().next();

		return datasetDTOService.createDTO(scope, event, dataset, acl);
	}

	@Operation(summary = "Save dataset")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "400", description = "A blocking error has occurred", content = {
			@Content(schema = @Schema(implementation = BlockingErrorsDTO.class))
		})
	})
	@PutMapping({ "events/{eventPk}/datasets/{datasetPk}", "datasets/{datasetPk}" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional(rollbackFor = DatasetSubmissionException.class)
	public DatasetDTO saveDataset(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long datasetPk,
		@Valid @RequestBody final DatasetUpdateDTO datasetUpdateDTO,
		@RequestHeader("X-Rationale") final Optional<String> rationale
	) throws DatasetSubmissionException {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);
		URLConsistencyUtils.checkConsistency(scope, event, dataset);

		final var acl = rightsService.getACL(currentActor(), scope);

		//override possible pk in DTO
		//the goal is to avoid that the client forges the pk and write another dataset
		datasetUpdateDTO.setPk(datasetPk);

		//rights will be checked when saving datasets
		datasetSubmissionService.saveDatasets(acl, scope, event, Collections.singleton(datasetUpdateDTO), currentContext(), rationale);

		return datasetDTOService.createDTO(scope, event, dataset, acl);
	}

	@Operation(summary = "Remove dataset")
	@PutMapping({ "events/{eventPk}/datasets/{datasetPk}/remove", "datasets/{datasetPk}/remove" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public DatasetDTO removeDataset(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long datasetPk,
		@RequestParam final String rationale
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);
		URLConsistencyUtils.checkConsistency(scope, event, dataset);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(dataset.getDatasetModel(), Rights.WRITE);

		// Remove dataset
		datasetService.delete(scope, event, dataset, currentContext(), rationale);

		return datasetDTOService.createDTO(scope, event, dataset, acl);
	}

	@Operation(summary = "Restore dataset")
	@PutMapping({ "events/{eventPk}/datasets/{datasetPk}/restore", "datasets/{datasetPk}/restore" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public DatasetDTO restoreDataset(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long datasetPk,
		@RequestParam final String rationale
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);
		URLConsistencyUtils.checkConsistency(scope, event, dataset);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(dataset.getDatasetModel(), Rights.WRITE);

		//check rights
		acl.checkRight(FeatureStatic.MANAGE_DELETED_DATA);
		acl.checkRight(dataset.getDatasetModel(), Rights.WRITE);

		datasetService.restore(scope, event, dataset, currentContext(), rationale);

		return datasetDTOService.createDTO(scope, event, dataset, acl);
	}

	@Operation(summary = "Save multiple datasets")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "400", description = "A blocking error has occurred", content = {
			@Content(schema = @Schema(implementation = BlockingErrorsDTO.class))
		})
	})
	@PutMapping({ "datasets/batch", "events/{eventPk}/datasets/batch" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional(rollbackFor = DatasetSubmissionException.class)
	public Set<DatasetDTO> saveScopeDatasets(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@Valid @RequestBody final Set<DatasetUpdateDTO> datasetDTOs,
		@RequestHeader("X-Rationale") final Optional<String> rationale
	) throws DatasetSubmissionException {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		final var acl = rightsService.getACL(currentActor(), scope);

		//rights will be checked when saving datasets
		final var datasets = datasetSubmissionService.saveDatasets(acl, scope, event, datasetDTOs, currentContext(), rationale);

		return datasets.stream()
			.map(d -> datasetDTOService.createDTO(scope, event, d, acl))
			.collect(Collectors.toSet());
	}

	//remove pk for the candidate dataset and its fields
	private void cleanPksFromDataset(final DatasetDTO datasetDTO) {
		datasetDTO.setPk(null);
		datasetDTO.fields.forEach(f -> {
			f.setPk(null);
			f.setDatasetPk(null);
		});
	}
}
