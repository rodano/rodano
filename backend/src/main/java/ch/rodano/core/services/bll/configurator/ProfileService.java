package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ProfileDTO;

public interface ProfileService {

	List<ProfileDTO> getProfiles(UUID projectId);

	ProfileDTO getProfile(UUID projectId, UUID profileId);

	ProfileDTO createProfile(UUID projectId, ProfileDTO profile);

	ProfileDTO updateProfile(UUID projectId, UUID profileId, ProfileDTO profile);

	void deleteProfile(UUID projectId, UUID profileId);
}
