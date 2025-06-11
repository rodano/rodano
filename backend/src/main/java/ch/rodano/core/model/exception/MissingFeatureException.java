package ch.rodano.core.model.exception;

import java.util.Arrays;
import java.util.Collection;

import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.configuration.model.feature.FeatureStatic;

public final class MissingFeatureException extends RuntimeException implements TechnicalException {
	private static final long serialVersionUID = -1873768816151745669L;

	private MissingFeatureException(final Collection<String> featureIds) {
		super(String.format("Missing features %s", String.join(",", featureIds)));
	}

	public MissingFeatureException(final Feature... features) {
		this(Arrays.stream(features).map(Feature::getId).toList());
	}

	public MissingFeatureException(final FeatureStatic... features) {
		this(Arrays.stream(features).map(FeatureStatic::getId).toList());
	}
}
