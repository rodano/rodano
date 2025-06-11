package ch.rodano.core.services.bll.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.exception.http.BadArgumentException;
import ch.rodano.api.resource.ResourceException;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.resource.ResourceSearch;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.resource.ResourceDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;

@Service
public class ResourceServiceImpl implements ResourceService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ScopeDAOService scopeDAOService;
	private final ResourceDAOService resourceDAOService;
	private final Configurator configurator;
	private final MailService mailService;
	private final ScopeService scopeService;

	public ResourceServiceImpl(
		final ResourceDAOService resourceDAOService,
		final Configurator configurator,
		final MailService mailService,
		final ScopeService scopeService,
		final ScopeDAOService scopeDAOService
	) {
		this.scopeDAOService = scopeDAOService;
		this.resourceDAOService = resourceDAOService;
		this.configurator = configurator;
		this.mailService = mailService;
		this.scopeService = scopeService;
	}

	@Override
	public Resource getResourceByPk(final Long pk) {
		return resourceDAOService.getResourceByPk(pk);
	}

	@Override
	public PagedResult<Resource> search(final ResourceSearch search) {
		return resourceDAOService.search(search);
	}

	@Override
	public Resource createResource(
		final Resource resource,
		final Actor actor,
		final DatabaseActionContext context
	) {
		resource.setUuid(UUID.randomUUID().toString());
		resource.setUserFk(actor.getPk());
		saveResource(resource, context, "Create a resource");
		return resource;
	}

	@Override
	public void saveResource(
		final Resource resource,
		final DatabaseActionContext context,
		final String rationale
	) {
		if(resource.getPublicResource()) {
			verifyPublicResourceRequirements(resource);
		}

		resourceDAOService.saveResource(resource, context, rationale);
	}

	@Override
	public void deleteResource(final Resource resource, final DatabaseActionContext context, final String rationale) {
		resourceDAOService.deleteResource(resource, context, rationale);
	}

	@Override
	public void restoreResource(final Resource resource, final DatabaseActionContext context, final String rationale) {
		resourceDAOService.restoreResource(resource, context, rationale);
	}

	@Override
	public Resource attachFileToResource(
		final Resource resource,
		final String filename,
		final InputStream fileContent,
		final DatabaseActionContext context,
		final String rationale
	) throws IOException {

		final var uuidFile = new File(configurator.getResourceFolder(), resource.getUuid());

		try(final var os = new FileOutputStream(uuidFile)) {
			fileContent.transferTo(os);
		}
		catch(final FileNotFoundException e) {
			//we are creating the file!
			logger.error(e.getLocalizedMessage(), e);
		}

		resource.setFilename(filename);
		resourceDAOService.saveResource(resource, context, "Attach a file to the resource");

		// Notify users of the new file for all categories, except the NEWS category
		if(!resource.getCategoryId().equals("NEWS") && context.actor().isPresent() && context.actor().get() instanceof final User user) {
			mailService.sendResourcePublicationNotification(user, resource, context);
		}

		return resource;
	}

	@Override
	public void getResourceFile(
		final OutputStream os,
		final Resource resource
	) throws IOException {
		// check if the resource has not been deleted
		if(resource.getDeleted()) {
			throw new ResourceException("The resource has been removed");
		}

		// Check if the resource has a file
		if(StringUtils.isBlank(resource.getFilename())) {
			throw new ResourceException("No file is attached to that resource");
		}

		final var file = new File(configurator.getResourceFolder(), resource.getUuid());
		if(!file.exists() || file.isDirectory()) {
			throw new ResourceException("The requested file could not be found");
		}

		Files.copy(file.toPath(), os);
	}

	/**
	 * Verify if the resource can be made public, otherwise throw an exception.
	 * @param resource The resource to be made public
	 * @throws BadArgumentException Thrown in case of non-conformity
	 */
	private void verifyPublicResourceRequirements(final Resource resource) {
		final var scope = scopeDAOService.getScopeByPk(resource.getScopeFk());
		if(!scopeService.isRootScope(scope)) {
			throw new BadArgumentException("Public resources must be attached to root scope");
		}
	}
}
