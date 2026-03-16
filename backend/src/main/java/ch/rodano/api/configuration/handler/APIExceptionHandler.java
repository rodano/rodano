package ch.rodano.api.configuration.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import ch.rodano.api.controller.form.exception.DatasetSubmissionException;
import ch.rodano.api.exception.ManagedException;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.configuration.core.Environment;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.exception.TechnicalException;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailOrigin;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.commons.cache.transaction.TransactionCacheDAOService;

@ControllerAdvice
public class APIExceptionHandler extends ResponseEntityExceptionHandler {

	private static final String INTERNAL_ERROR_MESSAGE = "An internal error occurred";

	private final StudyService studyService;
	private final MailService mailService;
	private final TransactionCacheDAOService transactionCacheDAOService;
	private final Configurator configurator;
	private final boolean sendExceptionEmail;
	private final String exceptionEmailRecipient;
	private final DatabaseActionContext context;

	public APIExceptionHandler(
		final StudyService studyService,
		final MailService mailService,
		final TransactionCacheDAOService transactionCacheDAOService,
		final AuditActionService auditActionService,
		@Value("${rodano.api.exception-emails.enabled:false}") final boolean sendExceptionEmail,
		@Value("${rodano.api.exception-emails.recipient:bug@rodano.ch}") final String exceptionEmailRecipient,
		final Configurator configurator
	) {
		this.studyService = studyService;
		this.mailService = mailService;
		this.transactionCacheDAOService = transactionCacheDAOService;
		this.configurator = configurator;
		this.sendExceptionEmail = sendExceptionEmail;
		this.exceptionEmailRecipient = exceptionEmailRecipient;

		//a context must be provided to send an e-mail even if it will not be used because e-mails are not audited
		this.context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, INTERNAL_ERROR_MESSAGE);
	}

	@ExceptionHandler(AccessDeniedException.class)
	protected ResponseEntity<Object> handleAccessDeniedException(final AccessDeniedException e, final WebRequest request) {
		logger.info(e.getLocalizedMessage(), e);
		final var unauthorized = new UnauthorizedException();
		return new ResponseEntity<>(new ErrorDetails(unauthorized.getHttpErrorStatus(), unauthorized.getMessage(), request.getDescription(false)), unauthorized.getHttpErrorStatus());
	}

	@ExceptionHandler(value = { Exception.class })
	protected ResponseEntity<Object> handleAnyException(final Exception e, final WebRequest request) {
		// IMPORTANT ! Empty the cache in case of an exception, otherwise the cache will be left in memory, polluting the thread execution.
		transactionCacheDAOService.emptyCache();

		final String path = request.getDescription(false);

		// Handle exceptions thrown from controllers via the ResponseStatusException API
		if(e instanceof final ResponseStatusException rse) {
			logger.info(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(new ErrorDetails(HttpStatus.valueOf(rse.getStatusCode().value()), rse.getReason(), path), rse.getHeaders(), rse.getStatusCode().value());
		}

		// Handle the form submission exception that sends out its own response entities
		if(e instanceof final DatasetSubmissionException dse) {
			logger.info(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(dse.getBlockingErrorsDTO(), dse.getHttpErrorStatus());
		}

		// Handle managed exception
		if(e instanceof final ManagedException me) {
			logger.info(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(new ErrorDetails(me.getHttpErrorStatus(), e.getMessage(), path), me.getHttpErrorStatus());
		}

		// Specific Java exception
		if(e instanceof IllegalArgumentException) {
			logger.info(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(new ErrorDetails(HttpStatus.BAD_REQUEST, e.getMessage(), path), HttpStatus.BAD_REQUEST);
		}

		//starting from here, we are managing "unexpected" exceptions
		logger.error(e.getLocalizedMessage(), e);

		if(sendExceptionEmail) {
			if(isExceptionWorthSendingEmail(e)) {
				//send an e-mail to warn about this
				final var study = studyService.getStudy();

				//retrieve actor name
				final var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				//the principal is not an actor for public HTTP requests (it's the string "anonymousUser")
				final String actorName = principal instanceof final Actor actor ? actor.getName() : (String) principal;

				//build body
				final var body = new StringWriter();
				body.append(String.format("Request path: %s\n", path));
				body.append(String.format("Actor name: %s\n", actorName));
				body.append(String.format("Exception message: %s\n", e.getLocalizedMessage()));
				body.append("Stack trace:\n");
				final PrintWriter writer = new PrintWriter(body);
				e.printStackTrace(writer);

				final var mail = new Mail();
				mail.setSender(study.getEmail());
				mail.setReplyTo(study.getEmail());
				mail.setRecipients(Collections.singleton(exceptionEmailRecipient));
				mail.setOrigin(MailOrigin.USER);
				mail.setIntent("Warn about an unexpected exception");
				mail.setSubject(String.format("An unexpected exception occurred on study %s", study.getDefaultLocalizedShortname()));
				mail.setTextBody(body.toString());
				//send the e-mail directly and do not try to store it
				//as the HTTP request failed, the transaction will be roll-backed and nothing will be saved in the database
				mailService.sendMail(mail, context, !Environment.PROD.equals(configurator.getEnvironment()));
			}
		}

		// Handle technical exceptions
		if(e instanceof TechnicalException || e instanceof UnexpectedRollbackException) {
			return new ResponseEntity<>(new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE, path), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Handle exception using the spring framework
		try {
			final ResponseEntity<Object> response = handleException(e, request);
			// Do not return empty body
			return new ResponseEntity<>(new ErrorDetails(HttpStatus.valueOf(response.getStatusCode().value()), INTERNAL_ERROR_MESSAGE, path), response.getHeaders(), response.getStatusCode());
		}
		catch(final Exception ex) {
			return new ResponseEntity<>(new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), path), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		final MethodArgumentNotValidException ex,
		final HttpHeaders headers,
		final HttpStatusCode status,
		final WebRequest request
	) {
		final var message = Stream.concat(
			ex.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()),
			ex.getBindingResult().getGlobalErrors().stream()
				.map(globalError -> globalError.getObjectName() + ": " + globalError.getDefaultMessage())
		).collect(Collectors.joining(", "));

		final var path = request.getDescription(false);
		return new ResponseEntity<>(new ErrorDetails(HttpStatus.BAD_REQUEST, message, path), HttpStatus.BAD_REQUEST);
	}

	//returns true if the exception is worth sending an e-mail
	protected boolean isExceptionWorthSendingEmail(final Exception exception) {
		//ClientAbortException means that the client closed the connection before all the data could be sent to them (for example, if they close a browser tab that was still receiving data)
		if(exception instanceof ClientAbortException) {
			return false;
		}
		return true;
	}
}
