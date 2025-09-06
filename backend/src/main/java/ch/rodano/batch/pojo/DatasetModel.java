package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetModel {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private boolean multiple;
	private boolean master;
	private boolean exportable;
	private Integer exportOrder;
	private String collapsedLabelPattern;
	private String expandedLabelPattern;
	private String family;

	private List<Rule> deleteRules;
	private List<Rule> restoreRules;

	private List<FieldModel> fieldModels;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public Map<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final Map<String, String> longname) {
		this.longname = longname;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(final boolean multiple) {
		this.multiple = multiple;
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster(final boolean master) {
		this.master = master;
	}

	public boolean isExportable() {
		return exportable;
	}

	public void setExportable(final boolean exportable) {
		this.exportable = exportable;
	}

	public Integer getExportOrder() {
		return exportOrder;
	}

	public void setExportOrder(final Integer exportOrder) {
		this.exportOrder = exportOrder;
	}

	public String getCollapsedLabelPattern() {
		return collapsedLabelPattern;
	}

	public void setCollapsedLabelPattern(final String collapsedLabelPattern) {
		this.collapsedLabelPattern = collapsedLabelPattern;
	}

	public String getExpandedLabelPattern() {
		return expandedLabelPattern;
	}

	public void setExpandedLabelPattern(final String expandedLabelPattern) {
		this.expandedLabelPattern = expandedLabelPattern;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(final String family) {
		this.family = family;
	}

	public List<Rule> getDeleteRules() {
		return deleteRules;
	}

	public void setDeleteRules(final List<Rule> deleteRules) {
		this.deleteRules = deleteRules;
	}

	public List<Rule> getRestoreRules() {
		return restoreRules;
	}

	public void setRestoreRules(final List<Rule> restoreRules) {
		this.restoreRules = restoreRules;
	}

	public List<FieldModel> getFieldModels() {
		return fieldModels;
	}

	public void setFieldModels(final List<FieldModel> fieldModels) {
		this.fieldModels = fieldModels;
	}

	@Override
	public String toString() {
		return "DatasetModel{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", multiple=" + multiple +
			", master=" + master +
			", exportable=" + exportable +
			", exportOrder=" + exportOrder +
			", collapsedLabelPattern='" + collapsedLabelPattern + '\'' +
			", expandedLabelPattern='" + expandedLabelPattern + '\'' +
			", family='" + family + '\'' +
			", deleteRules=" + deleteRules +
			", restoreRules=" + restoreRules +
			", fieldModels=" + fieldModels +
			'}';
	}
}
