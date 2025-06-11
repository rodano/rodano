package ch.rodano.core.services.dao.mail;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.MailAttachmentRecord;
import ch.rodano.core.model.mail.MailAttachment;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AbstractDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.MAIL_ATTACHMENT;

@Service
public class MailAttachmentDAOServiceImpl extends AbstractDAOService<MailAttachment, MailAttachmentRecord> implements MailAttachmentDAOService {

	public MailAttachmentDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<MailAttachmentRecord> getTable() {
		return Tables.MAIL_ATTACHMENT;
	}

	@Override
	protected Class<MailAttachment> getDAOClass() {
		return MailAttachment.class;
	}

	@Override
	public MailAttachment getMailAttachmentByPk(final Long pk) {
		final var query = create.selectFrom(MAIL_ATTACHMENT).where(MAIL_ATTACHMENT.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public List<MailAttachment> getMailAttachmentByMailPk(final Long mailPk) {
		final var query = create.selectFrom(MAIL_ATTACHMENT).where(MAIL_ATTACHMENT.MAIL_FK.eq(mailPk));
		return find(query);
	}

	@Override
	public List<MailAttachment> getMailAttachmentByMailPks(final List<Long> mailPks) {
		final var query = create.selectFrom(MAIL_ATTACHMENT).where(MAIL_ATTACHMENT.MAIL_FK.in(mailPks));
		return find(query);
	}

	@Override
	public void saveMailAttachment(final MailAttachment mailAttachment, final DatabaseActionContext context, final String rationale) {
		save(mailAttachment, context, rationale);
	}
}
