package ch.rodano.api.mail;

import java.io.ByteArrayInputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailAttachment;
import ch.rodano.core.model.mail.MailOrigin;
import ch.rodano.core.model.mail.MailSearch;
import ch.rodano.core.model.mail.MailSortBy;
import ch.rodano.core.model.mail.MailStatus;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.mail.MailAttachmentService;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.mail.MailAttachmentDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Profile("!migration")
@Tag(name = "Mail")
@RestController
@RequestMapping("/mails")
@Transactional(readOnly = true)
public class MailController extends AbstractSecuredController {
	private final MailService mailService;
	private final MailAttachmentService mailAttachmentService;
	private final MailAttachmentDAOService mailAttachmentDAOService;
	private final MailDTOService mailDTOService;
	private final UtilsService utilsService;

	private final Integer defaultPageSize;

	public MailController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final MailService mailService,
		final MailAttachmentService mailAttachmentService,
		final MailAttachmentDAOService mailAttachmentDAOService,
		final MailDTOService mailDTOService,
		final UtilsService utilsService,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.mailService = mailService;
		this.mailAttachmentService = mailAttachmentService;
		this.mailAttachmentDAOService = mailAttachmentDAOService;
		this.mailDTOService = mailDTOService;
		this.utilsService = utilsService;
		this.defaultPageSize = defaultPageSize;
	}

	@Operation(summary = "Send mail")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public MailDTO sendMail(
		@RequestBody final MailCreationDTO mailDTO
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		rightsService.checkRightAdmin(currentActor, currentRoles);

		mailService.checkEmailAddress(mailDTO.recipient());

		final var context = currentContext();
		final var rationale = "Send mail using the REST API";

		final var mail = mailService.createMail(Set.of(mailDTO.recipient()), mailDTO.subject(), mailDTO.body(), context, rationale);
		//send mail manually to override the "simulate" boolean
		final var sentMail = mailService.sendMail(mail, context, false);
		return mailDTOService.createDTO(sentMail);
	}

	@Operation(summary = "Get mail")
	@GetMapping("{mailPk}")
	@ResponseStatus(HttpStatus.OK)
	public MailDTO getMailByPk(
		@PathVariable final Long mailPk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_MAILS);

		final var mail = mailService.getMailByPk(mailPk);
		utilsService.checkNotNull(Mail.class, mail, mailPk);

		return mailDTOService.createDTO(mail);
	}

	@Operation(summary = "Get all mail attachments")
	@GetMapping("{mailPk}/attachments")
	@ResponseStatus(HttpStatus.OK)
	public List<MailAttachmentDTO> getAttachmentsByMailByPk(
		@PathVariable final Long mailPk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_MAILS);

		return mailAttachmentDAOService.getMailAttachmentByMailPk(mailPk).stream().map(MailAttachmentDTO::new).toList();
	}

	@Operation(summary = "Get mail attachment")
	@GetMapping("{mailPk}/attachments/{attachmentPk}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> getMailAttachment(
		@PathVariable final Long mailPk,
		@PathVariable final Long attachmentPk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_MAILS);

		final var mail = mailService.getMailByPk(mailPk);
		utilsService.checkNotNull(Mail.class, mail, mailPk);

		final var mailAttachment = mailAttachmentService.getMailAttachmentByPk(attachmentPk);

		utilsService.checkNotNull(MailAttachment.class, mailAttachment, attachmentPk);

		//send response
		final StreamingResponseBody stream = os -> new ByteArrayInputStream(mailAttachment.getContent()).transferTo(os);
		return fileResponse(stream, MediaType.APPLICATION_OCTET_STREAM, mailAttachment.getFilename());
	}

	@Operation(summary = "Cancel mail transmission")
	@PostMapping("{mailPk}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void cancelMail(
		@PathVariable final Long mailPk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_MAILS);

		final var mail = mailService.getMailByPk(mailPk);

		utilsService.checkNotNull(Mail.class, mail, mailPk);

		final var status = mail.getStatus();
		if(status != MailStatus.PENDING) {
			throw new NotPossibleMailCancelException(mailPk, status);
		}

		mail.setStatus(MailStatus.CANCELED);
		mailService.saveMail(mail, currentContext(), "Cancel mail");
	}

	@Operation(summary = "Resend mails")
	@PostMapping("resend")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void resendMails(
		@RequestParam final List<Long> mailPks
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_MAILS);

		if(mailPks.size() == 0) {
			return;
		}

		mailService.markMailsToBeSent(mailPks, currentContext());
	}

	@Operation(summary = "Search mails")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public PagedResult<MailDTO> search(
		@Parameter(description = "Mail origin") @RequestParam final Optional<MailOrigin> origin,
		@Parameter(description = "Mail status") @RequestParam final Optional<MailStatus> status,
		@Parameter(description = "Mail intent") @RequestParam final Optional<String> intent,
		@Parameter(description = "Mail sender") @RequestParam final Optional<String> sender,
		@Parameter(description = "Mail recipient") @RequestParam final Optional<String> recipient,
		@Parameter(description = "Full text search on mail body and subject") @RequestParam final Optional<String> fullText,
		@Parameter(description = "Lower boundary for the mail creation time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Optional<ZonedDateTime> beforeDate,
		@Parameter(description = "Upper boundary for the mail creation time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Optional<ZonedDateTime> afterDate,
		@Parameter(description = "Sort the results by which property?") @RequestParam final Optional<MailSortBy> sortBy,
		@Parameter(description = "Use the ascending order?") @RequestParam final Optional<Boolean> orderAscending,
		@Parameter(description = "Page size") @RequestParam final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam final Optional<Integer> pageIndex
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_MAILS);

		final var search = new MailSearch()
			.setOrigin(origin)
			.setStatus(status)
			.setIntent(intent.filter(StringUtils::isNotBlank))
			.setSender(sender.filter(StringUtils::isNotBlank))
			.setRecipient(recipient.filter(StringUtils::isNotBlank))
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			// Convert the requestParam dates to UTC, in case they aren't
			.setBeforeDate(beforeDate.map(date -> date.withZoneSameInstant(ZoneId.systemDefault())))
			.setAfterDate(afterDate.map(date -> date.withZoneSameInstant(ZoneId.systemDefault())))
			.setPageSize(pageSize.isEmpty() ? Optional.of(defaultPageSize) : pageSize)
			.setPageIndex(pageIndex.isEmpty() ? Optional.of(0) : pageIndex);
		//set sort if provided
		sortBy.map(search::setSortBy);
		orderAscending.map(search::setSortAscending);

		return mailService.search(search).withObjectsTransformation(mailDTOService::createDTOs);
	}

	@Operation(summary = "Export mails in excel format")
	@GetMapping("export")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<StreamingResponseBody> export(
		@Parameter(description = "Mail origin") @RequestParam final Optional<MailOrigin> origin,
		@Parameter(description = "Mail status") @RequestParam final Optional<MailStatus> status,
		@Parameter(description = "Mail intent") @RequestParam final Optional<String> intent,
		@Parameter(description = "Mail sender") @RequestParam final Optional<String> sender,
		@Parameter(description = "Mail recipient") @RequestParam final Optional<String> recipient,
		@Parameter(description = "Full text search on mail body and subject") @RequestParam final Optional<String> fullText,
		@Parameter(description = "Lower boundary for the mail creation time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Optional<ZonedDateTime> beforeDate,
		@Parameter(description = "Upper boundary for the mail creation time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Optional<ZonedDateTime> afterDate
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_MAILS);

		final var search = new MailSearch()
			.setOrigin(origin)
			.setStatus(status)
			.setIntent(intent.filter(StringUtils::isNotBlank))
			.setSender(sender.filter(StringUtils::isNotBlank))
			.setRecipient(recipient.filter(StringUtils::isNotBlank))
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			// Convert the requestParam dates to UTC, in case they aren't
			.setBeforeDate(beforeDate.map(date -> date.withZoneSameInstant(ZoneId.systemDefault())))
			.setAfterDate(afterDate.map(date -> date.withZoneSameInstant(ZoneId.systemDefault())))
			.setSortBy(MailSearch.DEFAULT_SORT_BY)
			.setSortAscending(MailSearch.DEFAULT_SORT_ASCENDING);

		final var mails = mailService.search(search).getObjects();

		//send response
		final StreamingResponseBody stream = os -> mailService.exportMails(os, mails);
		final var filename = studyService.getStudy().generateFilename("mails", ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}
}
