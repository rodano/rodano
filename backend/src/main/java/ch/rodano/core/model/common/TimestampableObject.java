package ch.rodano.core.model.common;

import java.time.ZonedDateTime;

public interface TimestampableObject {

	ZonedDateTime getCreationTime();

	void setCreationTime(ZonedDateTime creationTime);

	ZonedDateTime getLastUpdateTime();

	void setLastUpdateTime(ZonedDateTime lastUpdateTime);
}
