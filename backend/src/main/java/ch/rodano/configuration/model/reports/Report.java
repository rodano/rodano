package ch.rodano.configuration.model.reports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.rights.Assignable;
import ch.rodano.configuration.model.study.Study;

@JsonPropertyOrder(alphabetic = true)
public class Report implements Cloneable, SuperDisplayable, Serializable, Assignable<Report>, Node {
	private static final long serialVersionUID = 4081701801864249665L;

	private String id;
	private String rawSql;

	private Study study;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private String datasetModelId;
	private String workflowId;
	private List<String> fieldModelIds;

	public Report() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		fieldModelIds = new ArrayList<>();
	}

	public final List<String> getFieldModelIds() {
		return fieldModelIds;
	}

	public final void setFieldModelIds(final List<String> fieldModelIds) {
		this.fieldModelIds = fieldModelIds;
	}

	public final String getRawSql() {
		return rawSql;
	}

	public final void setRawSql(final String rawSql) {
		this.rawSql = rawSql;
	}

	public final String getDatasetModelId() {
		return datasetModelId;
	}

	public final void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public final String getWorkflowId() {
		return workflowId;
	}

	public final void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@JsonIgnore
	public final String getDefaultLocalizedLongname() {
		return getLocalizedLongname(getStudy().getDefaultLanguage().getId());
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getStudy().getDefaultLanguage().getId());
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	@JsonIgnore
	public final String getAssignableDescription() {
		return getDefaultLocalizedShortname();
	}

	@Override
	public final Entity getEntity() {
		return Entity.REPORT;
	}

	@Override
	@JsonIgnore
	public final int compareTo(final Report report) {
		return getId().compareTo(report.getId());
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
