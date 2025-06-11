package ch.rodano.core.services.dao.resource;

import java.util.List;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.resource.ResourceSearch;

public interface ResourceDAOService {

	/**
	 * Search resources
	 *
	 * @param search The predicate of resource to get
	 * @return A list of paginated resources
	 */
	PagedResult<Resource> search(ResourceSearch search);

	/**
	 * Create or update a resource
	 *
	 * @param resource The resource to save
	 * @param context  The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void saveResource(Resource resource, DatabaseActionContext context, String rationale);

	List<Resource> getAllResources();

	/**
	 * Delete a resource
	 *
	 * @param resource The resource to delete
	 * @param context  The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void deleteResource(Resource resource, DatabaseActionContext context, String rationale);

	/**
	 * Restore a resource
	 *
	 * @param resource The resource to restore
	 * @param context  The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void restoreResource(Resource resource, DatabaseActionContext context, String rationale);

	/**
	 * Get a resource by its pk
	 *
	 * @param pk The pk of the resource
	 * @return The wanted resource or null if doesn't exist
	 */
	Resource getResourceByPk(Long pk);
}
