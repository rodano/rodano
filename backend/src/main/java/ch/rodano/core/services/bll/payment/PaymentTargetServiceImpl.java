package ch.rodano.core.services.bll.payment;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.payment.Payable;
import ch.rodano.core.model.payment.PaymentTarget;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;

@Service
public class PaymentTargetServiceImpl implements PaymentTargetService {

	private final StudyService studyService;
	private final ScopeDAOService scopeDAOService;

	public PaymentTargetServiceImpl(final StudyService studyService, final ScopeDAOService scopeDAOService) {
		this.studyService = studyService;
		this.scopeDAOService = scopeDAOService;
	}

	@Override
	@SuppressWarnings("unused")
	public Payable getPayable(final PaymentTarget target) {
		try {
			return studyService.getStudy().getProfile(target.getPayableId());
		}
		catch(final Exception e) {
			try {
				return scopeDAOService.getScopeById(target.getPayableId());
			}
			catch(final Exception ex) {
				return null;
			}
		}
	}
}
