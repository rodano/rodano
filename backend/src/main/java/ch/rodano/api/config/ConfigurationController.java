package ch.rodano.api.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.cms.CMSDTOService;
import ch.rodano.api.cms.CMSLayoutDTO;
import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.exception.http.ForbiddenOperationException;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.api.workflow.WorkflowDTOService;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.configuration.core.Environment;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Configuration")
@RestController
@RequestMapping("/config")
@Transactional(readOnly = true)
public class ConfigurationController extends AbstractSecuredController {

	private final StudyDTOService studyDTOService;
	private final MenuDTOService menuDTOService;
	private final CMSDTOService cmsDTOService;
	private final WorkflowDTOService workflowDTOService;
	private final ConfigDTOService configDTOService;
	private final Configurator configurator;

	public ConfigurationController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final StudyDTOService studyDTOService,
		final MenuDTOService menuDTOService,
		final CMSDTOService cmsDTOService,
		final WorkflowDTOService workflowDTOService,
		final ConfigDTOService configDTOService,
		final Configurator configurator
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.studyDTOService = studyDTOService;
		this.menuDTOService = menuDTOService;
		this.cmsDTOService = cmsDTOService;
		this.workflowDTOService = workflowDTOService;
		this.configDTOService = configDTOService;
		this.configurator = configurator;
	}

	/**
	 * Pull the current configuration
	 *
	 */
	@Operation(summary = "Pull the study configuration")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> pull() {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_CONFIGURATION);

		final StreamingResponseBody stream = os -> studyService.read(os);
		return streamResponse(stream, MediaType.APPLICATION_JSON);
	}

	/**
	 * Push a new configuration to the application
	 *
	 */
	@Operation(summary = "Push the study configuration")
	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void push(
		@RequestParam final MultipartFile config,
		@RequestParam final boolean compressed
	) throws IOException {
		if(Environment.PROD.equals(configurator.getEnvironment())) {
			throw new ForbiddenOperationException("No right to push the configuration on a production instance");
		}

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_CONFIGURATION);

		try(var is = config.getInputStream()) {
			studyService.save(is, compressed);
		}
	}

	@Operation(summary = "Get study")
	//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
	@GetMapping("study")
	@ResponseStatus(HttpStatus.OK)
	public StudyDTO getStudy() {
		final var acl = rightsService.getACL(currentActor());
		return studyDTOService.createStudyDTO(studyService.getStudy(), acl);
	}

	@SecurityRequirements
	@Operation(summary = "Get public study")
	//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
	@GetMapping("public-study")
	@ResponseStatus(HttpStatus.OK)
	public PublicStudyDTO getPublicStudy() {
		return studyDTOService.createPublicStudyDTO(studyService.getStudy());
	}

	@Operation(summary = "Get the study menus", description = "Get menus as defined in the study configuration")
	@GetMapping("menu")
	@ResponseStatus(HttpStatus.OK)
	public List<MenuDTO> getMenus() {
		final var acl = rightsService.getACL(currentActor());

		return studyService.getStudy().getMenus().stream()
			.filter(m -> acl.hasRight(m))
			.map(m -> menuDTOService.createDTO(m, acl))
			.toList();
	}

	@Operation(summary = "Get the study event models")
	@GetMapping("event-models")
	@ResponseStatus(HttpStatus.OK)
	public List<EventModelDTO> getEventModels() {
		final var acl = rightsService.getACL(currentActor());

		return studyService.getStudy().getEventModels().stream()
			.filter(e -> acl.hasRight(e, Rights.READ))
			.map(EventModelDTO::new)
			.toList();
	}

	@Operation(summary = "Get the study field models")
	@GetMapping("field-models")
	@ResponseStatus(HttpStatus.OK)
	public List<FieldModelDTO> getFieldModels() {
		final var acl = rightsService.getACL(currentActor());
		final var languages = actorService.getLanguages(acl.actor());

		return studyService.getStudy().getFieldModels().stream()
			.filter(f -> acl.hasRight(f.getDatasetModel(), Rights.READ))
			.map(f -> new FieldModelDTO(f, languages))
			.toList();
	}

	@Operation(summary = "Get searchable field models")
	@GetMapping("searchable-field-models")
	@ResponseStatus(HttpStatus.OK)
	public List<FieldModelDTO> getSearchableFieldModels() {
		final var acl = rightsService.getACL(currentActor());
		final var languages = actorService.getLanguages(acl.actor());

		return studyService.getStudy().getSearchableFieldModels().stream()
			.filter(f -> acl.hasRight(f.getDatasetModel(), Rights.READ))
			.map(f -> new FieldModelDTO(f, languages))
			.toList();
	}

	@Operation(summary = "Get the study workflow models")
	@GetMapping("workflows")
	public List<WorkflowDTO> getWorkflows() {
		final var acl = rightsService.getACL(currentActor());

		return studyService.getStudy().getWorkflows().stream()
			.filter(w -> acl.hasRight(w))
			.map(w -> workflowDTOService.createWorkflowDTO(w, acl))
			.toList();
	}

	@Operation(summary = "Get form models for a scope model")
	@GetMapping("/scope-model/{scopeModelId}/form-models")
	@ResponseStatus(HttpStatus.OK)
	public List<FormModelDTO> getFormModels(
		@PathVariable final String scopeModelId
	) {
		final var acl = rightsService.getACL(currentActor());

		return studyService.getStudy().getScopeModel(scopeModelId).getFormModels().stream()
			.filter(p -> acl.hasRight(p, Rights.READ))
			.map(FormModelDTO::new)
			.toList();
	}

	@Operation(summary = "Get dataset models for a scope model")
	@GetMapping("/scope-model/{scopeModelId}/dataset-models")
	@ResponseStatus(HttpStatus.OK)
	public List<DatasetModelDTO> getInceptiveDatasetModels(
		@PathVariable final String scopeModelId
	) {
		final var acl = rightsService.getACL(currentActor());

		return studyService.getStudy().getScopeModel(scopeModelId).getDatasetModels().stream()
			.filter(p -> acl.hasRight(p, Rights.READ))
			.map(d -> configDTOService.createDatasetModelDTO(d, acl))
			.toList();
	}

	@Operation(summary = "Get menu layout")
	@GetMapping("menu/{menuId}/layout")
	@ResponseStatus(HttpStatus.OK)
	public CMSLayoutDTO getMenuLayout(
		@PathVariable final String menuId
	) {
		final var roles = currentActiveRoles();
		final var menu = studyService.getStudy().getAllMenu(menuId);
		return cmsDTOService.createLayoutDTO(menu.getLayout(), roles);
	}

	@Operation(summary = "Get field model autocomplete options")
	@GetMapping("dataset-models/{datasetModelId}/field-models/{fieldModelId}/autocomplete/{text}")
	@ResponseStatus(HttpStatus.OK)
	public List<String> getFieldModelAutocomplete(
		@PathVariable final String datasetModelId,
		@PathVariable final String fieldModelId,
		@PathVariable final String text
	) throws IOException {
		final var fieldModel = studyService.getStudy().getDatasetModel(datasetModelId).getFieldModel(fieldModelId);
		final var dictionary = fieldModel.getDictionary();
		final List<String> results = new ArrayList<>();
		if(StringUtils.isNotBlank(text) && text.length() > 1) {
			final var search = text.toLowerCase();
			var i = 0;
			try(var scan = new Scanner(ConfigurationController.class.getResource(String.format("/dictionaries/%s", dictionary)).openStream())) {
				while(scan.hasNext() && i < 100) {
					final var line = scan.nextLine();
					if(line.toLowerCase().contains(search)) {
						results.add(line);
						i++;
					}
				}
			}
		}
		return results;
	}

	@Operation(summary = "Get all available resource categories")
	@GetMapping("resource-categories")
	@ResponseStatus(HttpStatus.OK)
	public Set<ResourceCategoryDTO> getCategories() {
		final var currentRoles = currentActiveRoles();
		return currentRoles.stream()
			.map(Role::getProfile)
			.flatMap(p -> p.getResourceCategories().stream())
			.distinct()
			.map(ResourceCategoryDTO::new)
			.collect(Collectors.toSet());
	}
}
