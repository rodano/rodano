package ch.rodano.configuration.model.payment;

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

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class PaymentStep implements Serializable, SuperDisplayable, Node, Comparable<PaymentStep> {
	private static final long serialVersionUID = -3643822784996243315L;

	private String id;
	private PaymentPlan paymentPlan;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private List<PaymentDistribution> distributions;

	private boolean repeatable;
	private String workflowable;

	public PaymentStep() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		distributions = new ArrayList<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference
	public void setPaymentPlan(final PaymentPlan paymentPlan) {
		this.paymentPlan = paymentPlan;
	}

	@JsonBackReference
	public PaymentPlan getPaymentPlan() {
		return paymentPlan;
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

	public final boolean isRepeatable() {
		return repeatable;
	}

	public final void setRepeatable(final boolean repeatable) {
		this.repeatable = repeatable;
	}

	public final String getWorkflowable() {
		return workflowable;
	}

	public final void setWorkflowable(final String workflowable) {
		this.workflowable = workflowable;
	}

	@JsonManagedReference
	public final List<PaymentDistribution> getDistributions() {
		return distributions;
	}

	@JsonManagedReference
	public final void setDistributions(final List<PaymentDistribution> distributions) {
		this.distributions = distributions;
	}

	@JsonIgnore
	public final PaymentDistribution getDistributionFromPayableModelId(final String payableModelId) {
		return distributions.stream()
			.filter(d -> d.getPayableModelId().equals(payableModelId))
			.findAny()
			.orElseThrow(() -> new RuntimeException(String.format("No distribution in step [%s] in plan [%s] for payable model [%s]", id, paymentPlan.getId(), payableModelId)));
	}

	@JsonIgnore
	public final List<PayableModel> getPayableModels() {
		return distributions.stream().map(PaymentDistribution::getPayableModel).distinct().toList();
	}

	@Override
	public final Entity getEntity() {
		return Entity.PAYMENT_STEP;
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case PAYMENT_DISTRIBUTION:
				return Collections.unmodifiableList(distributions);
			default:
				return Collections.emptyList();
		}
	}

	@Override
	public final int compareTo(final PaymentStep o) {
		return getPaymentPlan().getSteps().indexOf(this) - o.getPaymentPlan().getSteps().indexOf(o);
	}
}
