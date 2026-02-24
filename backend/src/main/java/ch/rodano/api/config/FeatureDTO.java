package ch.rodano.api.config;

import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.feature.Feature;

public record FeatureDTO(
	@NotNull UUID featureId,
	@NotBlank String id,

	@NotNull SortedMap<String, String> shortname,
	SortedMap<String, String> longname,
	SortedMap<String, String> description,

	@NotNull Boolean optional
) {
	public FeatureDTO(final Feature feature) {
		this(
			feature.getFeatureId(),
			feature.getId(),
			feature.getShortname(),
			feature.getLongname(),
			feature.getDescription(),
			feature.isOptional()
		);
	}
}

