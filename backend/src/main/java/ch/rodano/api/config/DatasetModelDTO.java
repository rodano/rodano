package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dataset model")
public class DatasetModelDTO implements Comparable<DatasetModelDTO> {
	@Schema(description = "Unique document UUID")
	@NotNull
	UUID datasetModelId;
	@Schema(description = "Unique document ID")
	@NotBlank
	String id;
	@NotNull
	SortedMap<String, String> shortname;

	SortedMap<String, String> longname;
	SortedMap<String, String> description;

	@Schema(description = "Is the document repeatable?")
	@NotNull
	boolean multiple;
	@Schema(description = "Can the document be exported?")
	@NotNull
	boolean exportable;

	boolean master;
	Integer exportOrder;
	String family;

	@Schema(description = "Is the document attached directly to a scope?")
	@NotNull
	boolean scopeDocumentation;
	@Schema(description = "Field models")
	@NotNull
	List<FieldModelDTO> fieldModels;
	@Deprecated
	@Schema(description = "The label pattern when the multiple document is expanded")
	String expandedLabelPattern;
	@Deprecated
	@Schema(description = "The label pattern when the multiple document is collapsed")
	String collapsedLabelPattern;

	@Schema(description = "Does the current user have write permission on this document")
	@NotNull
	boolean canWrite;

	public UUID getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final UUID datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(final boolean multiple) {
		this.multiple = multiple;
	}

	public boolean isExportable() {
		return exportable;
	}

	public void setExportable(final boolean exportable) {
		this.exportable = exportable;
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster(final boolean master) {
		this.master = master;
	}

	public Integer getExportOrder() {
		return exportOrder;
	}

	public void setExportOrder(final Integer exportOrder) {
		this.exportOrder = exportOrder;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(final String family) {
		this.family = family;
	}

	public boolean isScopeDocumentation() {
		return scopeDocumentation;
	}

	public void setScopeDocumentation(final boolean scopeDocumentation) {
		this.scopeDocumentation = scopeDocumentation;
	}

	public List<FieldModelDTO> getFieldModels() {
		return fieldModels;
	}

	public void setFieldModels(final List<FieldModelDTO> fieldModels) {
		this.fieldModels = fieldModels;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public String getExpandedLabelPattern() {
		return expandedLabelPattern;
	}

	public void setExpandedLabelPattern(final String expandedLabelPattern) {
		this.expandedLabelPattern = expandedLabelPattern;
	}

	public String getCollapsedLabelPattern() {
		return collapsedLabelPattern;
	}

	public void setCollapsedLabelPattern(final String collapsedLabelPattern) {
		this.collapsedLabelPattern = collapsedLabelPattern;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(final boolean canWrite) {
		this.canWrite = canWrite;
	}

	@Override
	public int compareTo(final DatasetModelDTO o) {
		return this.id.compareTo(o.id);
	}
}
