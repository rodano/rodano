package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.PrivacyPolicyDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.PrivacyPolicyDAOService;

@Service
@Transactional
public class PrivacyPolicyServiceImpl implements PrivacyPolicyService {

	private final PrivacyPolicyDAOService privacyPolicyDAOService;

	public PrivacyPolicyServiceImpl(final PrivacyPolicyDAOService privacyPolicyDAOService) {
		this.privacyPolicyDAOService = privacyPolicyDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<PrivacyPolicyDTO> getPrivacyPolicies(final UUID projectId) {
		return privacyPolicyDAOService.getPrivacyPolicies(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public PrivacyPolicyDTO getPrivacyPolicy(final UUID projectId, final UUID privacyPolicyId) {
		final var privacyPolicy = privacyPolicyDAOService.getPrivacyPolicy(projectId, privacyPolicyId);
		if(privacyPolicy == null) {
			throw new NotFoundException("Privacy Policy not found: " + privacyPolicyId);
		}
		return privacyPolicy;
	}

	@Override
	public PrivacyPolicyDTO createPrivacyPolicy(final UUID projectId, final PrivacyPolicyDTO privacyPolicy) {
		return privacyPolicyDAOService.createPrivacyPolicy(projectId, privacyPolicy);
	}

	@Override
	public PrivacyPolicyDTO updatePrivacyPolicy(final UUID projectId, final UUID privacyPolicyId, final PrivacyPolicyDTO privacyPolicy) {
		final var existing = privacyPolicyDAOService.getPrivacyPolicy(projectId, privacyPolicyId);
		if(existing == null) {
			throw new NotFoundException("Privacy Policy not found: " + privacyPolicyId);
		}
		return privacyPolicyDAOService.updatePrivacyPolicy(projectId, privacyPolicyId, privacyPolicy);
	}

	@Override
	public void deletePrivacyPolicy(final UUID projectId, final UUID privacyPolicyId) {
		final var existing = privacyPolicyDAOService.getPrivacyPolicy(projectId, privacyPolicyId);
		if(existing == null) {
			throw new NotFoundException("Privacy Policy not found: " + privacyPolicyId);
		}
		privacyPolicyDAOService.deletePrivacyPolicy(projectId, privacyPolicyId);
	}
}
