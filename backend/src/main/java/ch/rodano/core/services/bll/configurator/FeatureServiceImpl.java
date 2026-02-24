package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.FeatureDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.FeatureDAOService;

@Service
@Transactional
public class FeatureServiceImpl implements FeatureService {

	private final FeatureDAOService featureDAOService;

	public FeatureServiceImpl(final FeatureDAOService featureDAOService) {
		this.featureDAOService = featureDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<FeatureDTO> getFeatures(final UUID projectId) {
		return featureDAOService.getFeatures(projectId);
	}

	@Override
	public FeatureDTO getFeature(final UUID projectId, final UUID featureId) {
		final var feature = featureDAOService.getFeature(projectId, featureId);
		if(feature == null) {
			throw new NotFoundException("Feature not found: " + featureId);
		}
		return feature;
	}

	@Override
	public FeatureDTO createFeature(final UUID projectId, final FeatureDTO feature) {
		return featureDAOService.createFeature(projectId, feature);
	}

	@Override
	public FeatureDTO updateFeature(final UUID projectId, final UUID featureId, final FeatureDTO feature) {
		final var existing = featureDAOService.getFeature(projectId, featureId);
		if(existing == null) {
			throw new NotFoundException("Feature not found: " + featureId);
		}
		return featureDAOService.updateFeature(projectId, featureId, feature);
	}

	@Override
	public void deleteFeature(final UUID projectId, final UUID featureId) {
		final var existing = featureDAOService.getFeature(projectId, featureId);
		if(existing == null) {
			throw new NotFoundException("Feature not found: " + featureId);
		}
		featureDAOService.deleteFeature(projectId, featureId);
	}
}
