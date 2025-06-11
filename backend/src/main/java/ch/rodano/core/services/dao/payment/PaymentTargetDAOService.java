package ch.rodano.core.services.dao.payment;

import java.util.List;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.payment.PaymentTarget;

public interface PaymentTargetDAOService {

	PaymentTarget getPaymentTargetByPk(Long pk);

	/**
	 * Get payment targets associated with the given payment
	 *
	 * @param paymentPk The payment pk
	 * @return The list of payment targets associated with the given payment
	 */
	List<PaymentTarget> getPaymentTargetsByPaymentPk(Long paymentPk);

	void deletePaymentTarget(PaymentTarget paymentTarget, DatabaseActionContext context, String rationale);

	void savePaymentTarget(PaymentTarget paymentTarget, DatabaseActionContext context, String rationale);
}
