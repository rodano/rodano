package ch.rodano.api.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.cms.CMSDTOService;
import ch.rodano.api.cms.CMSLayoutDTO;
import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.api.workflow.WorkflowDTOService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.configuration.core.Configurator;
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
	private final ProjectHeaderValidator projectHeaderValidator;

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
		final Configurator configurator,
		final ProjectHeaderValidator projectHeaderValidator
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.studyDTOService = studyDTOService;
		this.menuDTOService = menuDTOService;
		this.cmsDTOService = cmsDTOService;
		this.workflowDTOService = workflowDTOService;
		this.configDTOService = configDTOService;
		this.configurator = configurator;
		this.projectHeaderValidator = projectHeaderValidator;
	}

	@Operation(summary = "Get study")
	//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
	@GetMapping("study")
	@ResponseStatus(HttpStatus.OK)
	public StudyDTO getStudy(@RequestHeader("X-Project-Id") final UUID projectId) {
		projectHeaderValidator.validate(projectId);

		final var acl = rightsService.getACL(currentActor());
		return studyDTOService.createStudyDTO(studyService.getStudy(), acl);
	}

	@SecurityRequirements
	@Operation(summary = "Get public study")
	//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
	@GetMapping("public-study")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<PublicStudyDTO> getPublicStudy(@RequestHeader(value = "X-Project-Id", required = false) final UUID projectId) {
		if(!studyService.isStudyLoaded()) {
			return ResponseEntity.noContent().build();
		}

		if(projectId != null) {
			projectHeaderValidator.validate(projectId);
		}

		return ResponseEntity.ok(studyDTOService.createPublicStudyDTO(studyService.getStudy()));
	}

	@Operation(summary = "Get the study menus", description = "Get menus as defined in the study configuration")
	@GetMapping("menu")
	@ResponseStatus(HttpStatus.OK)
	public List<MenuDTO> getMenus(@RequestHeader("X-Project-Id") final UUID projectId) {
		projectHeaderValidator.validate(projectId);

		final var acl = rightsService.getACL(currentActor());

		return studyService.getStudy().getMenus().stream()
			.filter(acl::hasRight)
			.map(m -> menuDTOService.createDTO(m, acl))
			.toList();
	}

	@Operation(summary = "Get the study event models")
	@GetMapping("event-models")
	@ResponseStatus(HttpStatus.OK)
	public List<EventModelDTO> getEventModels(@RequestHeader("X-Project-Id") final UUID projectId) {
		projectHeaderValidator.validate(projectId);

		final var acl = rightsService.getACL(currentActor());

		return studyService.getStudy().getEventModels().stream()
			.filter(e -> acl.hasRight(e, Rights.READ))
			.map(EventModelDTO::new)
			.toList();
	}

	@Operation(summary = "Get the study field models")
	@GetMapping("field-models")
	@ResponseStatus(HttpStatus.OK)
	public List<FieldModelDTO> getFieldModels(@RequestHeader("X-Project-Id") final UUID projectId) {
		projectHeaderValidator.validate(projectId);

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
	public List<FieldModelDTO> getSearchableFieldModels(@RequestHeader("X-Project-Id") final UUID projectId) {
		projectHeaderValidator.validate(projectId);

		final var acl = rightsService.getACL(currentActor());
		final var languages = actorService.getLanguages(acl.actor());

		return studyService.getStudy().getSearchableFieldModels().stream()
			.filter(f -> acl.hasRight(f.getDatasetModel(), Rights.READ))
			.map(f -> new FieldModelDTO(f, languages))
			.toList();
	}

	@Operation(summary = "Get the study workflow models")
	@GetMapping("workflows")
	public List<WorkflowDTO> getWorkflows(@RequestHeader("X-Project-Id") final UUID projectId) {
		projectHeaderValidator.validate(projectId);

		final var acl = rightsService.getACL(currentActor());

		return studyService.getStudy().getWorkflows().stream()
			.filter(acl::hasRight)
			.map(w -> workflowDTOService.createWorkflowDTO(w, acl))
			.toList();
	}

	@Operation(summary = "Get form models for a scope model")
	@GetMapping("/scope-model/{scopeModelId}/form-models")
	@ResponseStatus(HttpStatus.OK)
	public List<FormModelDTO> getFormModels(
		@RequestHeader("X-Project-Id") final UUID projectId,
		@PathVariable final UUID scopeModelId
	) {
		projectHeaderValidator.validate(projectId);

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
		@RequestHeader("X-Project-Id") final UUID projectId,
		@PathVariable final UUID scopeModelId
	) {
		projectHeaderValidator.validate(projectId);

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
		@RequestHeader("X-Project-Id") final UUID projectId,
		@PathVariable final UUID menuId
	) {
		projectHeaderValidator.validate(projectId);

		final var roles = currentActiveRoles();
		final var menu = studyService.getStudy().getAllMenu(menuId);
		return cmsDTOService.createLayoutDTO(menu.getLayout(), roles);
	}

	@Operation(summary = "Get field model autocomplete options")
	@GetMapping("dataset-models/{datasetModelId}/field-models/{fieldModelId}/autocomplete/{text}")
	@ResponseStatus(HttpStatus.OK)
	public List<String> getFieldModelAutocomplete(
		@RequestHeader("X-Project-Id") final UUID projectId,
		@PathVariable final UUID datasetModelId,
		@PathVariable final UUID fieldModelId,
		@PathVariable final String text
	) throws IOException {
		projectHeaderValidator.validate(projectId);

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
	public Set<ResourceCategoryDTO> getCategories(@RequestHeader("X-Project-Id") final UUID projectId) {
		projectHeaderValidator.validate(projectId);

		final var currentRoles = currentActiveRoles();
		return currentRoles.stream()
			.map(Role::getProfile)
			.flatMap(p -> p.getResourceCategories().stream())
			.distinct()
			.map(ResourceCategoryDTO::new)
			.collect(Collectors.toSet());
	}
}
