package ch.rodano.core.model.jooqutils;

import java.time.ZonedDateTime;

public interface AuditRecord {

	Long getAuditActionFk();

	void setAuditActionFk(final Long actionFk);

	String getAuditActor();

	void setAuditActor(final String actor);

	Long getAuditUserFk();

	void setAuditUserFk(final Long userFk);

	Long getAuditRobotFk();

	void setAuditRobotFk(final Long robotFk);

	ZonedDateTime getAuditDatetime();

	void setAuditDatetime(final ZonedDateTime datetime);

	String getAuditContext();

	void setAuditContext(final String context);

	Long getAuditObjectFk();

	void setAuditObjectFk(final Long objectFk);

}
