package ch.rodano.core.model.mail;

import java.time.ZonedDateTime;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.common.PaginatedSearch;

public class MailSearch extends PaginatedSearch<MailSearch> {
	public static final MailSortBy DEFAULT_SORT_BY = MailSortBy.creationTime;
	public static final boolean DEFAULT_SORT_ASCENDING = false;

	@Schema(description = "Mail origin")
	private Optional<MailOrigin> origin = Optional.empty();

	@Schema(description = "Mail status")
	private Optional<MailStatus> status = Optional.empty();

	@Schema(description = "Text search on intent")
	private Optional<String> intent = Optional.empty();

	@Schema(description = "Text search on sender")
	private Optional<String> sender = Optional.empty();

	@Schema(description = "Text search on recipient")
	private Optional<String> recipient = Optional.empty();

	@Schema(description = "Text search on subject and body")
	private Optional<String> fullText = Optional.empty();

	@Schema(description = "Minimum date of creation")
	private Optional<ZonedDateTime> beforeDate = Optional.empty();

	@Schema(description = "Maximum date of creation")
	private Optional<ZonedDateTime> afterDate = Optional.empty();

	@Schema(description = "Sort by")
	private MailSortBy sortBy = DEFAULT_SORT_BY;

	public MailSearch() {
		super();
		sortAscending = DEFAULT_SORT_ASCENDING;
	}

	public Optional<MailOrigin> getOrigin() {
		return origin;
	}

	public MailSearch setOrigin(final Optional<MailOrigin> origin) {
		this.origin = origin;
		return this;
	}

	public Optional<MailStatus> getStatus() {
		return status;
	}

	public MailSearch setStatus(final Optional<MailStatus> status) {
		this.status = status;
		return this;
	}

	public Optional<String> getIntent() {
		return intent;
	}

	public MailSearch setIntent(final Optional<String> intent) {
		this.intent = intent;
		return this;
	}

	public Optional<String> getSender() {
		return sender;
	}

	public MailSearch setSender(final Optional<String> sender) {
		this.sender = sender;
		return this;
	}

	public Optional<String> getRecipient() {
		return recipient;
	}

	public MailSearch setRecipient(final Optional<String> recipient) {
		this.recipient = recipient;
		return this;
	}

	public Optional<String> getFullText() {
		return fullText;
	}

	public MailSearch setFullText(final Optional<String> fullText) {
		this.fullText = fullText;
		return this;
	}

	public Optional<ZonedDateTime> getBeforeDate() {
		return beforeDate;
	}

	public MailSearch setBeforeDate(final Optional<ZonedDateTime> beforeDate) {
		this.beforeDate = beforeDate;
		return this;
	}

	public Optional<ZonedDateTime> getAfterDate() {
		return afterDate;
	}

	public MailSearch setAfterDate(final Optional<ZonedDateTime> afterDate) {
		this.afterDate = afterDate;
		return this;
	}

	public MailSortBy getSortBy() {
		return sortBy;
	}

	public MailSearch setSortBy(final MailSortBy sortBy) {
		this.sortBy = sortBy;
		return this;
	}

}
