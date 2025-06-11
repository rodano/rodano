package ch.rodano.core.services.dao.payment;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.PaymentTargetRecord;
import ch.rodano.core.model.payment.PaymentTarget;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AbstractDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.PAYMENT_TARGET;

@Service
public class PaymentTargetDAOServiceImpl extends AbstractDAOService<PaymentTarget, PaymentTargetRecord> implements PaymentTargetDAOService {

	public PaymentTargetDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,

		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<PaymentTargetRecord> getTable() {
		return Tables.PAYMENT_TARGET;
	}

	@Override
	protected Class<PaymentTarget> getDAOClass() {
		return PaymentTarget.class;
	}

	@Override
	public PaymentTarget getPaymentTargetByPk(final Long pk) {
		final var query = create.selectFrom(PAYMENT_TARGET).where(PAYMENT_TARGET.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public List<PaymentTarget> getPaymentTargetsByPaymentPk(final Long paymentPk) {
		final var query = create.selectFrom(PAYMENT_TARGET).where(PAYMENT_TARGET.PAYMENT_FK.eq(paymentPk));
		return find(query);
	}

	@Override
	public void deletePaymentTarget(final PaymentTarget paymentTarget, final DatabaseActionContext context, final String rationale) {
		delete(paymentTarget, context, rationale);
	}

	@Override
	public void savePaymentTarget(final PaymentTarget paymentTarget, final DatabaseActionContext context, final String rationale) {
		save(paymentTarget, context, rationale);
	}
}
