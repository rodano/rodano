package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.services.dao.configurator.FeatureGrantsDAOService;

@Service
@Transactional
public class FeatureGrantsServiceImpl implements FeatureGrantsService {

	private final FeatureGrantsDAOService featureGrantsDAOService;

	public FeatureGrantsServiceImpl(final FeatureGrantsDAOService featureGrantsDAOService) {
		this.featureGrantsDAOService = featureGrantsDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, List<UUID>> getFeatureGrants(final UUID projectId) {
		return featureGrantsDAOService.getFeatureGrants(projectId);
	}

	@Override
	public void saveFeatureGrants(final UUID projectId, final Map<UUID, List<UUID>> profileFeatureMap) {
		featureGrantsDAOService.saveFeatureGrants(projectId, profileFeatureMap);
	}
}
