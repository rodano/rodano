package ch.rodano.api.config;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A cell in the CRF display. Can contain a field or static information. Can be displayed conditionally based on specified criteria.")
public class CellDTO {
	@NotBlank
	String id;

	@NotBlank
	String datasetModelId;
	@NotBlank
	String fieldModelId;

	SortedMap<String, String> textBefore;
	SortedMap<String, String> textAfter;

	@NotNull
	List<VisibilityCriteriaDTO> visibilityCriteria;

	@NotNull
	boolean displayLabel;
	@NotNull
	boolean displayPossibleValueLabels;

	Integer possibleValuesColumnNumber;
	Integer possibleValuesColumnWidth;

	int colspan;

	@NotNull
	boolean hasPrintButton;
	Map<String, String> printButtonLabel;

	String addEventButton;
	Map<String, String> addEventButtonLabel;

	FieldModelDTO fieldModel;

	String cssCodeForLabel;
	String cssCodeForInput;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public String getFieldModelId() {
		return fieldModelId;
	}

	public void setFieldModelId(final String fieldModelId) {
		this.fieldModelId = fieldModelId;
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

	public List<VisibilityCriteriaDTO> getVisibilityCriteria() {
		return visibilityCriteria;
	}

	public void setVisibilityCriteria(final List<VisibilityCriteriaDTO> visibilityCriteria) {
		this.visibilityCriteria = visibilityCriteria;
	}

	public boolean getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(final boolean displayLabel) {
		this.displayLabel = displayLabel;
	}

	public boolean isDisplayPossibleValueLabels() {
		return displayPossibleValueLabels;
	}

	public void setDisplayPossibleValueLabels(final boolean displayPossibleValueLabels) {
		this.displayPossibleValueLabels = displayPossibleValueLabels;
	}

	public Integer getPossibleValuesColumnNumber() {
		return possibleValuesColumnNumber;
	}

	public void setPossibleValuesColumnNumber(final Integer possibleValuesColumnNumber) {
		this.possibleValuesColumnNumber = possibleValuesColumnNumber;
	}

	public Integer getPossibleValuesColumnWidth() {
		return possibleValuesColumnWidth;
	}

	public void setPossibleValuesColumnWidth(final Integer possibleValuesColumnWidth) {
		this.possibleValuesColumnWidth = possibleValuesColumnWidth;
	}

	public int getColspan() {
		return colspan;
	}

	public void setColspan(final int colspan) {
		this.colspan = colspan;
	}

	public boolean isHasPrintButton() {
		return hasPrintButton;
	}

	public void setHasPrintButton(final boolean hasPrintButton) {
		this.hasPrintButton = hasPrintButton;
	}

	public Map<String, String> getPrintButtonLabel() {
		return printButtonLabel;
	}

	public void setPrintButtonLabel(final Map<String, String> printButtonLabel) {
		this.printButtonLabel = printButtonLabel;
	}

	public String getAddEventButton() {
		return addEventButton;
	}

	public void setAddEventButton(final String addEventButton) {
		this.addEventButton = addEventButton;
	}

	public Map<String, String> getAddEventButtonLabel() {
		return addEventButtonLabel;
	}

	public void setAddEventButtonLabel(final Map<String, String> addEventButtonLabel) {
		this.addEventButtonLabel = addEventButtonLabel;
	}

	public FieldModelDTO getFieldModel() {
		return fieldModel;
	}

	public void setFieldModel(final FieldModelDTO fieldModel) {
		this.fieldModel = fieldModel;
	}

	public String getCssCodeForLabel() {
		return cssCodeForLabel;
	}

	public void setCssCodeForLabel(final String cssCodeForLabel) {
		this.cssCodeForLabel = cssCodeForLabel;
	}

	public String getCssCodeForInput() {
		return cssCodeForInput;
	}

	public void setCssCodeForInput(final String cssCodeForInput) {
		this.cssCodeForInput = cssCodeForInput;
	}
}
