package ch.rodano.core.services.dao.form;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.FormAuditTrail;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.FormAuditRecord;
import ch.rodano.core.model.jooq.tables.records.FormRecord;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.tables.Form.FORM;

@Service
public class FormDAOServiceImpl extends AuditableDAOService<Form, FormAuditTrail, FormRecord, FormAuditRecord> implements FormDAOService {

	public FormDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<FormRecord> getTable() {
		return Tables.FORM;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Table<FormAuditRecord> getAuditTable() {
		return Tables.FORM_AUDIT;
	}

	@Override
	protected Class<FormAuditTrail> getEntityAuditClass() {
		return FormAuditTrail.class;
	}

	@Override
	protected Class<Form> getDAOClass() {
		return Form.class;
	}

	@Override
	public Form getFormByPk(final Long pk) {
		final var query = create.selectFrom(FORM).where(FORM.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public List<Form> getFormsByScopePkIncludingRemoved(final Long scopePk) {
		final var query = create.selectFrom(FORM).where(FORM.SCOPE_FK.eq(scopePk));
		return find(query);
	}

	@Override
	public List<Form> getFormsByScopePk(final Long scopePk) {
		final var query = create.selectFrom(FORM).where(FORM.SCOPE_FK.eq(scopePk)).and(FORM.DELETED.isFalse());
		return find(query);
	}

	@Override
	public Form getFormByScopePkAndFormModelId(final Long scopePk, final String formModelId) {
		final var query = create.selectFrom(FORM).where(FORM.SCOPE_FK.eq(scopePk).and(FORM.FORM_MODEL_ID.eq(formModelId)));
		return findUnique(query);
	}

	@Override
	public List<Form> getFormsByEventPkIncludingRemoved(final Long eventPk) {
		final var query = create.selectFrom(FORM).where(FORM.EVENT_FK.eq(eventPk));
		return find(query);
	}

	@Override
	public List<Form> getFormsByEventPk(final Long eventPk) {
		final var query = create.selectFrom(FORM).where(FORM.EVENT_FK.eq(eventPk)).and(FORM.DELETED.isFalse());
		return find(query);
	}

	@Override
	public Form getFormByEventPkAndFormModelId(final Long eventPk, final String formModelId) {
		final var query = create.selectFrom(FORM).where(FORM.EVENT_FK.eq(eventPk).and(FORM.FORM_MODEL_ID.eq(formModelId)));
		return findUnique(query);
	}

	@Override
	public void deleteForm(final Form form, final DatabaseActionContext context, final String rationale) {
		delete(form, context, rationale);
	}

	@Override
	public void restoreForm(final Form form, final DatabaseActionContext context, final String rationale) {
		restore(form, context, rationale);
	}

	@Override
	public void saveForm(final Form form, final DatabaseActionContext context, final String rationale) {
		save(form, context, rationale);
	}
}
