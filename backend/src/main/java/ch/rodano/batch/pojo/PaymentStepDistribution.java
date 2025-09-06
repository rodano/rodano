package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentStepDistribution {

	private String scopeModelId;
	private String profileId;
	private Integer value;

	public String getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final String scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(final String profileId) {
		this.profileId = profileId;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(final Integer value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "PaymentStepDistribution{" +
			"scopeModelId='" + scopeModelId + '\'' +
			", profileId='" + profileId + '\'' +
			", value=" + value +
			'}';
	}
}
