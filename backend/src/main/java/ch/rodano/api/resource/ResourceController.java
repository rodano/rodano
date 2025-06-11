package ch.rodano.api.resource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.exception.http.BadArgumentException;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.resource.ResourceSearch;
import ch.rodano.core.model.resource.ResourceSortBy;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.resource.ResourceService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Resources")
@RestController
@RequestMapping("/resources")
@Validated
@Transactional(readOnly = true)
public class ResourceController extends AbstractSecuredController {
	private final ScopeService scopeService;
	private final ResourceService resourceService;
	private final ResourceDTOService resourceDTOService;
	private final UtilsService utilsService;
	private final ScopeDAOService scopeDAOService;

	private final Integer defaultPageSize;

	public ResourceController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ScopeService scopeService,
		final ResourceService resourceService,
		final ResourceDTOService resourceDTOService,
		final UtilsService utilsService,
		final ScopeDAOService scopeDAOService,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.scopeService = scopeService;
		this.resourceService = resourceService;
		this.resourceDTOService = resourceDTOService;
		this.utilsService = utilsService;
		this.scopeDAOService = scopeDAOService;
		this.defaultPageSize = defaultPageSize;
	}

	// TODO maybe separate public and private resource endpoints
	// TODO the reasoning is that we can't assign different @SecurityRequirements if both endpoints are on the
	// TODO same resource
	@Operation(summary = "Search resources")
	// Warning : if you change this API endpoint, do not forget to change it in the security configuration !
	@GetMapping(value = { "", "public" })
	@ResponseStatus(HttpStatus.OK)
	public PagedResult<ResourceDTO> search(
		@Parameter(description = "Category ID of the resource") @RequestParam final Optional<String> categoryId,
		@Parameter(description = "Full text search on title and description") @RequestParam final Optional<String> fullText,
		@Parameter(description = "Has the resource been removed ?") @RequestParam final Optional<Boolean> removed,
		@Parameter(description = "Sort the results by which property?") @RequestParam final Optional<ResourceSortBy> sortBy,
		@Parameter(description = "Use the ascending order?") @RequestParam final Optional<Boolean> orderAscending,
		@Parameter(description = "Page size") @RequestParam final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam final Optional<Integer> pageIndex
	) {

		final Optional<Actor> currentActor;
		final Optional<List<Role>> currentRoles;

		// If there is no authentication, this is a public request
		if(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			currentActor = Optional.empty();
			currentRoles = Optional.empty();
		}
		else {
			currentActor = Optional.of(currentActor());
			currentRoles = currentActor.map(roleService::getActiveRoles);
		}

		final var hasRightToSeeDeleted = currentRoles.isPresent() && rightsService.hasRight(currentRoles.get(), FeatureStatic.MANAGE_DELETED_DATA);

		final var search = new ResourceSearch()
			.setCategoryId(categoryId.filter(StringUtils::isNotBlank))
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			.setIncludeDeleted(hasRightToSeeDeleted && removed.orElse(false))
			.setPageSize(pageSize.isEmpty() ? Optional.of(defaultPageSize) : pageSize)
			.setPageIndex(pageIndex.isEmpty() ? Optional.of(0) : pageIndex);
		//set sort if provided
		sortBy.map(search::setSortBy);
		orderAscending.map(search::setSortAscending);

		//find root scopes for actor
		if(currentActor.isPresent()) {
			final var rootScopesPks = actorService.getRootScopes(currentActor.get()).stream().map(Scope::getPk).toList();
			search.setReferenceScopePks(Optional.of(rootScopesPks));
		}
		else {
			search.setOnlyPublic(Optional.of(true));
		}

		return resourceService.search(search).withObjectTransformation(r -> resourceDTOService.createDTO(r, currentActor, currentRoles));
	}

	@Operation(summary = "Create a resource")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public ResourceDTO createResource(
		@Valid @RequestBody final ResourceSubmissionDTO resourceDTO
	) {
		//check resource is valid
		final var scope = scopeDAOService.getScopeByPk(resourceDTO.getScopePk());

		//check rights
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);
		rightsService.checkRight(currentActor(), currentRoles, scope);
		rightsService.checkRight(currentActor(), currentRoles, FeatureStatic.MANAGE_RESOURCE);

		//create resource
		final var resource = resourceDTOService.generateResource(resourceDTO);
		final var newResource = resourceService.createResource(resource, currentActor(), currentContext());

		return resourceDTOService.createDTO(newResource, Optional.ofNullable(currentActor), Optional.ofNullable(currentRoles));
	}

	@Operation(summary = "Update a resource")
	@PutMapping("{resourcePk}")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ResourceDTO updateResource(
		@PathVariable final Long resourcePk,
		@Valid @RequestBody final ResourceDTO resourceDTO
	) {
		// TODO remove these checks when the spring validation is integrated
		if(resourceDTO.isPublicResource() == null) {
			throw new BadArgumentException("Resource must be public or private.");
		}

		//retrieve resource
		final var resource = resourceService.getResourceByPk(resourcePk);
		utilsService.checkNotNull(Resource.class, resource, resourcePk);

		final var currentActor = currentActor();

		//check rights on the resource as it is before the update (using its current scope)
		final var oldScope = scopeService.get(resource);
		var currentRoles = currentActiveRoles(oldScope);
		rightsService.checkRight(currentActor, currentRoles, oldScope);
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_RESOURCE);

		//check rights on the new scope if it has been updated
		if(!oldScope.getPk().equals(resourceDTO.getScopePk())) {
			final var scope = scopeDAOService.getScopeByPk(resourceDTO.getScopePk());
			//update current roles so it can be used when generating the resulting DTO
			currentRoles = roleService.getActiveRoles(currentActor, scope);
			rightsService.checkRight(currentActor, currentRoles, scope);
			rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_RESOURCE);
		}

		//update the existing resource with the data from the DTO
		resourceDTOService.updateResource(resource, resourceDTO);
		resourceService.saveResource(resource, currentContext(), "Update resource");

		return resourceDTOService.createDTO(resource, Optional.of(currentActor), Optional.of(currentRoles));
	}

	@Operation(summary = "Restore resource")
	@PutMapping("{resourcePk}/restore")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ResourceDTO restoreResource(
		@PathVariable final Long resourcePk
	) {
		final var currentActor = currentActor();

		final var resource = resourceService.getResourceByPk(resourcePk);
		utilsService.checkNotNull(Resource.class, resource, resourcePk);

		final var currentRoles = currentActiveRoles(scopeService.get(resource));
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_RESOURCE);

		resourceService.restoreResource(resource, currentContext(), "Restore resource");

		return resourceDTOService.createDTO(resource, Optional.ofNullable(currentActor), Optional.ofNullable(currentRoles));
	}

	@Operation(summary = "Remove resource")
	@PutMapping("{resourcePk}/remove")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ResourceDTO removeResource(
		@PathVariable final Long resourcePk
	) {
		final var currentActor = currentActor();

		final var resource = resourceService.getResourceByPk(resourcePk);
		utilsService.checkNotNull(Resource.class, resource, resourcePk);

		final var currentRoles = currentActiveRoles(scopeService.get(resource));
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_RESOURCE);

		resourceService.deleteResource(resource, currentContext(), "Remove resource");

		return resourceDTOService.createDTO(resource, Optional.ofNullable(currentActor), Optional.ofNullable(currentRoles));
	}

	@Operation(summary = "Upload a resource file")
	@PostMapping(value = "{resourcePk}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public ResourceDTO uploadResourceFile(
		@PathVariable final Long resourcePk,
		@RequestParam final MultipartFile file
	) throws IOException {
		final var currentActor = currentActor();
		final var resource = resourceService.getResourceByPk(resourcePk);

		utilsService.checkNotNull(Resource.class, resource, resourcePk);

		final var currentRoles = currentActiveRoles(scopeService.get(resource));
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_RESOURCE);

		final var filename = file.getOriginalFilename();
		try(final var fileStream = file.getInputStream()) {
			final var updatedResource = resourceService.attachFileToResource(
				resource,
				filename,
				fileStream,
				currentContext(),
				"Attach a file to the resource"
			);
			return resourceDTOService.createDTO(
				updatedResource,
				Optional.ofNullable(currentActor),
				Optional.ofNullable(currentRoles)
			);
		}

	}

	@Operation(summary = "Download a resource file")
	// Warning : if you change this API endpoint, do not forget to change it in the security configuration !
	@GetMapping(value = { "{resourcePk}/file", "/public/{resourcePk}/file" })
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> downloadResourceFile(
		@PathVariable final Long resourcePk
	) {
		final var resource = resourceService.getResourceByPk(resourcePk);

		utilsService.checkNotNull(Resource.class, resource, resourcePk);

		if(!resource.getPublicResource()) {
			//only if resource is not public, check for roles
			//take all roles, not only roles that gives rights right to resource's scope
			//that's because resources are "propagated" through the scope tree
			//a user may not have the right on the resource's scope but on a descendant of the resource's scope
			final var currentActor = currentActor();
			final var currentRoles = currentActiveRoles();
			rightsService.checkRightToRead(currentActor, currentRoles, resource);
		}

		final StreamingResponseBody stream = out -> resourceService.getResourceFile(out, resource);
		return fileResponse(stream, MediaType.APPLICATION_OCTET_STREAM, resource.getFilename());
	}
}
