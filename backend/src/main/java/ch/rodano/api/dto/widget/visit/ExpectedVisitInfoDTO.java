package ch.rodano.api.dto.widget.visit;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.core.model.scope.Scope;

public class ExpectedVisitInfoDTO implements Comparable<ExpectedVisitInfoDTO> {
	private String scopeCode;
	private String scopeId;
	private Long eventPk;
	private Long scopePk;
	private String parentScopeCode;
	private ZonedDateTime lastEventDate;
	private ZonedDateTime dateOfEvent;
	private String comment;

	@JsonIgnore
	private Scope scope;

	/**
	 * Constructor
	 *
	 * @param scopeCode       The scope code
	 * @param scopeId         The scope id
	 * @param eventPk         The event pk
	 * @param scopePk         The scope pk
	 * @param parentScopeCode The parent scope
	 * @param lastEventDate   The last event date
	 * @param dateOfEvent     The date of event
	 * @param comment         The comment
	 */
	public ExpectedVisitInfoDTO(
		final String scopeCode,
		final String scopeId,
		final Long eventPk,
		final Long scopePk,
		final String parentScopeCode,
		final ZonedDateTime lastEventDate,
		final ZonedDateTime dateOfEvent,
		final String comment
	) {
		this.scopeCode = scopeCode;
		this.scopeId = scopeId;
		this.eventPk = eventPk;
		this.scopePk = scopePk;
		this.parentScopeCode = parentScopeCode;
		this.lastEventDate = lastEventDate;
		this.dateOfEvent = dateOfEvent;
		this.comment = comment;
	}

	public String getScopeCode() {
		return scopeCode;
	}

	public void setScopeCode(final String scopeCode) {
		this.scopeCode = scopeCode;
	}

	public String getScopeId() {
		return scopeId;
	}

	public void setScopeId(final String scopeId) {
		this.scopeId = scopeId;
	}

	public Long getEventPk() {
		return eventPk;
	}

	public void setEventPk(final Long eventPk) {
		this.eventPk = eventPk;
	}

	public Long getScopePk() {
		return scopePk;
	}

	public void setScopePk(final Long scopePk) {
		this.scopePk = scopePk;
	}

	public String getParentScopeCode() {
		return parentScopeCode;
	}

	public void setParentScopeCode(final String parentScopeCode) {
		this.parentScopeCode = parentScopeCode;
	}

	public ZonedDateTime getLastEventDate() {
		return lastEventDate;
	}

	public void setLastEventDate(final ZonedDateTime lastEventDate) {
		this.lastEventDate = lastEventDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public ZonedDateTime getDateOfEvent() {
		return dateOfEvent;
	}

	public void setDateOfEvent(final ZonedDateTime dateOfEvent) {
		this.dateOfEvent = dateOfEvent;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(final Scope scope) {
		this.scope = scope;
	}

	@Override
	public int compareTo(final ExpectedVisitInfoDTO o) {
		if(scope != null) {
			return scope.compareTo(o.getScope());
		}

		return -1;
	}
}
