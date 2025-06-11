package ch.rodano.api.config;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.configuration.model.policy.PrivacyPolicy;

@Schema(description = "Defines a privacy policy that is presented to users")
public record PrivacyPolicyDTO(
	@Schema(description = "Privacy policy ID")
	@NotBlank
	String id,
	@NotNull
	SortedMap<String, String> shortname,
	@NotNull
	SortedMap<String, String> longname,
	SortedMap<String, String> description,

	@Schema(description = "Privacy policy content")
	@NotEmpty
	Map<String, String> content,

	@Schema(description = "Profiles affected by the privacy policy")
	@NotEmpty
	Set<String> profileIds
) {
	public PrivacyPolicyDTO(final PrivacyPolicy privacyPolicy) {
		this(
			privacyPolicy.getId(),
			privacyPolicy.getShortname(),
			privacyPolicy.getLongname(),
			privacyPolicy.getDescription(),
			privacyPolicy.getContent(),
			privacyPolicy.getProfileIds()
		);
	}
}
