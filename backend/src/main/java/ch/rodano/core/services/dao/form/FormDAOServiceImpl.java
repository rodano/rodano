package ch.rodano.core.services.dao.form;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
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

	private List<Form> search(final Optional<Long> scopePk, final Optional<Long> eventPk, final boolean includeDeleted, final Optional<Collection<String>> formModelIds) {
		final var query = create.selectFrom(FORM).where(
			scopePk.map(FORM.SCOPE_FK::eq).orElse(DSL.noCondition())
				.and(eventPk.map(FORM.EVENT_FK::eq).orElse(FORM.EVENT_FK.isNull()))
				.and(includeDeleted ? DSL.noCondition() : FORM.DELETED.isFalse())
				.and(formModelIds.map(FORM.FORM_MODEL_ID::in).orElse(DSL.noCondition()))
		);
		return find(query);
	}

	@Override
	public List<Form> search(final Long scopePk, final Optional<Long> eventPk, final boolean includeDeleted, final Optional<Collection<String>> formModelIds) {
		return search(Optional.of(scopePk), eventPk, includeDeleted, formModelIds);
	}

	//scope
	@Override
	public List<Form> getFormsByScopePk(final Long scopePk) {
		return search(Optional.of(scopePk), Optional.empty(), false, Optional.empty());
	}

	@Override
	public List<Form> getFormsByScopePkAndFormModelIds(final Long scopePk, final Collection<String> formModelIds) {
		return search(Optional.of(scopePk), Optional.empty(), false, Optional.of(formModelIds));
	}

	@Override
	public List<Form> getAllFormsByScopePk(final Long scopePk) {
		return search(Optional.of(scopePk), Optional.empty(), true, Optional.empty());
	}

	@Override
	public List<Form> getAllFormsByScopePkAndFormModelIds(final Long scopePk, final Collection<String> formModelIds) {
		return search(Optional.of(scopePk), Optional.empty(), true, Optional.of(formModelIds));
	}

	//event
	@Override
	public List<Form> getFormsByEventPk(final Long eventPk) {
		return search(Optional.empty(), Optional.of(eventPk), false, Optional.empty());
	}

	@Override
	public List<Form> getFormsByEventPkAndFormModelIds(final Long eventPk, final Collection<String> formModelIds) {
		return search(Optional.empty(), Optional.of(eventPk), false, Optional.of(formModelIds));
	}

	@Override
	public List<Form> getAllFormsByEventPk(final Long eventPk) {
		return search(Optional.empty(), Optional.of(eventPk), true, Optional.empty());
	}

	@Override
	public List<Form> getAllFormsByEventPkAndFormModelIds(final Long eventPk, final Collection<String> formModelIds) {
		return search(Optional.empty(), Optional.of(eventPk), true, Optional.of(formModelIds));
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
