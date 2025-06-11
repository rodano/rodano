package ch.rodano.configuration.model.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.utils.DisplayableUtils;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Cell implements Node {
	private static final long serialVersionUID = 7222424955705312814L;

	private String id;
	private Line line;

	private String datasetModelId;
	private String fieldModelId;

	private SortedMap<String, String> textBefore;
	private SortedMap<String, String> textAfter;

	private RuleConstraint constraint;
	private List<VisibilityCriteria> visibilityCriteria;

	private String cssCodeForLabel;
	private String cssCodeForInput;

	private boolean displayLabel;
	private boolean displayPossibleValueLabels;

	private Integer possibleValuesColumnNumber;
	private Integer possibleValuesColumnWidth;

	private int colspan = 1;

	public Cell() {
		textBefore = new TreeMap<>();
		textAfter = new TreeMap<>();
		visibilityCriteria = new ArrayList<>();
	}

	@JsonBackReference
	public final Line getLine() {
		return line;
	}

	@JsonBackReference
	public final void setLine(final Line line) {
		this.line = line;
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	public Entity getEntity() {
		return Entity.CELL;
	}

	public final String getDatasetModelId() {
		return datasetModelId;
	}

	public final void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public final String getFieldModelId() {
		return fieldModelId;
	}

	public final void setFieldModelId(final String fieldModelId) {
		this.fieldModelId = fieldModelId;
	}

	public final String getCssCodeForInput() {
		return cssCodeForInput;
	}

	public final void setCssCodeForInput(final String cssCodeForInput) {
		this.cssCodeForInput = cssCodeForInput;
	}

	public final String getCssCodeForLabel() {
		return cssCodeForLabel;
	}

	public final void setCssCodeForLabel(final String cssCodeForLabel) {
		this.cssCodeForLabel = cssCodeForLabel;
	}

	public final int getColspan() {
		return colspan;
	}

	public final void setColspan(final int colspan) {
		this.colspan = colspan;
	}

	public final RuleConstraint getConstraint() {
		return constraint;
	}

	public final void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	@JsonManagedReference
	public List<VisibilityCriteria> getVisibilityCriteria() {
		return visibilityCriteria;
	}

	@JsonManagedReference
	public void setVisibilityCriteria(final List<VisibilityCriteria> visibilityCriteria) {
		this.visibilityCriteria = visibilityCriteria;
	}

	public final SortedMap<String, String> getTextBefore() {
		return textBefore;
	}

	public final void setTextBefore(final SortedMap<String, String> textBefore) {
		this.textBefore = textBefore;
	}

	@JsonIgnore
	public final String getLocalizedTextBefore(final String... languages) {
		return DisplayableUtils.getLocalizedMap(textBefore, languages);
	}

	public final SortedMap<String, String> getTextAfter() {
		return textAfter;
	}

	public final void setTextAfter(final SortedMap<String, String> textAfter) {
		this.textAfter = textAfter;
	}

	@JsonIgnore
	public final String getLocalizedTextAfter(final String... languages) {
		return DisplayableUtils.getLocalizedMap(textAfter, languages);
	}

	public final boolean getDisplayLabel() {
		return displayLabel;
	}

	public final void setDisplayLabel(final boolean displayLabel) {
		this.displayLabel = displayLabel;
	}

	public boolean getDisplayPossibleValueLabels() {
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

	@JsonIgnore
	public boolean hasFieldModel() {
		return StringUtils.isNoneBlank(datasetModelId, fieldModelId);
	}

	public boolean containsFieldModel(final FieldModel fieldModel) {
		return hasFieldModel() && fieldModel.getDatasetModel().getId().equals(datasetModelId) && fieldModel.getId().equals(fieldModelId);
	}

	@JsonIgnore
	public DatasetModel getDatasetModel() {
		if(!hasFieldModel()) {
			throw new NoRespectForConfigurationException(String.format("Cell %s does not refer to any field model", id));
		}
		return getStudy().getDatasetModel(datasetModelId);
	}

	@JsonIgnore
	public FieldModel getFieldModel() {
		return getDatasetModel().getFieldModel(fieldModelId);
	}

	@JsonIgnore
	public Study getStudy() {
		return line.getLayout().getFormModel().getStudy();
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
