package ch.rodano.core.services.dao.payment;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.PaymentBatchRecord;
import ch.rodano.core.model.payment.PaymentBatch;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AbstractDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.PAYMENT_BATCH;

@Service
public class PaymentBatchDAOServiceImpl extends AbstractDAOService<PaymentBatch, PaymentBatchRecord> implements PaymentBatchDAOService {

	public PaymentBatchDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,

		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<PaymentBatchRecord> getTable() {
		return Tables.PAYMENT_BATCH;
	}

	@Override
	protected Class<PaymentBatch> getDAOClass() {
		return PaymentBatch.class;
	}

	@Override
	public PaymentBatch getPaymentBatchByPk(final Long pk) {
		final var query = create.selectFrom(PAYMENT_BATCH).where(PAYMENT_BATCH.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public void deletePaymentBatch(final PaymentBatch paymentBatch, final DatabaseActionContext context, final String rationale) {
		delete(paymentBatch, context, rationale);
	}

	@Override
	public void savePaymentBatch(final PaymentBatch paymentBatch, final DatabaseActionContext context, final String rationale) {
		save(paymentBatch, context, rationale);
	}
}
