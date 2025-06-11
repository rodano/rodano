package ch.rodano.core.services.bll.payment;

import ch.rodano.configuration.model.payment.Payable;
import ch.rodano.core.model.payment.PaymentTarget;

public interface PaymentTargetService {

	Payable getPayable(PaymentTarget target);

}
