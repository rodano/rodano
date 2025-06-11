package ch.rodano.core.services.dao.payment;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.payment.PaymentBatch;

public interface PaymentBatchDAOService {

	PaymentBatch getPaymentBatchByPk(Long pk);

	void deletePaymentBatch(PaymentBatch paymentBatch, DatabaseActionContext context, String rationale);

	/**
	 * Create of update payment batch
	 *
	 * @param paymentBatch The payment batch to create/update
	 * @param context      The context of the action
	 * @param rationale The rationale for the operation
	 */
	void savePaymentBatch(PaymentBatch paymentBatch, DatabaseActionContext context, String rationale);
}
