package ch.rodano.core.model.file;

import java.time.ZonedDateTime;

import ch.rodano.core.model.common.HardDeletableObject;
import ch.rodano.core.model.common.IdentifiableObject;
import ch.rodano.core.model.common.TimestampableObject;

public class File implements IdentifiableObject, TimestampableObject, HardDeletableObject {

	protected Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	private Long userFk;
	private Long scopeFk;
	private Long eventFk;
	private Long datasetFk;
	private Long fieldFk;
	private Long trailFk;
	private String uuid;
	private String name;
	private byte[] checksum;
	private boolean submitted;

	public File() {}

	@Override
	public Long getPk() {
		return pk;
	}

	@Override
	public void setPk(final Long pk) {
		this.pk = pk;
	}

	@Override
	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	@Override
	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	@Override
	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public Long getScopeFk() {
		return scopeFk;
	}

	public void setScopeFk(final Long scopeFk) {
		this.scopeFk = scopeFk;
	}

	public Long getEventFk() {
		return eventFk;
	}

	public void setEventFk(final Long eventFk) {
		this.eventFk = eventFk;
	}

	public Long getDatasetFk() {
		return datasetFk;
	}

	public void setDatasetFk(final Long datasetFk) {
		this.datasetFk = datasetFk;
	}

	public final Long getFieldFk() {
		return fieldFk;
	}

	public final void setFieldFk(final Long fieldFk) {
		this.fieldFk = fieldFk;
	}

	public Long getUserFk() {
		return userFk;
	}

	public void setUserFk(final Long userFk) {
		this.userFk = userFk;
	}

	public final Long getTrailFk() {
		return trailFk;
	}

	public final void setTrailFk(final Long trailFk) {
		this.trailFk = trailFk;
	}

	public final boolean isSubmitted() {
		return submitted;
	}

	public final void setSubmitted(final boolean submitted) {
		this.submitted = submitted;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public void setChecksum(final byte[] checksum) {
		this.checksum = checksum;
	}

	public byte[] getChecksum() {
		return checksum;
	}
}
