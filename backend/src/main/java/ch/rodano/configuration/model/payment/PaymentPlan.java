package ch.rodano.configuration.model.payment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.text.WordUtils;

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
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.configuration.model.workflow.WorkflowableModel;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class PaymentPlan implements Serializable, SuperDisplayable, Node, RightAssignable<PaymentPlan> {
	private static final long serialVersionUID = 7456710559952363241L;

	private String id;
	private Study study;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private String currency;
	private String invoicedScopeModel;

	private String workflow;
	private String state;

	private boolean allowBatchMerger;
	private boolean extendedSteps;
	private String templateFile;

	private List<PaymentStep> steps;

	public PaymentPlan() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		steps = new ArrayList<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	public boolean isAllowBatchMerger() {
		return allowBatchMerger;
	}

	public void setAllowBatchMerger(final boolean allowBatchMerger) {
		this.allowBatchMerger = allowBatchMerger;
	}

	public String getTemplateFile() {
		return templateFile;
	}

	public void setTemplateFile(final String templateFile) {
		this.templateFile = templateFile;
	}

	public final String getInvoicedScopeModel() {
		return invoicedScopeModel;
	}

	public final void setInvoicedScopeModel(final String invoicedScopeModel) {
		this.invoicedScopeModel = invoicedScopeModel;
	}

	public final String getWorkflow() {
		return workflow;
	}

	public final void setWorkflow(final String workflow) {
		this.workflow = workflow;
	}

	public final String getState() {
		return state;
	}

	public final void setState(final String state) {
		this.state = state;
	}

	public final String getCurrency() {
		return currency;
	}

	public final void setCurrency(final String currency) {
		this.currency = currency;
	}

	public final boolean getExtendedSteps() {
		return extendedSteps;
	}

	public final void setExtendedSteps(final boolean extendedSteps) {
		this.extendedSteps = extendedSteps;
	}

	@JsonIgnore
	public Workflow getJSONWorkflow() {
		return study.getWorkflow(workflow);
	}

	@JsonIgnore
	public WorkflowableModel getWorkflowableModel() {
		//plan are based on workflows used only once
		return study.getWorkflow(workflow).getWorkflowableModels().get(0);
	}

	@JsonIgnore
	public WorkflowState getJSONState() {
		return getJSONWorkflow().getState(state);
	}

	@JsonManagedReference
	public final List<PaymentStep> getSteps() {
		return steps;
	}

	@JsonManagedReference
	public final void setSteps(final List<PaymentStep> steps) {
		this.steps = steps;
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
	public final PaymentStep getStepFromId(final String stepId) {
		return steps.stream()
			.filter(s -> s.getId().equalsIgnoreCase(stepId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.PAYMENT_STEP, stepId));
	}

	@JsonIgnore
	public final List<PayableModel> getPayableModels() {
		final List<PayableModel> payableModels = new ArrayList<>();
		getSteps().forEach(step -> step.getPayableModels().stream().filter(payableModel -> !payableModels.contains(payableModel)).forEach(payableModels::add));
		return payableModels;
	}

	@JsonIgnore
	public final List<EventModel> getEventModels() {
		return study.getEventModels().stream().filter(event -> event.getDatasetModelIds().contains(id)).toList();
	}

	@Override
	public final Entity getEntity() {
		return Entity.PAYMENT_PLAN;
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case PAYMENT_STEP:
				return Collections.unmodifiableList(steps);
			default:
				return Collections.emptyList();
		}
	}

	@Override
	public final int compareTo(final PaymentPlan paymentPlan) {
		return getId().compareTo(paymentPlan.getId());
	}

	@JsonIgnore
	public String getTemplateLabel() {
		return WordUtils.capitalize(getTemplateFile().substring(0, getTemplateFile().lastIndexOf('.')).replaceAll("_", " "));
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getStudy().getDefaultLanguage().getId());
	}

	@Override
	@JsonIgnore
	public String getAssignableDescription() {
		return getDefaultLocalizedShortname();
	}
}
