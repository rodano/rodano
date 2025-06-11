package ch.rodano.api.resource;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ch.rodano.api.config.ResourceCategoryDTO;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.utils.RightsService;

@Service
public class ResourceDTOServiceImpl implements ResourceDTOService {

	private final StudyService studyService;
	private final ScopeService scopeService;
	private final UserService userService;
	private final RightsService rightsService;

	public ResourceDTOServiceImpl(
		final StudyService studyService,
		final ScopeService scopeService,
		final UserService userService,
		final RightsService rightsService
	) {
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.userService = userService;
		this.rightsService = rightsService;
	}

	@Override
	public ResourceDTO createDTO(final Resource resource, final Optional<Actor> actor, final Optional<List<Role>> roles) {
		final var dto = new ResourceDTO();
		dto.pk = resource.getPk();
		dto.creationTime = resource.getCreationTime();
		dto.lastUpdateTime = resource.getLastUpdateTime();
		dto.title = resource.getTitle();
		dto.description = resource.getDescription();
		dto.filename = resource.getFilename();

		dto.categoryId = resource.getCategoryId();
		final var resourceCategory = studyService.getStudy().getResourceCategory(resource.getCategoryId());
		dto.category = new ResourceCategoryDTO(resourceCategory);

		dto.removed = resource.getDeleted();

		dto.publicResource = resource.getPublicResource();

		dto.canBeManaged = actor.isPresent() && rightsService.hasRight(roles.get(), FeatureStatic.MANAGE_RESOURCE);

		final var author = userService.getUser(resource);
		dto.userPk = author.getPk();
		dto.userName = author.getName();
		dto.userEmail = author.getEmail();

		final var scope = scopeService.get(resource);
		dto.scopePk = scope.getPk();
		dto.scopeShortname = scope.getShortname();

		return dto;
	}

	@Override
	public Resource generateResource(final ResourceSubmissionDTO resourceDTO) {
		final var resource = new Resource();
		resource.setTitle(resourceDTO.getTitle());
		resource.setDescription(resourceDTO.getDescription());
		resource.setScopeFk(resourceDTO.getScopePk());
		resource.setCategoryId(resourceDTO.getCategoryId());
		//do not set filename here
		//it will be set when a file is attached to the resource
		resource.setPublicResource(resourceDTO.isPublicResource());
		return resource;
	}

	@Override
	public void updateResource(final Resource resource, final ResourceSubmissionDTO resourceDTO) {
		resource.setTitle(resourceDTO.getTitle());
		resource.setDescription(resourceDTO.getDescription());
		resource.setCategoryId(resourceDTO.getCategoryId());
		resource.setPublicResource(resourceDTO.isPublicResource());
		resource.setScopeFk(resourceDTO.getScopePk());
	}

}
