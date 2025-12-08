package ch.rodano.batch.processor;


import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.inject.Inject;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Project;

public class ProjectIdProcessor implements ItemProcessor {

	@Inject
	@BatchProperty(name = "projectId")
	private String projectId;

	private UUID resolvedProjectId;

	@Override
	public Object processItem(final Object item) {
		if(resolvedProjectId == null) {
			if(projectId == null || projectId.isBlank()) {
				throw new IllegalStateException("Batch property 'projectId' must be provided");
			}

			try {
				resolvedProjectId = UUID.fromString(projectId);
			}
			catch(IllegalArgumentException ex) {
				resolvedProjectId = UUID.nameUUIDFromBytes(("PROJECT:" + projectId).getBytes(StandardCharsets.UTF_8));
			}
		}

		if(item instanceof Project project && project.getProjectId() == null) {
			project.setProjectId(resolvedProjectId);
			return new ProjectScoped<>(resolvedProjectId, project);
		}

		return new ProjectScoped<>(resolvedProjectId, item);
	}
}
