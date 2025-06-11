package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.layout.LayoutType;

public class LayoutDTO {
	@NotNull
	Long scopePk;
	Long eventPk;

	@NotBlank
	String id;
	@NotBlank
	String formModelId;

	SortedMap<String, String> description;

	@NotNull
	LayoutType type;

	@NotNull
	DatasetModelDTO datasetModel;
	String defaultSortFieldModelId;

	@NotNull
	Boolean contribution;

	@NotNull
	List<ColumnHeaderDTO> columns;
	@NotNull
	List<LayoutLineDTO> lines;

	SortedMap<String, String> textBefore;
	SortedMap<String, String> textAfter;

	String cssCode;

	public String getFormModelId() {
		return formModelId;
	}

	public void setFormModelId(final String formModelId) {
		this.formModelId = formModelId;
	}

	public Long getScopePk() {
		return scopePk;
	}

	public void setScopePk(final Long scopePk) {
		this.scopePk = scopePk;
	}

	public Long getEventPk() {
		return eventPk;
	}

	public void setEventPk(final Long eventPk) {
		this.eventPk = eventPk;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public LayoutType getType() {
		return type;
	}

	public void setType(final LayoutType type) {
		this.type = type;
	}

	public DatasetModelDTO getDatasetModel() {
		return datasetModel;
	}

	public void setDatasetModel(final DatasetModelDTO datasetModel) {
		this.datasetModel = datasetModel;
	}

	public String getDefaultSortFieldModelId() {
		return defaultSortFieldModelId;
	}

	public void setDefaultSortFieldModelId(final String defaultSortFieldModelId) {
		this.defaultSortFieldModelId = defaultSortFieldModelId;
	}

	public Boolean getContribution() {
		return contribution;
	}

	public void setContribution(final Boolean contribution) {
		this.contribution = contribution;
	}

	public List<ColumnHeaderDTO> getColumns() {
		return columns;
	}

	public void setColumns(final List<ColumnHeaderDTO> columns) {
		this.columns = columns;
	}

	public List<LayoutLineDTO> getLines() {
		return lines;
	}

	public void setLines(final List<LayoutLineDTO> lines) {
		this.lines = lines;
	}

	public SortedMap<String, String> getTextBefore() {
		return textBefore;
	}

	public void setTextBefore(final SortedMap<String, String> textBefore) {
		this.textBefore = textBefore;
	}

	public SortedMap<String, String> getTextAfter() {
		return textAfter;
	}

	public void setTextAfter(final SortedMap<String, String> textAfter) {
		this.textAfter = textAfter;
	}

	public String getCssCode() {
		return cssCode;
	}

	public void setCssCode(final String cssCode) {
		this.cssCode = cssCode;
	}
}
