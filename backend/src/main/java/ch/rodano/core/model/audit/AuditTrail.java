package ch.rodano.core.model.audit;

import java.time.ZonedDateTime;
import java.util.Comparator;

import ch.rodano.core.model.common.IdentifiableObject;

public interface AuditTrail extends Comparable<AuditTrail>, IdentifiableObject {

	static Comparator<AuditTrail> DEFAULT_COMPARATOR = Comparator.comparing(AuditTrail::getAuditDatetime)
		.thenComparing(AuditTrail::getPk);

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

	/**
	 * All versions are sorted starting from oldest to latest.
	 * If the two trails have the same timestamp, we rely on their PKs.
	 */
	@Override
	default int compareTo(final AuditTrail o) {
		return DEFAULT_COMPARATOR.compare(this, o);
	}
}
