package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentPlan {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private String currency;
	private String invoicedScopeModel;
	private String workflow;
	private Boolean allowBatchMerger;
	private Boolean extendedSteps;

	private List<PaymentStep> steps;

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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(final String currency) {
		this.currency = currency;
	}

	public String getInvoicedScopeModel() {
		return invoicedScopeModel;
	}

	public void setInvoicedScopeModel(final String invoicedScopeModel) {
		this.invoicedScopeModel = invoicedScopeModel;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(final String workflow) {
		this.workflow = workflow;
	}

	public Boolean getAllowBatchMerger() {
		return allowBatchMerger;
	}

	public void setAllowBatchMerger(final Boolean allowBatchMerger) {
		this.allowBatchMerger = allowBatchMerger;
	}

	public Boolean getExtendedSteps() {
		return extendedSteps;
	}

	public void setExtendedSteps(final Boolean extendedSteps) {
		this.extendedSteps = extendedSteps;
	}

	public List<PaymentStep> getSteps() {
		return steps;
	}

	public void setSteps(final List<PaymentStep> steps) {
		this.steps = steps;
	}

	@Override
	public String toString() {
		return "PaymentPlan{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", currency='" + currency + '\'' +
			", invoicedScopeModel='" + invoicedScopeModel + '\'' +
			", workflow='" + workflow + '\'' +
			", allowBatchMerger=" + allowBatchMerger +
			", extendedSteps=" + extendedSteps +
			", steps=" + steps +
			'}';
	}
}
