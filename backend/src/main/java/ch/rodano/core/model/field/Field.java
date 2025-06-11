package ch.rodano.core.model.field;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.configuration.model.workflow.WorkflowableModel;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.common.PersistentObject;
import ch.rodano.core.model.common.TimestampableObject;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.sql.SQLValue;
import ch.rodano.core.model.workflow.Workflowable;

@JsonInclude(Include.NON_NULL)
public class Field extends FieldRecord implements TimestampableObject, PersistentObject, AuditableObject, Serializable, SQLValue, Workflowable, Comparable<Field>, Evaluable {
	private static final long serialVersionUID = 6565374409857436890L;

	private static final DateTimeFormatter SQL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	private DatasetModel datasetModel;
	private FieldModel fieldModel;

	public Field() {
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

	@Override
	public final String getId() {
		return fieldModelId;
	}

	public void setDatasetModel(final DatasetModel datasetModel) {
		this.datasetModel = datasetModel;
		this.datasetModelId = datasetModel.getId();
	}

	public void setFieldModel(final FieldModel fieldModel) {
		this.fieldModel = fieldModel;
		this.fieldModelId = fieldModel.getId();
	}

	@JsonIgnore
	public DatasetModel getDatasetModel() {
		return datasetModel;
	}

	@JsonIgnore
	public FieldModel getFieldModel() {
		return fieldModel;
	}

	@JsonIgnore
	public Object getObjectValue() {
		return getFieldModel().stringToObject(value);
	}

	@Override
	@JsonIgnore
	public int compareTo(final Field otherField) {
		if(this == otherField) {
			return 0;
		}
		if(datasetFk.equals(otherField.datasetFk)) {
			throw new RuntimeException("Cannot compare two fields in different datasets");
		}

		return Comparator
			.comparing(Field::getFieldModel)
			.thenComparing(Field::getCreationTime)
			.thenComparing(Field::getPk)
			.compare(this, otherField);
	}

	@Override
	@JsonIgnore
	public String getSqlValue() {
		//special handling of numbers
		if(OperandType.NUMBER.equals(fieldModel.getDataType())) {
			if(value == null || value.isEmpty()) {
				return null;
			}

			return value.trim();
		}

		//special handling of dates
		if(FieldModelType.DATE.equals(fieldModel.getType())) {
			if(value == null || value.isEmpty()) {
				return null;
			}

			final var dateValue = (PartialDate) fieldModel.stringToObject(value);
			if(!dateValue.isAnchoredInTime()) {
				return null;
			}
			return dateValue.toZonedDateTime().get().format(SQL_FORMAT);
		}

		//for every other case return raw value
		return value;
	}

	/**
	 * @return true if value is null, empty or whitespace
	 */
	@JsonIgnore
	public boolean isBlank() {
		return StringUtils.isBlank(value);
	}

	/**
	 * @return true if value is null
	 */
	@JsonIgnore
	public boolean isNull() {
		return value == null;
	}

	@JsonAnySetter
	public void setAnySetter(final String key, final Object value) {
		final var message = new StringBuilder("Field - Unknown property : ");
		message.append(key);
		message.append(" - ");
		message.append(value);
		System.err.println(message);
	}

	@Override
	public WorkflowableEntity getWorkflowableEntity() {
		return WorkflowableEntity.FIELD;
	}

	@Override
	@JsonIgnore
	public WorkflowableModel getWorkflowableModel() {
		return getFieldModel();
	}

	@Override
	@JsonIgnore
	public RulableEntity getRulableEntity() {
		return RulableEntity.FIELD;
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
		fieldModel = datasetModel.getFieldModel(fieldModelId);
	}
}
