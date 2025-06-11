package ch.rodano.core.model.dataset;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.PersistentObject;
import ch.rodano.core.model.common.TimestampableObject;
import ch.rodano.core.model.rules.Evaluable;

public class Dataset extends DatasetRecord implements DeletableObject, TimestampableObject, PersistentObject, AuditableObject, Evaluable, Comparable<Dataset> {

	public static final Comparator<Dataset> DEFAULT_COMPARATOR = Comparator
		.comparing(Dataset::getDatasetModel)
		.thenComparing(Dataset::getCreationTime)
		.thenComparing(Dataset::getPk);

	private Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	private DatasetModel datasetModel;

	public Dataset() {
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

	public DatasetModel getDatasetModel() {
		return datasetModel;
	}

	public void setDatasetModel(final DatasetModel datasetModel) {
		this.datasetModel = datasetModel;
		this.datasetModelId = datasetModel.getId();
	}

	@Override
	public RulableEntity getRulableEntity() {
		return RulableEntity.DATASET;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(final Object o) {
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		final var dataset = (Dataset) o;
		if(id == null && dataset.id == null) {
			return this == o;
		}
		return Objects.equals(id, dataset.id);
	}

	@Override
	public int compareTo(final Dataset otherDataset) {
		//preserve consistency between equals and comparator
		if(equals(otherDataset)) {
			return 0;
		}
		if(scopeFk != null && !scopeFk.equals(otherDataset.scopeFk) || eventFk != null && !eventFk.equals(otherDataset.eventFk)) {
			throw new RuntimeException("Cannot compare two datasets in different scopes or visits");
		}
		return DEFAULT_COMPARATOR.compare(this, otherDataset);
	}

	@Override
	public void onPreUpdate() {
		//nothing
	}

	@Override
	public void onPostUpdate(final Study study) {
		//nothing
	}

	@Override
	public void onPostLoad(final Study study) {
		datasetModel = study.getDatasetModel(datasetModelId);
	}
}
