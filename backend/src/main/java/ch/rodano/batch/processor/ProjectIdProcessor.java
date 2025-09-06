package ch.rodano.batch.processor;


import java.util.UUID;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.inject.Inject;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.core.services.project.ProjectIdResolver;

public class ProjectIdProcessor implements ItemProcessor {

	@Inject
	@BatchProperty(name = "projectId")
	private String projectId;

	@Inject
	private ProjectIdResolver projectIdResolver;

	@Override
	public Object processItem(final Object item) {
		final UUID pid = projectId != null && !projectId.isBlank()
			? UUID.fromString(projectId)
			: projectIdResolver.id();
		return new ProjectScoped<>(pid, item);
	}
}
