package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.PrivacyPolicyDTO;


public interface PrivacyPolicyDAOService {

	List<PrivacyPolicyDTO> getPrivacyPolicies(UUID projectId);

	PrivacyPolicyDTO getPrivacyPolicy(UUID projectId, UUID privacyPolicyId);

	PrivacyPolicyDTO createPrivacyPolicy(UUID projectId, PrivacyPolicyDTO privacyPolicy);

	PrivacyPolicyDTO updatePrivacyPolicy(UUID projectId, UUID privacyPolicyId, PrivacyPolicyDTO privacyPolicy);

	void deletePrivacyPolicy(UUID projectId, UUID privacyPolicyId);
}
