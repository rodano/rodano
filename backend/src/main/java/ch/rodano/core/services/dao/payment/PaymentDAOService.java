package ch.rodano.core.services.dao.payment;

import java.util.List;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.payment.Payment;

public interface PaymentDAOService {

	Payment getPaymentByPk(Long pk);

	/**
	 * Get a list of payments associated with the given payment batch
	 *
	 * @param batchPk The payment batch pk
	 * @return A list of payments for the given batch
	 */
	List<Payment> getPaymentByBatchPk(Long batchPk);

	List<Payment> getPaymentsByWorkflowStatusFk(Long workflowStatusFk);

	void deletePayment(Payment payment, DatabaseActionContext context, String rationale);

	void savePayment(Payment payment, DatabaseActionContext context, String rationale);
}
