package ch.rodano.core.services.bll.payment;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;

import ch.rodano.configuration.model.payment.Payable;
import ch.rodano.configuration.model.payment.PaymentDistribution;
import ch.rodano.core.model.payment.Payment;
import ch.rodano.core.model.payment.PaymentTarget;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;

public interface PaymentService {

	Scope getScope(Payment payment);

	List<Payable> getPayables(Payment payment);

	Payable getPayable(Payment payment, PaymentDistribution distribution);

	double getTotalValue(Payment payment);

	double getTotalValue(Payment payment, Payable payable);

	Collection<User> getUsersToNotify(Payment payment);

	/**
	 * Validate payment by creating payment target for each distribution
	 *
	 * @return the list of payment targets
	 */
	List<PaymentTarget> validate(Payment payment) throws Exception;

	Document getExportForXml(Payment payment, String... languages) throws Exception;
}
