package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.EntityRightDTO;
import ch.rodano.core.services.dao.configurator.ProfileRightsDAOService;

@Service
@Transactional
public class ProfileRightsServiceImpl implements ProfileRightsService {

	private final ProfileRightsDAOService profileRightsDAOService;

	public ProfileRightsServiceImpl(final ProfileRightsDAOService profileRightsDAOService) {
		this.profileRightsDAOService = profileRightsDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, Map<UUID, EntityRightDTO>> getProfileRights(final UUID projectId) {
		return profileRightsDAOService.getProfileRights(projectId);
	}

	@Override
	public void saveProfileRights(final UUID projectId, final Map<UUID, Map<UUID, EntityRightDTO>> rights) {
		profileRightsDAOService.saveProfileRights(projectId, rights);
	}
}
