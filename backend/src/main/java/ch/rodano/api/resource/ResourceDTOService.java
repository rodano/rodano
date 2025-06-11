package ch.rodano.api.resource;

import java.util.List;
import java.util.Optional;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;

public interface ResourceDTOService {

	ResourceDTO createDTO(final Resource resource, final Optional<Actor> actor, final Optional<List<Role>> roles);

	/**
	 * Generate a resource from a resource DTO
	 *
	 * @return A generated resource
	 */
	Resource generateResource(ResourceSubmissionDTO resourceDTO);

	/**
	 * Update an already existing resource with a resource DTO
	 *
	 * @param resource       Resource to be updated
	 * @param resourceDTO    The DTO containing new info
	 */
	void updateResource(Resource resource, ResourceSubmissionDTO resourceDTO);
}
