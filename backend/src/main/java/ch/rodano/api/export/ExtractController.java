package ch.rodano.api.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.config.ConfigDTOService;
import ch.rodano.api.config.DatasetModelDTO;
import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.export.extract.ExtractService;
import ch.rodano.core.services.bll.export.report.ReportService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Extract", description = "Extract data")
@RestController
@RequestMapping("extracts")
@Transactional(readOnly = true)
public class ExtractController extends AbstractSecuredController {
	private static final String ZIPPED_SPECIFICATIONS_FILENAME = "specifications.zip";
	private static final String ZIPPED_EXTRACTS_FILENAME = "extracts.zip";

	private final ExtractService extractService;
	private final ReportService reportService;
	private final ConfigDTOService configDTOService;
	private final ScopeDAOService scopeDAOService;

	public ExtractController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ExtractService extractService,
		final ConfigDTOService configDTOService,
		final ScopeDAOService scopeDAOService,
		final ReportService reportService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.extractService = extractService;
		this.reportService = reportService;
		this.configDTOService = configDTOService;
		this.scopeDAOService = scopeDAOService;
	}

	@Operation(summary = "Get all dataset models that are exportable for a scope model")
	@GetMapping("dataset-models")
	@ResponseStatus(HttpStatus.OK)
	public List<DatasetModelDTO> getDatasetModelsPerScopeModel(
		@Parameter(description = "Scope model ID") @RequestParam final String scopeModelId
	) {
		final var scopeModel = studyService.getStudy().getScopeModel(scopeModelId);

		final var acl = rightsService.getACL(currentActor());
		acl.checkRight(scopeModel, Rights.READ);

		final Set<DatasetModel> datasetModels = new TreeSet<>();

		//add dataset models linked directly to the scope model
		scopeModel.getDatasetModels().stream()
			.filter(d -> d.isExportable() && acl.hasRight(d, Rights.READ))
			.forEach(datasetModels::add);

		//add dataset models linked directly to the scope model event models
		for(final var eventModel : scopeModel.getEventModels()) {
			eventModel.getDatasetModels().stream()
				.filter(d -> d.isExportable() && acl.hasRight(d, Rights.READ))
				.forEach(datasetModels::add);
		}

		return datasetModels.stream().map(d -> configDTOService.createDatasetModelDTO(d, acl)).toList();
	}

	@Operation(summary = "Export dataset specifications to CSV or ZIP")
	@GetMapping(value = "specifications")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> exportSpecifications(
		@Parameter(description = """
			Dataset model IDs.
			If only one is provided, a CSV file is returned.
			If more than one ID is provided, a ZIP file containing individual specifications is returned.
			""") @RequestParam final List<String> datasetModelIds,
		@Parameter(description = "Should the modification dates be included ?") @RequestParam final Optional<Boolean> withModificationDates
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		// Check rights on export
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.EXPORT);

		// Check document id
		final var datasetModels = datasetModelIds.stream()
			.map(datasetModelId -> studyService.getStudy().getDatasetModel(datasetModelId))
			.toList();

		for(final var datasetModel : datasetModels) {
			// TODO check rights even for contributions
			if(!datasetModel.isContribution()) {
				rightsService.checkRight(currentActor, currentRoles, datasetModel, Rights.READ);

				if(!datasetModel.isExportable()) {
					throw new UnauthorizedException(String.format("Dataset model %s is not exportable", datasetModel.getId()));
				}
			}
		}

		if(datasetModels.size() == 1) {
			final var document = datasetModels.get(0);
			final StreamingResponseBody stream = os -> reportService.getDataStructure(
				os,
				document,
				withModificationDates.orElse(false),
				actorService.getLanguages(currentActor)
			);
			final var filename = reportService.getDataStructureFilename(document);
			return exportResponse(ExportFormat.CSV, stream, filename);
		}
		final StreamingResponseBody stream = os -> reportService.zipDataStructures(
			os,
			datasetModels,
			withModificationDates.orElse(false),
			actorService.getLanguages(currentActor)
		);
		final var filename = ZIPPED_SPECIFICATIONS_FILENAME;
		return exportResponse(ExportFormat.ZIP, stream, filename);
	}

	@Operation(summary = "Extract dataset to CSV or ZIP")
	@GetMapping()
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> extract(
		@Parameter(description = """
			Dataset model IDs.
			If only one is provided, a CSV file is returned.
			If more than one ID is provided, a ZIP file containing individual specifications is returned.
			""") @RequestParam final List<String> datasetModelIds,
		@Parameter(description = "Scope reference on which the export will be performed") @RequestParam final Optional<Long> scopePk,
		@Parameter(description = "Should the modification dates be included ?") @RequestParam final Optional<Boolean> withModificationDates
	) {
		final var currentActor = currentActor();
		var currentRoles = currentActiveRoles();
		final var study = studyService.getStudy();

		// Check document id
		final var datasetModels = datasetModelIds.stream()
			.map(datasetModelId -> studyService.getStudy().getDatasetModel(datasetModelId))
			.toList();

		for(final var document : datasetModels) {
			// TODO check rights even for contributions
			if(!document.isContribution()) {
				rightsService.checkRight(currentActor, currentRoles, document, Rights.READ);

				if(!document.isExportable()) {
					throw new UnauthorizedException(String.format("Dataset model %s is not exportable", document.getId()));
				}
			}
		}

		//use the scope pk provided in the URL or retrieve root scopes of the user
		final Collection<Scope> scopes = new ArrayList<>();
		if(scopePk.isPresent()) {
			final var scope = scopeDAOService.getScopeByPk(scopePk.get());
			currentRoles = currentActiveRoles(scope);
			rightsService.checkRight(currentActor, currentRoles, FeatureStatic.EXPORT);
			scopes.add(scope);
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor, FeatureStatic.EXPORT));
		}

		//extract the selected dataset model
		if(datasetModels.size() == 1) {
			final var document = datasetModels.get(0);
			final StreamingResponseBody stream = os -> extractService.getDataExtract(
				os,
				document,
				study.getDefaultLanguageId(),
				scopes,
				withModificationDates.orElse(false)
			);
			final var filename = extractService.getCSVDocumentFilename(document);
			return exportResponse(ExportFormat.CSV, stream, filename);
		}
		//extract multiple dataset models
		final StreamingResponseBody stream = os -> extractService.zipExtracts(
			os,
			datasetModels,
			study.getDefaultLanguageId(),
			scopes,
			withModificationDates.orElse(false)
		);
		final var filename = ZIPPED_EXTRACTS_FILENAME;
		return exportResponse(ExportFormat.ZIP, stream, filename);
	}

}
