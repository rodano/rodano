package ch.rodano.configuration.model.predicate;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.study.Study;

/**
 * Use a FieldModelCriterion when possible
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class ValueSource implements Serializable {
	private static final long serialVersionUID = -1527838562630244654L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ValueSource.class);

	private Study study;

	private boolean forStatistics;
	private String scopeModelId;
	private String eventModelId;
	private String datasetModelId;
	private String fieldModelId;
	private ValueSourceCriteria criteria;

	private EventSource eventSource;

	private String eventSourceParameter;

	private boolean ignoreNull;
	private String rawSql;

	@JsonIgnore
	public Study getStudy() {
		return study;
	}

	@JsonIgnore
	public void setStudy(final Study study) {
		this.study = study;
	}

	@Override
	public String toString() {
		return String.format("scopeModelId : %s / eventSource : %s / eventModelId : %s / datasetModelId : %s / fieldModelId : %s /", scopeModelId, eventSource, eventModelId, datasetModelId, fieldModelId);
	}

	@JsonIgnore
	public final String getSqlFieldModel() {
		return "count(doc.scope_fk) as total";
	}

	@JsonIgnore
	public final String getSqlEvent() {
		final var sql = new StringBuilder();

		if(StringUtils.isNoneBlank(eventModelId)) {
			sql.append(" and doc.event_model_id = '");
			sql.append(getEventModelId());
			sql.append("' ");
		}

		if(eventSource != null) {
			if(eventSource.equals(EventSource.POSITION)) {
				sql.append(" and doc.event_group_number = ");
				sql.append(eventSourceParameter);
				sql.append(" ");
			}
			else if(eventSource.equals(EventSource.BEFORE)) {
				sql.append(" and doc.event_date < '");
				sql.append(eventSourceParameter);
				sql.append("' ");
			}
			else if(eventSource.equals(EventSource.AFTER)) {
				sql.append(" and doc.event_date > '");
				sql.append(eventSourceParameter);
				sql.append("' ");
			}
			else if(eventSource.equals(EventSource.FIRST) || eventSource.equals(EventSource.LAST)) {
				final var events = new StringBuilder();
				events.append("(");
				var first = true;

				final var datasetModelEventModels = getDatasetModel().getEventModels();
				if(datasetModelEventModels.size() == 1 && datasetModelEventModels.get(0).isInceptive()) {
					System.err.println(String.format("You should set parameter event to %s and delete event source parameter in chart configuration because fieldModel %s appears only on inceptive event! "
						+ "You'll get a huge performance boost!", datasetModelEventModels.get(0).getId(), fieldModelId));
				}

				for(final var e : datasetModelEventModels) {
					if(!first) {
						events.append(",");
					}

					first = false;
					events.append("'");
					events.append(e.getId());
					events.append("'");
				}

				events.append(")");

				sql.append(" and ");
				sql.append("doc.event_date = ");

				//build where
				if(eventSource.equals(EventSource.LAST)) {
					sql.append("(select max(vmax.date) from visit vmax where vmax.scope_fk = doc.scope_fk and vmax.expected = 0 and vmax.event_model_id in ");
				}
				else {
					sql.append("(select min(vmin.date) from visit vmin where vmin.scope_fk = doc.scope_fk and vmin.expected = 0 and vmin.event_model_id in ");
				}

				sql.append(events);
				sql.append(")");
			}
		}

		return sql.toString();
	}

	public final String getFieldModelId() {
		return fieldModelId;
	}

	public final void setFieldModelId(final String fieldModelId) {
		this.fieldModelId = fieldModelId;
	}

	public final String getEventModelId() {
		return eventModelId;
	}

	public final void setEventModelId(final String eventId) {
		this.eventModelId = eventId;
	}

	@JsonIgnore
	public final DatasetModel getDatasetModel() {
		return study.getDatasetModel(getDatasetModelId());
	}

	public final String getDatasetModelId() {
		return datasetModelId;
	}

	public final void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public final boolean isForStatistics() {
		return forStatistics;
	}

	public final void setForStatistics(final boolean forStatistics) {
		this.forStatistics = forStatistics;
	}

	public final String getScopeModelId() {
		return scopeModelId;
	}

	public final void setScopeModelId(final String scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public final ValueSourceCriteria getCriteria() {
		return criteria;
	}

	public final void setCriteria(final ValueSourceCriteria criteria) {
		this.criteria = criteria;
	}

	public final EventSource getEventSource() {
		return eventSource;
	}

	public final void setEventSource(final EventSource eventSource) {
		this.eventSource = eventSource;
	}

	public final String getEventSourceParameter() {
		return eventSourceParameter;
	}

	public final void setEventSourceParameter(final String eventSourceParameter) {
		this.eventSourceParameter = eventSourceParameter;
	}

	public final boolean getIgnoreNull() {
		return ignoreNull;
	}

	public final void setIgnoreNull(final boolean ignoreNull) {
		this.ignoreNull = ignoreNull;
	}

	public final String getRawSql() {
		return rawSql;
	}

	public final void setRawSql(final String rawSql) {
		this.rawSql = rawSql;
	}

	public String getEntity() {
		return "VALUE_SOURCE";
	}

	@JsonAnySetter
	public void setAnySetter(final String key, final Object value) {
		if(!"entity".equals(key)) {
			final var message = new StringBuilder("ValueSource - Unknown property : ");
			message.append(key);
			message.append(" - ");
			message.append(value);
			LOGGER.error(message.toString());
		}
	}
}
