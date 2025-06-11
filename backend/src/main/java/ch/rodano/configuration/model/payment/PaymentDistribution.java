package ch.rodano.configuration.model.payment;

import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.scope.ScopeModel;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class PaymentDistribution implements Node {
	private static final long serialVersionUID = -7856978742306432331L;

	private PaymentStep step;

	private String scopeModelId;
	private String profileId;
	private double value;

	@JsonBackReference
	public final void setStep(final PaymentStep step) {
		this.step = step;
	}

	@JsonBackReference
	public final PaymentStep getStep() {
		return step;
	}

	public final String getScopeModelId() {
		return scopeModelId;
	}

	public final void setScopeModelId(final String scopeId) {
		scopeModelId = scopeId;
	}

	public final String getProfileId() {
		return profileId;
	}

	public final void setProfileId(final String profileId) {
		this.profileId = profileId;
	}

	public final double getValue() {
		return value;
	}

	public final void setValue(final double value) {
		this.value = value;
	}

	@Override
	public final Entity getEntity() {
		return Entity.PAYMENT_DISTRIBUTION;
	}

	@JsonIgnore
	public final Profile getProfile() {
		return getStep().getPaymentPlan().getStudy().getProfile(profileId);
	}

	@JsonIgnore
	public final void setProfile(final Profile profile) {
		profileId = profile == null ? null : profile.getId();
	}

	@JsonIgnore
	public final ScopeModel getScopeModel() {
		return getStep().getPaymentPlan().getStudy().getScopeModel(scopeModelId);
	}

	@JsonIgnore
	public final void setScopeModel(final ScopeModel scopeModel) {
		scopeModelId = scopeModel == null ? null : scopeModel.getId();
	}

	@JsonIgnore
	public final String getPayableModelId() {
		if(getProfileId() != null) {
			return profileId;
		}
		return scopeModelId;
	}

	@JsonIgnore
	public final PayableModel getPayableModel() {
		if(getProfileId() != null) {
			return getProfile();
		}
		return getScopeModel();
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

}
