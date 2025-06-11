package ch.rodano.configuration.model.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.configuration.utils.DisplayableUtils;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Layout implements Node {
	private static final long serialVersionUID = -2887720419558440587L;

	private FormModel formModel;
	private String id;
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> description;

	private boolean contribution;

	private LayoutType type;
	private String datasetModelId;
	private String defaultSortFieldModelId;

	private List<ColumnHeader> columns;
	private List<Line> lines;

	private SortedMap<String, String> textBefore;
	private SortedMap<String, String> textAfter;

	private String cssCode;

	private RuleConstraint constraint;

	public Layout() {
		shortname = new TreeMap<>();
		description = new TreeMap<>();
		contribution = false;
		columns = new ArrayList<>();
		lines = new ArrayList<>();
		textBefore = new TreeMap<>();
		textAfter = new TreeMap<>();
	}

	public Layout(final Layout layout) {
		formModel = layout.getFormModel();
		id = layout.getId();
		shortname = layout.getShortname();
		description = layout.getDescription();
		contribution = layout.isContribution();
		type = layout.getType();
		datasetModelId = layout.getDatasetModelId();
		columns = layout.getColumns();
		lines = layout.getLines();
		textBefore = layout.getTextBefore();
		textAfter = layout.getTextAfter();
		cssCode = layout.getCssCode();
		constraint = layout.getConstraint();
	}

	@JsonBackReference
	public final FormModel getFormModel() {
		return formModel;
	}

	@JsonBackReference
	public final void setFormModel(final FormModel formModel) {
		this.formModel = formModel;
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@JsonIgnore
	public String getLocalizedShortname(final String... languages) {
		return DisplayableUtils.getLocalizedMap(shortname, languages);
	}

	@JsonIgnore
	public String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getFormModel().getStudy().getDefaultLanguage().getId());
	}

	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	@JsonIgnore
	public final boolean isContribution() {
		return contribution;
	}

	@JsonIgnore
	public final void setContribution(final boolean contribution) {
		this.contribution = contribution;
	}

	public final LayoutType getType() {
		return type;
	}

	public final void setType(final LayoutType type) {
		this.type = type;
	}

	public final String getDatasetModelId() {
		return datasetModelId;
	}

	public final void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public String getDefaultSortFieldModelId() {
		return defaultSortFieldModelId;
	}

	public void setDefaultSortFieldModelId(final String defaultSortFieldModelId) {
		this.defaultSortFieldModelId = defaultSortFieldModelId;
	}

	public final List<ColumnHeader> getColumns() {
		return columns;
	}

	public final void setColumns(final List<ColumnHeader> columns) {
		this.columns = columns;
	}

	@JsonManagedReference
	public final List<Line> getLines() {
		return lines;
	}

	@JsonManagedReference
	public final void setLines(final List<Line> lines) {
		this.lines = lines;
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

	@Override
	public final Entity getEntity() {
		return Entity.LAYOUT;
	}

	public final String getCssCode() {
		return cssCode;
	}

	public final void setCssCode(final String cssCode) {
		this.cssCode = cssCode;
	}

	public final RuleConstraint getConstraint() {
		return constraint;
	}

	public final void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	@JsonIgnore
	public DatasetModel getDatasetModel() {
		return getFormModel().getStudy().getDatasetModel(datasetModelId);
	}

	@JsonIgnore
	public List<Cell> getCells() {
		return getLines().stream().flatMap(l -> l.getCells().stream()).toList();
	}

	@JsonIgnore
	public Cell getCell(final String cellId) {
		return getCells().stream()
			.filter(c -> c.getId().equalsIgnoreCase(cellId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.CELL, cellId));
	}

	@JsonIgnore
	public List<FieldModel> getFieldModels() {
		return getCells().stream().filter(Cell::hasFieldModel).map(Cell::getFieldModel).toList();
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case LINE:
				return Collections.unmodifiableList(lines);
			default:
				return Collections.emptyList();
		}
	}
}
