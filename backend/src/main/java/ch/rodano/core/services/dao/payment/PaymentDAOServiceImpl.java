package ch.rodano.core.services.dao.payment;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.PaymentRecord;
import ch.rodano.core.model.payment.Payment;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AbstractDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.PAYMENT;

@Service
public class PaymentDAOServiceImpl extends AbstractDAOService<Payment, PaymentRecord> implements PaymentDAOService {

	public PaymentDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,

		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<PaymentRecord> getTable() {
		return Tables.PAYMENT;
	}

	@Override
	protected Class<Payment> getDAOClass() {
		return Payment.class;
	}

	@Override
	public Payment getPaymentByPk(final Long pk) {
		final var query = create.selectFrom(PAYMENT).where(PAYMENT.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public List<Payment> getPaymentByBatchPk(final Long batchPk) {
		final var query = create.selectFrom(PAYMENT).where(PAYMENT.PAYMENT_BATCH_FK.eq(batchPk));
		return find(query);
	}

	@Override
	public List<Payment> getPaymentsByWorkflowStatusFk(final Long workflowStatusPk) {
		final var query = create.selectFrom(PAYMENT).where(PAYMENT.WORKFLOW_STATUS_FK.eq(workflowStatusPk));
		return find(query);
	}

	@Override
	public void deletePayment(final Payment payment, final DatabaseActionContext context, final String rationale) {
		delete(payment, context, rationale);
	}

	@Override
	public void savePayment(final Payment payment, final DatabaseActionContext context, final String rationale) {
		save(payment, context, rationale);
	}
}
