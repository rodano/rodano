package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormModelLayoutCell {

	private String id;
	private Map<String, String> textBefore;
	private Map<String, String> textAfter;

	private String datasetModelId;
	private String fieldModelId;

	private String cssCodeForLabel;
	private String cssCodeForInput;
	private boolean displayLabel;
	private boolean displayPossibleValueLabels;
	private Integer colspan;
	private Integer possibleValuesColumnNumber;
	private Integer possibleValuesColumnWidth;

	private List<FormCellVisibilityCriteria> visibilityCriteria;

	private RuleConstraint constraint;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getTextBefore() {
		return textBefore;
	}

	public void setTextBefore(final Map<String, String> textBefore) {
		this.textBefore = textBefore;
	}

	public Map<String, String> getTextAfter() {
		return textAfter;
	}

	public void setTextAfter(final Map<String, String> textAfter) {
		this.textAfter = textAfter;
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

	public boolean isDisplayLabel() {
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

	public Integer getColspan() {
		return colspan;
	}

	public void setColspan(final Integer colspan) {
		this.colspan = colspan;
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

	public List<FormCellVisibilityCriteria> getVisibilityCriteria() {
		return visibilityCriteria;
	}

	public void setVisibilityCriteria(final List<FormCellVisibilityCriteria> visibilityCriteria) {
		this.visibilityCriteria = visibilityCriteria;
	}

	public RuleConstraint getConstraint() {
		return constraint;
	}

	public void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	@Override
	public String toString() {
		return "FormModelLayoutCell{" +
			"id='" + id + '\'' +
			", textBefore=" + textBefore +
			", textAfter=" + textAfter +
			", datasetModelId='" + datasetModelId + '\'' +
			", fieldModelId='" + fieldModelId + '\'' +
			", cssCodeForLabel='" + cssCodeForLabel + '\'' +
			", cssCodeForInput='" + cssCodeForInput + '\'' +
			", displayLabel=" + displayLabel +
			", displayPossibleValueLabels=" + displayPossibleValueLabels +
			", colspan=" + colspan +
			", possibleValuesColumnNumber=" + possibleValuesColumnNumber +
			", possibleValuesColumnWidth=" + possibleValuesColumnWidth +
			", visibilityCriteria=" + visibilityCriteria +
			", constraint=" + constraint +
			'}';
	}
}
