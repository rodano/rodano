package ch.rodano.core.model.audit.models;

import java.time.ZonedDateTime;

import ch.rodano.core.model.audit.AuditTrail;
import ch.rodano.core.model.common.IdentifiableObject;
import ch.rodano.core.model.event.EventRecord;

public final class EventAuditTrail extends EventRecord implements IdentifiableObject, AuditTrail {

	private Long pk;

	private Long auditObjectFk;
	private Long auditActionFk;
	private String auditActor;
	private Long auditUserFk;
	private Long auditRobotFk;
	private ZonedDateTime auditDatetime;
	private String auditContext;

	public EventAuditTrail() {
		super();
	}

	@Override
	public Long getPk() {
		return pk;
	}

	@Override
	public void setPk(final Long pk) {
		this.pk = pk;
	}

	@Override
	public Long getAuditObjectFk() {
		return auditObjectFk;
	}

	@Override
	public void setAuditObjectFk(final Long auditObjectFk) {
		this.auditObjectFk = auditObjectFk;
	}

	@Override
	public Long getAuditActionFk() {
		return auditActionFk;
	}

	@Override
	public void setAuditActionFk(final Long auditActionFk) {
		this.auditActionFk = auditActionFk;
	}

	@Override
	public String getAuditActor() {
		return auditActor;
	}

	@Override
	public void setAuditActor(final String auditActor) {
		this.auditActor = auditActor;
	}

	@Override
	public Long getAuditUserFk() {
		return auditUserFk;
	}

	@Override
	public void setAuditUserFk(final Long auditUserFk) {
		this.auditUserFk = auditUserFk;
	}

	@Override
	public Long getAuditRobotFk() {
		return auditRobotFk;
	}

	@Override
	public void setAuditRobotFk(final Long auditRobotFk) {
		this.auditRobotFk = auditRobotFk;
	}

	@Override
	public ZonedDateTime getAuditDatetime() {
		return auditDatetime;
	}

	@Override
	public void setAuditDatetime(final ZonedDateTime auditDatetime) {
		this.auditDatetime = auditDatetime;
	}

	@Override
	public String getAuditContext() {
		return auditContext;
	}

	@Override
	public void setAuditContext(final String auditContext) {
		this.auditContext = auditContext;
	}
}
