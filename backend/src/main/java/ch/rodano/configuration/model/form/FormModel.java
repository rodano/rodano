package ch.rodano.configuration.model.form;

import java.io.Serializable;
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
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowableModel;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class FormModel implements Serializable, SuperDisplayable, RightAssignable<FormModel> , WorkflowableModel, Node, Comparable<FormModel> {
	private static final long serialVersionUID = 6338027622028458338L;

	private String id;
	private Study study;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private boolean optional;
	private boolean contribution;
	//save rules
	private List<Rule> rules;

	private List<Layout> layouts;
	private List<String> workflowIds;
	private RuleConstraint constraint;

	private String xslTemplate;
	private String xslFilename;
	private SortedMap<String, String> printButtonLabel;

	public FormModel() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		layouts = new ArrayList<>();
		workflowIds = new ArrayList<>();
		printButtonLabel = new TreeMap<>();
	}

	public FormModel(final FormModel formModel) {
		id = formModel.getId();
		study = formModel.getStudy();
		shortname = formModel.getShortname();
		longname = formModel.getLongname();
		description = formModel.getDescription();
		optional = formModel.isOptional();
		layouts = formModel.getLayouts();
		workflowIds = formModel.getWorkflowIds();
		constraint = formModel.getConstraint();
		contribution = formModel.isContribution();
		xslTemplate = formModel.getXslTemplate();
		xslFilename = formModel.getXslFilename();
		printButtonLabel = formModel.getPrintButtonLabel();
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	@JsonManagedReference
	public final List<Layout> getLayouts() {
		return layouts;
	}

	@JsonManagedReference
	public final void setLayouts(final List<Layout> layouts) {
		this.layouts = layouts;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	public final String getId() {
		return id;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(final boolean optional) {
		this.optional = optional;
	}

	@JsonIgnore
	public final boolean isContribution() {
		return contribution;
	}

	@JsonIgnore
	public final void setContribution(final boolean contribution) {
		this.contribution = contribution;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	public final SortedMap<String, String> getPrintButtonLabel() {
		return printButtonLabel;
	}

	public final void setPrintButtonLabel(final SortedMap<String, String> printButtonLabel) {
		this.printButtonLabel = printButtonLabel;
	}

	public final RuleConstraint getConstraint() {
		return constraint;
	}

	public final void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	public final String getXslTemplate() {
		return xslTemplate;
	}

	public final void setXslTemplate(final String xslTemplate) {
		this.xslTemplate = xslTemplate;
	}

	public final String getXslFilename() {
		return xslFilename;
	}

	public final void setXslFilename(final String xslFilename) {
		this.xslFilename = xslFilename;
	}

	@JsonIgnore
	public String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getStudy().getDefaultLanguage().getId());
	}

	@JsonIgnore
	public Layout getLayout(final String layoutId) {
		return layouts.stream()
			.filter(l -> l.getId().equalsIgnoreCase(layoutId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.LAYOUT, layoutId));
	}

	@JsonIgnore
	public List<Cell> getCells() {
		return getLayouts().stream().flatMap(l -> l.getCells().stream()).toList();
	}

	@JsonIgnore
	public Cell getCell(final String cellId) {
		return getCells().stream()
			.filter(c -> c.getId().equalsIgnoreCase(cellId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.CELL, cellId));
	}

	@JsonIgnore
	public boolean containsFieldModel(final FieldModel fieldModel) {
		return getCells().stream().anyMatch(c -> c.containsFieldModel(fieldModel));
	}

	@JsonIgnore
	public List<DatasetModel> getDatasetModels() {
		return getCells().stream().filter(Cell::hasFieldModel).map(Cell::getDatasetModel).toList();
	}

	@JsonIgnore
	public List<FieldModel> getFieldModels() {
		return getCells().stream().filter(Cell::hasFieldModel).map(Cell::getFieldModel).toList();
	}

	@JsonIgnore
	public List<FieldModel> getFieldModelsContainingDataRequired() {
		return getFieldModels().stream().filter(FieldModel::isRequired).toList();
	}

	@Override
	@JsonIgnore
	public String getAssignableDescription() {
		return getId();
	}

	@Override
	public List<String> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<String> workflowIds) {
		this.workflowIds = workflowIds;
	}

	@Override
	@JsonIgnore
	public List<Workflow> getWorkflows() {
		return getStudy().getNodesFromIds(Entity.WORKFLOW, getWorkflowIds());
	}

	@Override
	public Entity getEntity() {
		return Entity.FORM_MODEL;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	@JsonIgnore
	public int compareTo(final FormModel formModel) {
		return id.compareTo(formModel.id);
	}

	@JsonIgnore
	public List<ScopeModel> getScopeModels() {
		return study.getScopeModels().stream().filter(s -> s.getFormModelIds().contains(id)).toList();
	}

	@JsonIgnore
	public List<EventModel> getEventModels() {
		return study.getEventModels().stream().filter(e -> e.getFormModelIds().contains(id)).toList();
	}

	@JsonIgnore
	public boolean isScopeDocumentation() {
		return !getScopeModels().isEmpty();
	}

	@Override
	@JsonIgnore
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case WORKFLOW:
				return Collections.unmodifiableList(getWorkflows());
			case FIELD_MODEL:
				return Collections.unmodifiableList(getFieldModels());
			default:
				return Collections.emptyList();
		}
	}
}
