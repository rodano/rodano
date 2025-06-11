package ch.rodano.core.model.scope;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.event.EventConfigurationHook;
import ch.rodano.core.model.enrollment.EnrollmentModel;
import ch.rodano.core.model.enrollment.EnrollmentTarget;
import ch.rodano.core.model.enrollment.SubscriptionRestriction;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class ScopeData implements Serializable {
	private static final long serialVersionUID = -2250927876597655682L;

	private SortedMap<String, String> description;

	//define how enrollment is supposed to happen (star, stop and targets at different time)
	private ZonedDateTime enrollmentStart;
	private ZonedDateTime enrollmentStop;
	private List<EnrollmentTarget> enrollmentTargets;

	//define how scope can be enrolled in this scope
	private EnrollmentModel enrollmentModel;
	//store configuration parts for virtual scopes
	private List<EventConfigurationHook> eventConfigurationHooks;
	//define which users can participate in this scope
	private List<SubscriptionRestriction> subscriptionRestrictions;

	public ScopeData() {
		description = new TreeMap<>();
		subscriptionRestrictions = new ArrayList<>();
		enrollmentTargets = new ArrayList<>();
		eventConfigurationHooks = new ArrayList<>();
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public ZonedDateTime getEnrollmentStart() {
		return enrollmentStart;
	}

	public void setEnrollmentStart(final ZonedDateTime enrollmentStart) {
		this.enrollmentStart = enrollmentStart;
	}

	public ZonedDateTime getEnrollmentStop() {
		return enrollmentStop;
	}

	public void setEnrollmentStop(final ZonedDateTime enrollmentStop) {
		this.enrollmentStop = enrollmentStop;
	}

	public List<EnrollmentTarget> getEnrollmentTargets() {
		return enrollmentTargets;
	}

	public void setEnrollmentTargets(final List<EnrollmentTarget> enrollmentTargets) {
		this.enrollmentTargets = enrollmentTargets;
	}

	public List<SubscriptionRestriction> getSubscriptionRestrictions() {
		return subscriptionRestrictions;
	}

	public void setSubscriptionRestrictions(final List<SubscriptionRestriction> subscriptionRestrictions) {
		this.subscriptionRestrictions = subscriptionRestrictions;
	}

	public List<EventConfigurationHook> getEventConfigurationHooks() {
		return eventConfigurationHooks;
	}

	public void setEventConfigurationHooks(final List<EventConfigurationHook> eventConfigurationHooks) {
		this.eventConfigurationHooks = eventConfigurationHooks;
	}

	public EnrollmentModel getEnrollmentModel() {
		return enrollmentModel;
	}

	public void setEnrollmentModel(final EnrollmentModel enrollmentModel) {
		this.enrollmentModel = enrollmentModel;
	}
}
