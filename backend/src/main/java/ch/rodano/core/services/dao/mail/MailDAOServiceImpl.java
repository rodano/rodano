package ch.rodano.core.services.dao.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.MailRecord;
import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailAttachment;
import ch.rodano.core.model.mail.MailSearch;
import ch.rodano.core.model.mail.MailStatus;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AbstractDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.MAIL;

@Service
public class MailDAOServiceImpl extends AbstractDAOService<Mail, MailRecord> implements MailDAOService {

	private final MailAttachmentDAOService mailAttachmentDAOService;

	public MailDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService,
		final MailAttachmentDAOService mailAttachmentDAOService
	) {
		super(create, strategy, studyService);
		this.mailAttachmentDAOService = mailAttachmentDAOService;
	}

	@Override
	protected Table<MailRecord> getTable() {
		return Tables.MAIL;
	}

	@Override
	protected Class<Mail> getDAOClass() {
		return Mail.class;
	}

	/**
	 * Get a mail by its pk
	 *
	 * @param pk The mail pk
	 * @return A mail
	 */
	@Override
	public Mail getMailByPk(final Long pk) {
		final var query = create.selectFrom(MAIL).where(MAIL.PK.eq(pk));
		return findUnique(query);
	}

	/**
	 * Get mails by their pks
	 *
	 * @param pks The mails' pk
	 * @return The mails
	 */
	@Override
	public List<Mail> getMailsByPks(final List<Long> pks) {
		final var query = create.selectFrom(MAIL).where(MAIL.PK.in(pks));
		return find(query);
	}

	/**
	 * Get mails by their status
	 *
	 * @param status The status
	 * @param limit  The limit of mail to retrieve
	 * @return The mails
	 */
	@Override
	public List<Mail> getMailsByStatus(final MailStatus status, final Integer limit) {
		final var query = create.selectFrom(MAIL).where(MAIL.STATUS.eq(status)).limit(limit);
		return find(query);
	}

	/**
	 * Create or update a mail
	 *
	 * @param mail    The mail
	 * @param context The context
	 * @return The created or updated mail
	 */
	// The method doesn't save mail attachments from Mail object anymore, since mail attachments are not an attribute
	@Override
	public Mail saveMail(final Mail mail, final DatabaseActionContext context, final String rationale) {
		save(mail, context, rationale);
		return mail;
	}

	//alternative method, allows to add/update mailAttachments from the Java and save to DB
	@Override
	public Mail saveMail(final Mail mail, final List<MailAttachment> mailAttachments, final DatabaseActionContext context, final String rationale) {
		save(mail, context, rationale);

		mailAttachments.forEach(attachment -> {
			attachment.setMailFk(mail.getPk());
			mailAttachmentDAOService.saveMailAttachment(attachment, context, rationale);
		});

		return mail;
	}

	/**
	 * Get all valid mails
	 *
	 * @param search The search
	 * @return The mails corresponding to the given predicate
	 */
	@Override
	public PagedResult<Mail> search(final MailSearch search) {
		final List<Condition> conditions = new ArrayList<>();

		search.getOrigin().ifPresent(origin -> {
			conditions.add(MAIL.ORIGIN.eq(origin));
		});

		search.getIntent().ifPresent(intent -> {
			conditions.add(MAIL.INTENT.containsIgnoreCase(intent));
		});

		search.getSender().ifPresent(sender -> {
			conditions.add(MAIL.SENDER.containsIgnoreCase(sender));
		});

		search.getRecipient().ifPresent(recipient -> {
			conditions.add(MAIL.RECIPIENTS.containsIgnoreCase(Collections.singleton(recipient)));
		});

		search.getStatus().ifPresent(status -> {
			conditions.add(MAIL.STATUS.eq(status));
		});

		search.getFullText().ifPresent(fullText -> {
			conditions.add(MAIL.SUBJECT.containsIgnoreCase(fullText).or(MAIL.TEXT_BODY.containsIgnoreCase(fullText)));
		});

		search.getBeforeDate().ifPresent(beforeDate -> {
			conditions.add(MAIL.CREATION_TIME.le(beforeDate));
		});

		search.getAfterDate().ifPresent(afterDate -> {
			conditions.add(MAIL.CREATION_TIME.ge(afterDate));
		});

		final var query = create.select(MAIL.asterisk(), DSL.count().over().as("total"))
			.from(MAIL)
			.where(conditions)
			.orderBy(search.getSortBy().getField().sort(search.getOrder()))
			.limit(search.getLimitField())
			.offset(search.getOffsetField());

		final var result = query.fetch();
		var total = 0;
		if(result.size() > 0) {
			total = result.get(0).getValue("total", Integer.class);
		}

		final var mails = result.into(Mail.class);
		return new PagedResult<>(mails, search.getPageSize(), search.getPageIndex(), total);
	}

}
