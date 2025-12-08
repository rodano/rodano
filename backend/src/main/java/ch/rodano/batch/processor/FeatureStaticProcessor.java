package ch.rodano.batch.processor;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.inject.Inject;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Feature;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.core.services.project.ProjectIdResolver;

public class FeatureStaticProcessor implements ItemProcessor {

	@Inject
	@BatchProperty(name = "projectId")
	private String projectId;

	@Inject
	private ProjectIdResolver projectIdResolver;

	private UUID resolvedProjectId;

	@Override
	public Object processItem(final Object o) {
		final UUID pid = resolveProjectId();

		final FeatureStatic featureStatic = (FeatureStatic) o;

		final Feature feature = new Feature();
		feature.setId(featureStatic.getId());
		feature.setShortname(new HashMap<>(featureStatic.getShortname()));
		feature.setLongname(new HashMap<>(featureStatic.getLongname()));
		feature.setDescription(new HashMap<>(featureStatic.getDescription()));
		feature.setOptional(featureStatic.isOptional());

		return new ProjectScoped<>(pid, feature);
	}

	private UUID resolveProjectId() {
		if(resolvedProjectId != null) {
			return resolvedProjectId;
		}

		if(projectId != null && !projectId.isBlank()) {
			try {
				resolvedProjectId = UUID.fromString(projectId);
			}
			catch(IllegalArgumentException ex) {
				resolvedProjectId = UUID.nameUUIDFromBytes(("PROJECT:" + projectId).getBytes(StandardCharsets.UTF_8));
			}
		}
		else {
			resolvedProjectId = projectIdResolver.id();
		}

		return resolvedProjectId;
	}
}
