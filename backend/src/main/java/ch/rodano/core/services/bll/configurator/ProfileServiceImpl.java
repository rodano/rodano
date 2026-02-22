package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.ProfileDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.ProfileDAOService;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {

	private final ProfileDAOService profileDAOService;

	public ProfileServiceImpl(final ProfileDAOService profileDAOService) {
		this.profileDAOService = profileDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProfileDTO> getProfiles(final UUID projectId) {
		return profileDAOService.getProfiles(projectId);
	}

	@Override
	public ProfileDTO getProfile(final UUID projectId, final UUID profileId) {
		final var profile = profileDAOService.getProfile(projectId, profileId);
		if(profile == null) {
			throw new NotFoundException("Profile not found: " + profileId);
		}
		return profile;
	}

	@Override
	public ProfileDTO createProfile(final UUID projectId, final ProfileDTO profile) {
		return profileDAOService.createProfile(projectId, profile);
	}

	@Override
	public ProfileDTO updateProfile(final UUID projectId, final UUID profileId, final ProfileDTO profile) {
		final var existing = profileDAOService.getProfile(projectId, profileId);
		if(existing == null) {
			throw new NotFoundException("Profile not found: " + profileId);
		}
		return profileDAOService.updateProfile(projectId, profileId, profile);
	}

	@Override
	public void deleteProfile(final UUID projectId, final UUID profileId) {
		final var existing = profileDAOService.getProfile(projectId, profileId);
		if(existing == null) {
			throw new NotFoundException("Profile not found: " + profileId);
		}
		profileDAOService.deleteProfile(projectId, profileId);
	}
}
