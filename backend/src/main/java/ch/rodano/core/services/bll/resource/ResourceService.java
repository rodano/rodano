package ch.rodano.core.services.bll.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.resource.ResourceSearch;

public interface ResourceService {

	/**
	 * Get a resource by its pk
	 *
	 * @param pk The pk of the resource
	 * @return The wanted resource or null if doesn't exist
	 */
	Resource getResourceByPk(Long pk);

	/**
	 * Perform a search on resources
	 *
	 * @param search The resource search predicate
	 * @return A list of resources
	 */
	PagedResult<Resource> search(ResourceSearch search);

	/**
	 * Create a resource
	 * @param resource The resource to create
	 * @param actor    The actor of creation
	 * @param context  The context in which the action takes place
	 * @return The created resource
	 */
	Resource createResource(
		Resource resource,
		Actor actor,
		DatabaseActionContext context
	);

	/**
	 * Create or update a resource
	 *
	 * @param resource The resource to save
	 * @param context  The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void saveResource(
		Resource resource,
		DatabaseActionContext context,
		String rationale
	);

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
	 * Attach a file to a resource
	 *
	 * @param resource    The resource to save
	 * @param filename    Filename
	 * @param fileContent   File content
	 * @param context     The context in which the action takes place
	 * @param rationale The rationale for the operation
	 * @return The saved resource
	 */
	Resource attachFileToResource(
		Resource resource,
		final String filename,
		final InputStream fileContent,
		DatabaseActionContext context,
		String rationale
	) throws IOException;

	/**
	 * Get file attached to a resource
	 *
	 * @param os        File stream
	 * @param resource  The resource
	 */
	void getResourceFile(
		OutputStream os,
		Resource resource
	) throws IOException;
}
