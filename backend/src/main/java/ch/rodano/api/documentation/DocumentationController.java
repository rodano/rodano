package ch.rodano.api.documentation;

import java.util.Collections;
import java.util.Map;

import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.utils.ExportableUtils;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.export.dct.CRFDocumentationGenerationStatus;
import ch.rodano.core.services.bll.export.dct.CRFDocumentationService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Profile("!migration")
@Tag(name = "Documentation")
@RestController
@RequestMapping("/documentation")
@Transactional(readOnly = true)
public class DocumentationController extends AbstractSecuredController {

	private final CRFDocumentationService crfDocumentationService;
	private final ScopeService scopeService;
	private final ScopeDAOService scopeDAOService;

	public DocumentationController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final CRFDocumentationService crfDocumentationService,
		final ScopeService scopeService,
		final ScopeDAOService scopeDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.crfDocumentationService = crfDocumentationService;
		this.scopeService = scopeService;
		this.scopeDAOService = scopeDAOService;
	}

	@Operation(summary = "Export blank CRF in PDF format")
	@GetMapping("/crf-blank/{scopeModelId}")
	@Transactional
	public ResponseEntity<StreamingResponseBody> crfBlank(
		@PathVariable final String scopeModelId,
		@Parameter(description = "Annotated?") @RequestParam final boolean annotated
	) {
		final var scopeModel = studyService.getStudy().getScopeModel(scopeModelId);

		final var currentActor = currentActor();
		final var currentRoles = roleService.getActiveRoles(currentActor, scopeService.getRootScope());
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.DOCUMENTATION);
		rightsService.checkRight(currentActor, currentRoles, scopeModel, Rights.READ);

		final StreamingResponseBody stream = os -> crfDocumentationService.generateCRFBlank(currentActor, scopeModel, annotated, os);
		final var basename = annotated ? "annotated_blank_crf" : "blank_crf";
		final var filename = studyService.getStudy().generateFilename(basename, ExportFormat.PDF);
		return fileResponse(stream, MediaType.APPLICATION_PDF, filename);
	}

	@Operation(summary = "Export archive CRF in PDF format for one scope")
	@GetMapping("/crf-archive/{scopePk}")
	public ResponseEntity<StreamingResponseBody> crfOneScopeArchive(
		@PathVariable final Long scopePk,
		@Parameter(description = "With audit trails?") @RequestParam final boolean auditTrails
	) {
		final var currentActor = currentActor();
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		//check rights
		final var roles = currentActiveRoles(scope);
		rightsService.checkRight(currentActor, roles, FeatureStatic.DOCUMENTATION);
		rightsService.checkRight(currentActor, roles, scope.getScopeModel(), Rights.READ);

		// Generate the CRF archive
		final StreamingResponseBody stream = os -> crfDocumentationService.generateCRFArchive(currentActor, scope, auditTrails, os);
		final var filename = crfDocumentationService.generateCRFArchiveFilename(studyService.getStudy(), scope);
		return fileResponse(stream, MediaType.APPLICATION_PDF, filename);
	}

	@Operation(summary = "Export archive CRF in PDF format for multiple scopes")
	@PostMapping("/crf-archive/request")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Map<String, String> crfArchive(
		@Valid @RequestBody final ArchiveRequestDTO request
	) {
		final var currentActor = currentActor();
		final var scopeModel = studyService.getStudy().getScopeModel(request.scopeModelId());
		final var scopes = scopeDAOService.getScopesByPks(request.scopePks());

		//check rights
		for(final var scope : scopes) {
			final var roles = currentActiveRoles(scope);
			rightsService.checkRight(currentActor, roles, FeatureStatic.DOCUMENTATION);
			rightsService.checkRight(currentActor, roles, scopeModel, Rights.READ);
		}

		//do not allow multiple generation at the same time
		if(CRFDocumentationGenerationStatus.IN_PROGRESS.equals(crfDocumentationService.getCRFArchiveGenerationStatus())) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "There is already one running archive request");
		}

		final var generationId = crfDocumentationService.generateCRFArchive(currentActor, scopeModel, scopes, request.auditTrails());
		return Collections.singletonMap("id", generationId);
	}

	@Operation(summary = "Get the current status of the CRF archive generation")
	@GetMapping("/crf-archive/status")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, CRFDocumentationGenerationStatus> getCRFArchiveStatus() {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scopeService.getRootScope());
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.DOCUMENTATION);

		return Collections.singletonMap("status", crfDocumentationService.getCRFArchiveGenerationStatus());
	}

	@Operation(summary = "Get the latest CRF archive in zip form")
	@GetMapping("/crf-archive/download")
	public ResponseEntity<StreamingResponseBody> getCRFArchiveZip() {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scopeService.getRootScope());

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.DOCUMENTATION);

		final StreamingResponseBody stream = crfDocumentationService::streamCRFArchive;
		return fileResponse(stream, MediaType.valueOf("application/zip"), crfDocumentationService.getCRFArchiveFilename());
	}

	@Operation(summary = "Generate the data structure")
	@GetMapping("/data-structure")
	public ResponseEntity<StreamingResponseBody> getDataStructure() {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scopeService.getRootScope());

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.DOCUMENTATION);

		final var study = studyService.getStudy();

		//find exportable dataset models
		final var datasetModels = study.getDatasetModels().stream()
			.filter(DatasetModel::isExportable)
			.toList();

		final StreamingResponseBody stream = os -> ExportableUtils.getDataStructure(os, datasetModels, false, study.getDefaultLanguage().getId());

		//generate filename
		final var versionNumber = StringUtils.defaultIfBlank(study.getVersionNumber(), "").replace(" ", "_");
		final var versionDate = StringUtils.defaultIfBlank(study.getVersionDate(), "").replace(" ", "_");
		final var studyLabel = StringUtils.replace(study.getDefaultLocalizedShortname().toLowerCase(), " ", "_");
		//do not user the filename generator from the study because this file must contain the date of the version
		final var filename = String.format("%s_data_structure_%s_%s.csv", studyLabel, versionNumber, versionDate);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}
}
