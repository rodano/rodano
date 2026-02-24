package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.FeatureDTO;

public interface FeatureService {

	List<FeatureDTO> getFeatures(UUID projectId);

	FeatureDTO getFeature(UUID projectId, UUID featureId);

	FeatureDTO createFeature(UUID projectId, FeatureDTO feature);

	FeatureDTO updateFeature(UUID projectId, UUID featureId, FeatureDTO feature);

	void deleteFeature(UUID projectId, UUID featureId);
}
