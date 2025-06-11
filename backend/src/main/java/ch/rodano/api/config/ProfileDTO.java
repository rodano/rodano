package ch.rodano.api.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Rights;

public record ProfileDTO(
	@NotBlank String id,
	@NotNull SortedMap<String, String> shortname,
	SortedMap<String, String> longname,
	SortedMap<String, String> description,

	//rights
	@NotNull Collection<String> features,
	@NotNull Map<String, Set<Rights>> profilesRight,

	@NotNull List<PrivacyPolicyDTO> privacyPolicies
) {

	public ProfileDTO(final Profile profile) {
		this(
			profile.getId(),
			profile.getShortname(),
			profile.getLongname(),
			profile.getDescription(),
			profile.getGrantedFeatureIds(),
			profile.getGrantedProfileIdRights(),
			profile.getPrivacyPolicies().stream().map(PrivacyPolicyDTO::new).toList()
		);
	}
}
